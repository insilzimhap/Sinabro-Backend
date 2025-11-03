package com.sinabro.backend.game.writing.service;

import com.sinabro.backend.game.writing.dto.*;
import com.sinabro.backend.game.writing.entity.*;
import com.sinabro.backend.game.writing.repository.*;
import com.sinabro.backend.record.entity.WritingGameResult;
import com.sinabro.backend.record.repository.WritingGameResultRepository;
import com.sinabro.backend.stage.entity.ChildFruitStatus;
import com.sinabro.backend.stage.entity.LearningFruit;
import com.sinabro.backend.stage.repository.ChildFruitStatusRepository;
import com.sinabro.backend.stage.repository.LearningFruitRepository;
import com.sinabro.backend.user.entity.Child;
import com.sinabro.backend.user.repository.ChildRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * WritingGameService
 * - 쓰기 게임 관련 주요 비즈니스 로직을 담당
 *   (start / choice / complete)
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class WritingGameService {

    private final WritingGameQuestionRepository questionRepository;
    private final WritingGameChoicesRepository choicesRepository;
    private final WritingGameResultRepository resultRepository;
    private final LearningFruitRepository learningFruitRepository;
    private final ChildRepository childRepository;
    private final ChildFruitStatusRepository childFruitStatusRepository;

    //================== 쓰기 게임용 비즈니스 메서드 ==================

    /**
     * 게임 시작 검증
     * - 입력 DTO: WritingGameStartRequestDto
     * - 동작:
     *   1) ChildFruitStatus.isActive = TRUE 확인
     *   2) Child, Fruit 존재 검증
     *   3) WritingGameResult 스텁 INSERT
     */
    @Transactional
    public WritingGameStartResponseDto start(WritingGameStartRequestDto req) {
        String childId = req.getChildId();
        String fruitId = req.getFruitId();
        log.info("[WritingGame][start] 시작 요청 childId={} fruitId={}", childId, fruitId);

        // 1️⃣ 자녀 존재 확인
        childRepository.findById(childId).orElseThrow(() -> {
            log.warn("[WritingGame][start] 자녀 미존재 childId={}", childId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "자녀를 찾을 수 없습니다.");
        });

        // 2️⃣ 열매 존재 확인
        LearningFruit fruit = learningFruitRepository.findById(fruitId)
                .orElseThrow(() -> {
                    log.warn("[WritingGame][start] 열매 미존재 fruitId={}", fruitId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 열매를 찾을 수 없습니다.");
                });

        // 3️⃣ 카테고리 확인
        if (fruit.getCategory() == null || !fruit.getCategory().name().equalsIgnoreCase("WRITING_GAME")) {
            log.warn("[WritingGame][start] 열매가 쓰기 게임 카테고리가 아님 fruitId={} category={}", fruitId, fruit.getCategory());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 열매는 쓰기 게임용이 아닙니다.");
        }

        // 4️⃣ ChildFruitStatus 기반 활성 여부 검증 // changed
        Optional<ChildFruitStatus> statusOpt =
                childFruitStatusRepository.findByChildIdAndFruitId(childId, fruitId);

        if (statusOpt.isEmpty() || !statusOpt.get().isActive()) {
            log.warn("[WritingGame][start] 잠금 상태 - childId={} fruitId={}", childId, fruitId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "잠금된 열매입니다. 입장 불가.");
        }


        // 3️⃣ resultId 생성 및 스텁 저장
        String resultId = "wg-res-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        WritingGameResult stub = WritingGameResult.builder()
                .wgResultId(resultId)
                .resultType("쓰기 게임")
                .wgChildId(childId)
                .fruitId(fruitId)
                .wgScore(0)
                .totalQuestions(0) // 나중에 채워질 예정
                .isSuccess(false)
                .timeSpentSecs(null)
                .build();
        resultRepository.save(stub);
        log.info("[WritingGame][start] 새로운 세션 스텁 생성 완료 → resultId={} childId={} fruitId={}",
                resultId, childId, fruitId);


        return WritingGameStartResponseDto.builder()
                .resultId(resultId)
                .fruitId(fruitId)
                .questionCount(0) // 문제는 프론트에서 랜덤 처리
                .questions(Collections.emptyList())
                .isActive(statusOpt.get().isActive())
                .build();
    }

    /**
     * 선택 기록 저장
     * - 입력 DTO: WritingGameChoiceRequestDto
     * - 동작:
     *   1) resultId 존재 확인
     *   2) questionId 존재 확인
     *   3) WritingGameChoices 엔티티 생성 및 저장
     *   4) UNIQUE(wg_result_id, wg_question_id) 위반 시 409 반환
     */
    @Transactional
    public void choice(WritingGameChoiceRequestDto req) {
        String resultId = req.getResultId();
        String questionId = req.getQuestionId();

        log.info("[WritingGame][choice] 기록 요청 resultId={} questionId={} isCorrect={}", resultId, questionId, req.getIsCorrect());

        // 1️⃣ result 존재 확인
        if (!resultRepository.existsById(resultId)) {
            log.warn("[WritingGame][choice] resultId={} 존재하지 않음", resultId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게임 세션(resultId)을 찾을 수 없습니다.");
        }

        // 2️⃣ question 존재 확인
        questionRepository.findById(questionId).orElseThrow(() -> {
            log.warn("[WritingGame][choice] 문제 없음 questionId={}", questionId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "문제를 찾을 수 없습니다.");
        });

        // 3️⃣ 엔티티 생성
        String choiceId = "wch-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        WritingGameChoices choice = WritingGameChoices.builder()
                .wgChoiceId(choiceId)
                .wgResultId(resultId)
                .wgQuestionId(questionId)
                .childWrittenText(req.getChildWrittenText())
                .isCorrect(req.getIsCorrect())
                .build();

        try {
            choicesRepository.save(choice);
            log.info("[WritingGame][choice] 선택 기록 저장 성공 choiceId={} resultId={}", choiceId, resultId);
        } catch (DataIntegrityViolationException ex) {
            log.warn("[WritingGame][choice] 무결성 오류(중복 가능) resultId={} questionId={}", resultId, questionId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 해당 문제에 대한 기록이 존재합니다.");
        }

        // ✅ Stage2~3 문제수 고정(4문항) 세팅: choice 시점에서 totalQuestions=4로 업데이트
        resultRepository.findById(resultId).ifPresent(r -> { // changed
            String fruitId = r.getFruitId();
            learningFruitRepository.findById(fruitId).ifPresent(f -> {
                boolean isStage1 = isStage1(f.getStageId()); // changed
                if (isStage1) {
                    if (r.getTotalQuestions() <= 0) {
                        int qCount = getQuestionCountByFruitId(fruitId); // changed
                        r.setTotalQuestions(qCount);
                        resultRepository.save(r);
                        log.info("[WritingGame][choice] Stage1 totalQuestions={} 세팅 완료 resultId={}", qCount, resultId);
                    }
                } else {
                    if (r.getTotalQuestions() <= 0 || r.getTotalQuestions() != 4) {
                        r.setTotalQuestions(4);
                        resultRepository.save(r);
                        log.info("[WritingGame][choice] Stage2~3 totalQuestions=4 세팅 완료 resultId={}", resultId);
                    }
                }
            });
        }); // changed
    }

    /**
     * 게임 완료 처리
     * - 입력 DTO: WritingGameCompleteRequestDto
     * - 동작:
     *   1) child, fruit 존재 검증
     *   2) choices 집계 -> 정답 수, 전체 문항 수 계산
     *   3) WritingGameResult UPDATE
     *   4) 성공 시 다음 열매 활성화
     */
    @Transactional
    public WritingGameResult complete(WritingGameCompleteRequestDto req) {
        String childId = req.getChildId();
        String fruitId = req.getFruitId();
        String resultId = req.getResultId();
        Integer timeSpentSecs = req.getTimeSpentSecs();

        log.info("[WritingGame][complete] 완료 요청 childId={} fruitId={} resultId={}", childId, fruitId, resultId);

        // 1️⃣ 자녀 확인
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> {
                    log.warn("[WritingGame][complete] 자녀 미존재 childId={}", childId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "자녀를 찾을 수 없습니다.");
                });

        // 2️⃣ 열매 확인
        LearningFruit fruit = learningFruitRepository.findById(fruitId)
                .orElseThrow(() -> {
                    log.warn("[WritingGame][complete] 열매 미존재 fruitId={}", fruitId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "열매를 찾을 수 없습니다.");
                });

        // 3️⃣ 결과 존재 확인
        WritingGameResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> {
                    log.warn("[WritingGame][complete] 결과(resultId) 미존재 resultId={}", resultId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "결과(resultId)를 찾을 수 없습니다.");
                });

        // 4️⃣ 선택 기록 조회
        var choices = choicesRepository.findByWgResultId(resultId);

        // Stage 1은 choices 없어도 통과 허용
        boolean skipChoiceCheck = isStage1(fruit.getStageId()); // changed
        if (choices.isEmpty() && !skipChoiceCheck) {
            log.warn("[WritingGame][complete] 기록 없음 resultId={}", resultId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택 기록이 존재하지 않습니다.");
        }
        if (skipChoiceCheck) {
            log.info("[WritingGame][complete] Stage1 자동 통과 처리 — choices 없음 허용 resultId={}", resultId);
        }


        // 5️⃣ 결과 계산
        int totalQuestions = choices.size();
        int correctCount = (int) choices.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsCorrect()))
                .count();
        boolean isSuccess = determineSuccess(fruit, correctCount, totalQuestions);

        // 6️⃣ 결과 업데이트
        result.setWgScore(correctCount);
        result.setTotalQuestions(totalQuestions);
        result.setSuccess(isSuccess);
        result.setTimeSpentSecs(timeSpentSecs);

        resultRepository.saveAndFlush(result);
        log.info("[WritingGame][complete] 결과 저장 완료 resultId={} score={} total={} success={}",
                resultId, correctCount, totalQuestions, isSuccess);

        // 7️⃣ 성공 시 다음 열매 활성화 (자녀별 Child_Fruit_Status 기반)
        if (isSuccess) {
            try {
                activateNextFruitIfExists(childId, fruit);
            } catch (Exception e) {
                log.warn("[WritingGame][complete] 다음 열매 활성화 실패: {}", e.getMessage());
            }
        }

        return result;
    }

    /// 🚨 나중에 gpt api 리포트 코드 넣어야 함!!! 🚨


    /**
     * 나무(열매 진행도) 조회
     * - stageId, childId를 받아 해당 단계의 열매 리스트와 상태(last result)를 반환
     * - 반환 DTO: WritingGameTreeResponseDto
     */
    @Transactional(readOnly = true)
    public List<WritingGameTreeResponseDto> tree(String stageId, String childId) {
        log.info("[WritingGame][tree] 조회 요청 stageId={} childId={}", stageId, childId);

        // LearningFruit의 카테고리는 프로젝트 공용 enum을 그대로 사용
        List<LearningFruit> fruits = learningFruitRepository
                .findByCategoryAndStageIdOrderBySequenceInStage(
                        com.sinabro.backend.progress.entity.Category.writing_game, stageId);

        Map<String, Optional<WritingGameResult>> latestByFruit = new HashMap<>();
        if (childId != null && !childId.isBlank()) {
            for (LearningFruit f : fruits) {
                Optional<WritingGameResult> latest = resultRepository
                        .findTop1ByWgChildIdAndFruitIdOrderByWgPlayDateDesc(childId, f.getFruitId());
                latestByFruit.put(f.getFruitId(), latest);
            }
        }

        List<WritingGameTreeResponseDto> resp = fruits.stream().map(f -> {
            Optional<WritingGameResult> latest = latestByFruit.getOrDefault(f.getFruitId(), Optional.empty());
            Boolean lastSuccess = latest.map(WritingGameResult::isSuccess).orElse(null);
            Integer lastScore   = latest.map(WritingGameResult::getWgScore).orElse(null);
            return WritingGameTreeResponseDto.builder()
                    .fruitId(f.getFruitId())
                    .title(f.getTitle())
                    .isActive(f.isActive())
                    .lastSuccess(lastSuccess)
                    .lastScore(lastScore)
                    .build();
        }).collect(Collectors.toList());

        log.info("[WritingGame][tree] 조회 완료 stageId={} count={}", stageId, resp.size());
        return resp;
    }


    //================== 내부 유틸 메서드 ==================

    /**
     * 성공 판정
     * - Stage 1(3세용): 무조건 통과
     * - Stage 2~3(4세 이상): 정답 3개 이상 시 통과
     */
    private boolean determineSuccess(LearningFruit fruit, int correctCount, int totalQuestions) {
        String stageId = fruit.getStageId();

        // 🌳 Stage 1 → 무조건 통과
        if (isStage1(stageId)) {
            log.info("[WritingGame][determineSuccess] Stage1(초급) 자동 통과 처리 stageId={}", stageId); // changed
            return true;
        }

        // 🌳 Stage 2~3 → 점수 기준 통과
        boolean result = correctCount >= 3;
        log.info("[WritingGame][determineSuccess] stageId={} correct={}/{} → success={}",
                stageId, correctCount, totalQuestions, result);
        return result;
    }


    /**
     * 단계별 문항 수 반환
     * - 1~2단계: 2문항
     * - 나머지 단계: 4문항
     */
    private int getQuestionCountByFruitId(String fruitId) {
        return switch (fruitId) {
            case "FR_WG_001", "FR_WG_002", "FR_WG_003", "FR_WG_004" -> 2;
            default -> 4;
        };
    }

    /**
     * 다음 열매 활성화 처리
     * - 현재 열매의 sequence_in_stage + 1 항목을 찾아 is_active=true 로 설정
     */
    private void activateNextFruitIfExists(String childId, LearningFruit currentFruit) {
        try {
            String stageId = currentFruit.getStageId();
            int nextSeq = currentFruit.getSequenceInStage() + 1;

            // ✅ 쓰기 게임(ST010~ST012)에서는 ST012 이후는 멈춤
            if ("ST013".equals(stageId)) {
                log.info("[ListeningGame][activate] ST013(쓰기게임 마지막) 이후 단계 없음 — 다음 Stage 활성화 중단");
                return;
            }

            // 1️⃣ 현재 stage 내 다음 열매 탐색
            Optional<LearningFruit> nextOpt = learningFruitRepository.findByStageIdAndSequenceInStage(stageId, nextSeq);
            if (nextOpt.isPresent()) {
                LearningFruit next = nextOpt.get();
                upsertChildFruitActive(childId, next.getFruitId());
                log.info("[WritingGame][activate] 다음 열매 활성화 완료(child별) childId={} nextFruitId={}", childId, next.getFruitId());
            } else {
                // 2️⃣ 현재가 마지막 열매일 경우 다음 Stage의 첫 열매 활성화
                String nextStageId = tryIncrementStageId(stageId);
                if (nextStageId != null) {
                    Optional<LearningFruit> nextStageFirst = learningFruitRepository.findByStageIdAndSequenceInStage(nextStageId, 1);
                    if (nextStageFirst.isPresent()) {
                        upsertChildFruitActive(childId, nextStageFirst.get().getFruitId());
                        log.info("[WritingGame][activate] 다음 Stage 첫 열매 활성화(child별) childId={} nextStageId={} fruitId={}",
                                childId, nextStageId, nextStageFirst.get().getFruitId());
                    } else {
                        log.info("[WritingGame][activate] 다음 Stage 첫 열매 없음 nextStageId={}", nextStageId);
                    }
                } else {
                    log.info("[WritingGame][activate] 다음 Stage 계산 불가 stageId={}", stageId);
                }
            }
        } catch (Exception e) {
            log.warn("[WritingGame][activate] 예외 발생(child별): {}", e.getMessage());
        }
    }

    // ✅ Child_Fruit_Status upsert helper (INSERT or UPDATE TRUE)
    private void upsertChildFruitActive(String childId, String fruitId) {
        Optional<ChildFruitStatus> cur = childFruitStatusRepository.findByChildIdAndFruitId(childId, fruitId);
        if (cur.isPresent()) {
            if (!cur.get().isActive()) {
                childFruitStatusRepository.activateFruit(childId, fruitId); // UPDATE is_active=TRUE
            }
        } else {
            ChildFruitStatus entity = ChildFruitStatus.builder()
                    .id(new com.sinabro.backend.stage.entity.ChildFruitStatusId(childId, fruitId))
                    .isActive(true)
                    .openedAt(LocalDateTime.now())
                    .build();
            childFruitStatusRepository.save(entity); // INSERT
        }
    }


    /**
     * Stage ID 증가 시도
     * - 예: "ST001" → "ST002" 숫자 부분 +1 (실패 시 null)
     * - 숫자 자릿수(3자리) 유지
     */
    private String tryIncrementStageId(String stageId) {
        if (stageId == null) return null;
        try {
            // "ST001" → prefix="ST", digits="001"
            String prefix = stageId.replaceAll("[0-9]", "");
            String digits = stageId.replaceAll("\\D", "");
            if (digits.isEmpty()) return null;

            // 현재 숫자 +1
            int num = Integer.parseInt(digits);
            // 숫자 길이만큼 0패딩 유지 (예: width=3 → "002")
            String nextDigits = String.format("%0" + digits.length() + "d", num + 1);

            return prefix + nextDigits; // ST + 002 → "ST002"
        } catch (Exception e) {
            log.warn("[StageId][tryIncrementStageId] 변환 실패 stageId={} err={}", stageId, e.getMessage());
            return null;
        }
    }


    /**
     * Stage1(초급, 3세용) 판정
     * - stage_id가 ST010(쓰기 게임 초급)인 경우 자동 통과 처리
     * - ST001/004/007/010 → 초급 그룹
     */
    private boolean isStage1(String stageId) {
        if (stageId == null) return false;
        return switch (stageId) {
            case "ST001", "ST004", "ST007", "ST010" -> true;
            default -> false;
        };
    }

}
