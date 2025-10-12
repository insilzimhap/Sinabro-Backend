package com.sinabro.backend.game.writing.service;

import com.sinabro.backend.game.writing.dto.*;
import com.sinabro.backend.game.writing.entity.*;
import com.sinabro.backend.game.writing.repository.*;
import com.sinabro.backend.record.entity.WritingGameResult;
import com.sinabro.backend.record.repository.WritingGameResultRepository;
import com.sinabro.backend.stage.entity.LearningFruit;
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

    //================== 쓰기 게임용 비즈니스 메서드 ==================

    /**
     * 게임 시작 검증 및 문제 랜덤 출제
     * - 입력 DTO: WritingGameStartRequestDto
     * - 동작:
     *   1) LearningFruit 유효성 검증 (카테고리, 활성 상태)
     *   2) Child 존재 검증
     *   3) WritingGameResult 스텁 INSERT (기본값 저장)
     *   4) fruitId 기반 랜덤 문제 N개 조회 후 반환
     */
    @Transactional
    public WritingGameStartResponseDto start(WritingGameStartRequestDto req) {
        String childId = req.getChildId();
        String fruitId = req.getFruitId();
        log.info("[WritingGame][start] 시작 요청 childId={} fruitId={}", childId, fruitId);

        // 1️⃣ 열매 유효성 검증
        LearningFruit fruit = learningFruitRepository.findById(fruitId)
                .orElseThrow(() -> {
                    log.warn("[WritingGame][start] 열매 미존재 fruitId={}", fruitId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 열매를 찾을 수 없습니다.");
                });

        if (fruit.getCategory() == null || !fruit.getCategory().name().equalsIgnoreCase("WRITING_GAME")) {
            log.warn("[WritingGame][start] 열매가 쓰기 게임 카테고리가 아님 fruitId={} category={}", fruitId, fruit.getCategory());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 열매는 쓰기 게임용이 아닙니다.");
        }

        if (!fruit.isActive()) {
            log.warn("[WritingGame][start] 열매 비활성 상태로 입장 불가 fruitId={}", fruitId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 열매는 현재 활성화되어 있지 않습니다.");
        }

        // 2️⃣ 자녀 존재 확인
        childRepository.findById(childId).orElseThrow(() -> {
            log.warn("[WritingGame][start] 자녀 미존재 childId={}", childId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "자녀를 찾을 수 없습니다.");
        });

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

        // 4️⃣ 문제 랜덤 출제
        int questionCount = getQuestionCountByFruitId(fruitId);
        List<WritingGameQuestion> questions = questionRepository.findRandomQuestionsByFruitId(fruitId, questionCount);

        List<WritingQuestionDto> questionDtos = questions.stream()
                .map(q -> WritingQuestionDto.builder()
                        .wgQuestionId(q.getWgQuestionId())
                        .fruitId(q.getFruitId())
                        .wgSubjectTag(q.getWgSubjectTag().name())
                        .wgCorrectAnswer(q.getWgCorrectAnswer())
                        .build())
                .collect(Collectors.toList());

        log.info("[WritingGame][start] 시작 검증 완료 및 문제 {}개 출제됨", questionDtos.size());

        return WritingGameStartResponseDto.builder()
                .resultId(resultId)
                .fruitId(fruitId)
                .questionCount(questionDtos.size())
                .questions(questionDtos)
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

        log.info("[WritingGame][choice] 기록 요청 resultId={} questionId={} isCorrect={}", resultId, questionId, req.isCorrect());

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
                .isCorrect(req.isCorrect())
                .build();

        try {
            choicesRepository.save(choice);
            log.info("[WritingGame][choice] 선택 기록 저장 성공 choiceId={} resultId={}", choiceId, resultId);
        } catch (DataIntegrityViolationException ex) {
            log.warn("[WritingGame][choice] 무결성 오류(중복 가능) resultId={} questionId={}", resultId, questionId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 해당 문제에 대한 기록이 존재합니다.");
        }
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
        boolean skipChoiceCheck = fruit.getStageId().equalsIgnoreCase("ST01");
        if (choices.isEmpty() && !skipChoiceCheck) {
            log.warn("[WritingGame][complete] 기록 없음 resultId={}", resultId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택 기록이 존재하지 않습니다.");
        }
        if (skipChoiceCheck) {
            log.info("[WritingGame][complete] Stage1 자동 통과 처리 — choices 없음 허용 resultId={}", resultId);
        }


        // 5️⃣ 결과 계산
        int totalQuestions = choices.size();
        int correctCount = (int) choices.stream().filter(WritingGameChoices::isCorrect).count();
        boolean isSuccess = determineSuccess(fruit, correctCount, totalQuestions);

        // 6️⃣ 결과 업데이트
        result.setWgScore(correctCount);
        result.setTotalQuestions(totalQuestions);
        result.setSuccess(isSuccess);
        result.setTimeSpentSecs(timeSpentSecs);

        resultRepository.saveAndFlush(result);
        log.info("[WritingGame][complete] 결과 저장 완료 resultId={} score={} total={} success={}",
                resultId, correctCount, totalQuestions, isSuccess);

        // 7️⃣ 성공 시 다음 열매 활성화
        if (isSuccess) {
            try {
                activateNextFruitIfExists(fruit);
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
     * - Stage 1(4세용): 무조건 통과
     * - Stage 2~3(5세 이상): 정답 3개 이상 시 통과
     */
    private boolean determineSuccess(LearningFruit fruit, int correctCount, int totalQuestions) {
        String stageId = fruit.getStageId();

        // 🌳 Stage 1 → 무조건 통과
        if (stageId != null && stageId.equalsIgnoreCase("ST01")) {
            log.info("[WritingGame][determineSuccess] Stage1 자동 통과 처리 stageId={}", stageId);
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
            case "FR_WG_001", "FR_WG_002" -> 2;
            default -> 4;
        };
    }

    /**
     * 다음 열매 활성화 처리
     * - 현재 열매의 sequence_in_stage + 1 항목을 찾아 is_active=true 로 설정
     */
    private void activateNextFruitIfExists(LearningFruit currentFruit) {
        try {
            String stageId = currentFruit.getStageId();
            int nextSeq = currentFruit.getSequenceInStage() + 1;
            Optional<LearningFruit> nextOpt = learningFruitRepository.findByStageIdAndSequenceInStage(stageId, nextSeq);
            if (nextOpt.isPresent()) {
                LearningFruit next = nextOpt.get();
                if (!next.isActive()) {
                    try {
                        next.getClass().getMethod("setIsActive", boolean.class).invoke(next, true);
                        learningFruitRepository.save(next);
                        log.info("[WritingGame][activate] 다음 열매 활성화 완료 nextFruitId={}", next.getFruitId());
                    } catch (NoSuchMethodException nsme) {
                        log.info("[WritingGame][activate] setter 없음. Repository의 activateByFruitId 사용 시도 nextFruitId={}", next.getFruitId());
                        learningFruitRepository.activateByFruitId(next.getFruitId());
                        log.info("[WritingGame][activate] 다음 열매 활성화(레포 방식) 완료 nextFruitId={}", next.getFruitId());
                    }
                } else {
                    log.info("[WritingGame][activate] 다음 열매 이미 활성화됨 nextFruitId={}", next.getFruitId());
                }
            } else {
                log.info("[WritingGame][activate] 다음 열매 없음 stageId={} nextSeq={}", stageId, nextSeq);
            }
        } catch (Exception e) {
            log.warn("[WritingGame][activate] 예외 발생: {}", e.getMessage());
        }
    }
}
