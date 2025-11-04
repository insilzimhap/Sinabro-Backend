package com.sinabro.backend.game.listening.service;

import com.sinabro.backend.game.listening.dto.*;
import com.sinabro.backend.game.listening.entity.*;
import com.sinabro.backend.record.entity.ListeningGameResult;
import com.sinabro.backend.game.listening.repository.*;
import com.sinabro.backend.record.repository.ListeningGameResultRepository;
import com.sinabro.backend.stage.entity.ChildFruitStatus;
import com.sinabro.backend.stage.entity.ChildFruitStatusId;
import com.sinabro.backend.stage.entity.LearningFruit;
import com.sinabro.backend.stage.repository.ChildFruitStatusRepository;
import com.sinabro.backend.stage.repository.LearningFruitRepository;
import com.sinabro.backend.user.entity.Child;
import com.sinabro.backend.user.repository.ChildRepository;
import com.sinabro.backend.weakness.service.ChildWeaknessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.sinabro.backend.progress.repository.ChildProgressRepository;
import com.sinabro.backend.progress.entity.Category;
import com.sinabro.backend.progress.entity.ChildProgress;
import com.sinabro.backend.progress.entity.ChildProgressId;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * ListeningGameService
 * - 듣기 게임 관련 주요 비즈니스 로직을 담당
 *   (start / choice / complete / tree + 기존 report용 processListeningGameResult 유지)
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class ListeningGameService {

    private final ListeningGameResultRepository listeningGameResultRepository;
    private final ListeningGameQuestionRepository questionRepository;
    private final ListeningGameOptionRepository optionRepository;
    private final ListeningGameChoicesRepository choicesRepository;
    private final LearningFruitRepository learningFruitRepository;
    private final ChildRepository childRepository;
    private final ChildWeaknessService childWeaknessService;
    private final ChildFruitStatusRepository childFruitStatusRepository;
    private final ChildProgressRepository childProgressRepository;


    //================== 듣기 게임용 비즈니스 메서드 ==================

    /**
     * 게임 시작 검증
     * - 입력 DTO: ListeningGameStartRequestDto
     * - 동작:
     *   1) LearningFruit 유효성 검증 (카테고리, 활성 상태)
     *   2) Child 존재 검증
     *   3) ListeningGameResult 스텁 INSERT (기본값 저장)
     *   4) resultId 및 총 문항 수 반환
     *
     * 실패 시 ResponseStatusException으로 적절한 HTTP 상태 반환
     */
    @Transactional
    public ListeningGameStartResponseDto start(ListeningGameStartRequestDto req) {
        String childId = req.getChildId();
        String fruitId = req.getFruitId();
        log.info("[ListeningGame][start] 시작 요청 childId={} fruitId={}", childId, fruitId);


        // 1️⃣ 열매 존재 및 카테고리 검증
        LearningFruit fruit = learningFruitRepository.findById(fruitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 열매를 찾을 수 없습니다."));

        if (!fruit.getCategory().name().equalsIgnoreCase("listening_game")) {
            log.warn("[ListeningGame][start] 열매가 듣기 게임 카테고리가 아님 fruitId={} category={}", fruitId, fruit.getCategory());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "듣기 게임 전용 열매가 아닙니다.");
        }

        // 2️⃣ 자녀 존재 확인
        childRepository.findById(childId).orElseThrow(() -> {
            log.warn("[ListeningGame][start] 자녀 미존재 childId={}", childId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "자녀를 찾을 수 없습니다.");
        });

        // 3️⃣ 자녀별 활성 여부 확인
        Optional<ChildFruitStatus> statusOpt = childFruitStatusRepository.findByChildIdAndFruitId(childId, fruitId);
        if (statusOpt.isEmpty() || !statusOpt.get().isActive()) {
            log.warn("[ListeningGame][start] 잠금 상태(입장 불가) childId={} fruitId={}", childId, fruitId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "잠금된 열매입니다. 입장 불가.");
        }

        log.info("[ListeningGame][start] 시작 검증 통과 childId={} fruitId={}", childId, fruitId);


        // 3️⃣ ListeningGameResult 스텁 INSERT
        String resultId = "lg-res-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        int totalQuestions = (int) questionRepository.countByFruitId(fruitId);

        ListeningGameResult stub = ListeningGameResult.builder()
                .lgResultId(resultId)
                .resultType("듣기 게임")
                .lgChildId(childId)
                .fruitId(fruitId)
                .lgScore(0)
                .totalQuestions(totalQuestions)
                .isSuccess(false)
                .timeSpentSecs(null)
                .build();

        listeningGameResultRepository.save(stub);
        log.info("[ListeningGame][start] 스텁 결과 생성 완료 resultId={} totalQuestions={}", resultId, totalQuestions);

        // 4️⃣ resultId 및 총 문항 수 반환
        return ListeningGameStartResponseDto.builder()
                .resultId(resultId)
                .totalQuestions(totalQuestions)
                .isActive(true)
                .build();

    }


    /**
     * 선택 기록 저장
     * - 입력 DTO: ListeningGameChoiceRequestDto
     * - 동작:
     *   1) option 존재 확인 (정답 여부 읽기)
     *   2) question 존재 확인
     *   3) ListeningGameChoices 엔티티 생성 및 저장 (is_correct는 Option 스냅샷)
     *   4) UNIQUE(lg_result_id, lg_question_id) 위반 시 409 반환
     */
    @Transactional
    public void choice(ListeningGameChoiceRequestDto req) {
        String resultId = req.getResultId();
        String questionId = req.getQuestionId();
        String optionId = req.getOptionId();

        log.info("[ListeningGame][choice] 선택 요청 resultId={} questionId={} optionId={}", resultId, questionId, optionId);

        // option 확인
        ListeningGameOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> {
                    log.warn("[ListeningGame][choice] 선택지 없음 optionId={}", optionId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "선택지를 찾을 수 없습니다.");
                });

        // result 존재 확인
        if (!listeningGameResultRepository.existsById(resultId)) {
            log.warn("[ListeningGame][choice] resultId={} 존재하지 않음", resultId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게임 세션(resultId)을 찾을 수 없습니다.");
        }

        // question 확인
        questionRepository.findById(questionId).orElseThrow(() -> {
            log.warn("[ListeningGame][choice] 문제 없음 questionId={}", questionId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "문제를 찾을 수 없습니다.");
        });

        // Choices 엔티티 생성
        String choiceId = "lch-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        ListeningGameChoices choice = ListeningGameChoices.builder()
                .lgChoiceId(choiceId)
                .lgResultId(resultId)
                .lgQuestionId(questionId)
                .lgOptionId(optionId)
                .isCorrect(option.isCorrect())
                .answeredAt(LocalDateTime.now())
                .build();

        try {
            choicesRepository.save(choice);
            log.info("[ListeningGame][choice] 선택 기록 저장 성공 choiceId={} resultId={}", choiceId, resultId);
        } catch (DataIntegrityViolationException ex) {
            // UNIQUE 제약 위반 또는 FK 무결성 오류 처리
            log.warn("[ListeningGame][choice] 무결성 오류(중복 가능) resultId={} questionId={}", resultId, questionId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 해당 문제에 대한 선택이 존재합니다.");
        }
    }

    /**
     * 게임 완료 처리
     * - 입력 DTO: ListeningGameCompleteRequestDto
     * - 동작:
     *   1) child, fruit 존재 검증
     *   2) choices 집계 -> 정답 개수, 전체 문항 수 계산
     *   3) ListeningGameResult 기존 행 UPDATE
     *   4) 성공이면 다음 열매 활성화, 취약점 분석 호출
     */
    @Transactional
    public ListeningGameResult complete(ListeningGameCompleteRequestDto req) {
        String childId = req.getChildId();
        String fruitId = req.getFruitId();
        String resultId = req.getResultId();
        Integer timeSpentSecs = req.getTimeSpentSecs();

        log.info("[ListeningGame][complete] 완료 요청 childId={} fruitId={} resultId={}", childId, fruitId, resultId);

        // 1️⃣ 자녀 존재 확인
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> {
                    log.warn("[ListeningGame][complete] 자녀 미존재 childId={}", childId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "자녀를 찾을 수 없습니다.");
                });

        // 2️⃣ 열매 존재 확인
        LearningFruit fruit = learningFruitRepository.findById(fruitId)
                .orElseThrow(() -> {
                    log.warn("[ListeningGame][complete] 열매 미존재 fruitId={}", fruitId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "열매를 찾을 수 없습니다.");
                });

        // 3️⃣ 기존 Result 존재 여부 확인
        ListeningGameResult result = listeningGameResultRepository.findById(resultId)
                .orElseThrow(() -> {
                    log.warn("[ListeningGame][complete] 결과(resultId) 미존재 resultId={}", resultId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "결과(resultId)를 찾을 수 없습니다.");
                });

        // 4️⃣ 선택 기록 조회
        List<ListeningGameChoices> choices = choicesRepository.findByLgResultId(resultId);
        if (choices.isEmpty()) {
            log.warn("[ListeningGame][complete] 선택 기록 없음 resultId={}", resultId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택 기록이 존재하지 않습니다.");
        }

        // 5️⃣ 결과 계산
        int totalQuestions = choices.size();
        int correctCount = (int) choices.stream().filter(ListeningGameChoices::isCorrect).count();
        boolean isSuccess = determineSuccess(correctCount, totalQuestions);


        // 6️⃣ 기존 Result UPDATE
        result.setLgScore(correctCount);
        result.setTotalQuestions(totalQuestions);
        result.setSuccess(isSuccess);
        result.setTimeSpentSecs(timeSpentSecs);

        listeningGameResultRepository.saveAndFlush(result);
        log.info("[ListeningGame][complete] 결과 갱신 완료 resultId={} score={} total={} success={}",
                resultId, correctCount, totalQuestions, isSuccess);

        // 7️⃣ 성공 시 후속 처리: 자녀별 다음 열매 활성화 (Child_Fruit_Status 조작) //changed
        if (isSuccess) {
            try {
                activateNextFruitForChild(childId, fruit); //changed
            } catch (Exception e) {
                log.warn("[ListeningGame][complete] 다음 열매 활성화 실패(child별) childId={} fruitId={} err={}",
                        childId, fruitId, e.getMessage()); //changed
            }
        }

        // 8️⃣ 취약점 분석 (주석 해제!)
        log.info("[ListeningGame][complete] 취약점 분석 호출...");
        childWeaknessService.analyzeAndUpsertWeakness(child, fruitId);

        // 9️⃣ [추가] ChildProgress 업데이트 (Category.listening_game 사용)
        log.info("[ListeningGame][complete] ChildProgress 업데이트 호출...");
        updateChildProgress(childId, Category.listening_game, fruitId);

        return result;
    }



    /**
     * 나무 조회 (프론트용)
     * - stageId, childId를 받아 해당 단계의 열매 리스트와 상태(last result)를 반환
     * - 반환 DTO: ListeningGameTreeResponseDto
     */
    @Transactional(readOnly = true)
    public List<ListeningGameTreeResponseDto> tree(String stageId, String childId) {
        log.info("[ListeningGame][tree] 조회 요청 stageId={} childId={}", stageId, childId);

        // LearningFruit의 카테고리 enum 값은 프로젝트의 Category enum에 따름 (예: LISTENING_GAME)
        List<LearningFruit> fruits = learningFruitRepository
                .findByCategoryAndStageIdOrderBySequenceInStage(com.sinabro.backend.progress.entity.Category.listening_game, stageId);

        Map<String, Optional<ListeningGameResult>> latestByFruit = new HashMap<>();
        if (childId != null && !childId.isBlank()) {
            for (LearningFruit f : fruits) {
                Optional<ListeningGameResult> latest = listeningGameResultRepository
                        .findTop1ByLgChildIdAndFruitIdOrderByLgPlayDateDesc(childId, f.getFruitId());
                latestByFruit.put(f.getFruitId(), latest);
            }
        }

        // 🔁 isActive는 이제 Child_Fruit_Status 기준으로 계산 (공용 is_active 사용 제거) //changed
        List<ListeningGameTreeResponseDto> resp = fruits.stream().map(f -> {
            Optional<ListeningGameResult> latest = latestByFruit.getOrDefault(f.getFruitId(), Optional.empty());
            Boolean lastSuccess = latest.map(ListeningGameResult::isSuccess).orElse(null);
            Integer lastScore   = latest.map(ListeningGameResult::getLgScore).orElse(null);

            boolean isActiveForChild = false; //changed
            if (childId != null && !childId.isBlank()) { //changed
                isActiveForChild = childFruitStatusRepository
                        .findByChildIdAndFruitId(childId, f.getFruitId())
                        .map(ChildFruitStatus::isActive)
                        .orElse(false);
            } //changed

            return ListeningGameTreeResponseDto.builder()
                    .fruitId(f.getFruitId())
                    .title(f.getTitle())
                    .isActive(isActiveForChild) //changed
                    .lastSuccess(lastSuccess)
                    .lastScore(lastScore)
                    .build();
        }).collect(Collectors.toList());

        log.info("[ListeningGame][tree] 조회 완료 stageId={} count={}", stageId, resp.size());
        return resp;
    }



    // report 용
    @Transactional
    public void processListeningGameResult(ListeningGameResultDto dto) {
        // 1. 게임 결과(Result) 엔티티 생성 및 DB 저장
        ListeningGameResult result = ListeningGameResult.builder()
                .lgResultId("lg-res-" + UUID.randomUUID()) // 결과 ID 랜덤 생성
                .lgChildId(dto.getChildId())
                .fruitId(dto.getFruitId())
                .lgScore(dto.getScore())
                .totalQuestions(dto.getTotalQuestions())
                .isSuccess(dto.isSuccess())
                .timeSpentSecs(dto.getTimeSpentSecs())
                .build();
        listeningGameResultRepository.save(result);

        // 2. 자녀 엔티티 조회
        Child child = childRepository.findById(dto.getChildId())
                .orElseThrow(() -> new RuntimeException("Child not found"));

        // 3. 취약점 분석 서비스 호출
        childWeaknessService.analyzeAndUpsertWeakness(child, dto.getFruitId());

        // 4. 게임 성공 시, 보상(스티커) 지급 서비스 호출
        if (dto.isSuccess()) {
            //rewardService.grantStickerForFruitCompletion(dto.getChildId(), dto.getFruitId());
        }
    }

    //================== 내부 유틸 메서드 ==================
    /**
     * 성공 판정 (3개 이상 정답 시 통과)
     */
    private boolean determineSuccess(int correctCount, int totalQuestions) {
        return correctCount >= 3;
    }


    /**
     * 다음 열매 활성화 처리(자녀별)
     * - Child_Fruit_Status 기준으로 다음 열매를 활성화
     * - 현재 열매의 sequence_in_stage + 1 항목을 찾아
     *   * 없으면 Child_Fruit_Status INSERT(is_active=TRUE, opened_at=NOW)
     *   * 있으면 is_active=TRUE 업데이트
     * - 마지막 열매인 경우: 다음 Stage의 첫 번째 열매 활성화 시도
     */
    private void activateNextFruitForChild(String childId, LearningFruit currentFruit) { //changed
        try {
            String stageId = currentFruit.getStageId();
            int nextSeq = currentFruit.getSequenceInStage() + 1;

            // ✅ 듣기 게임(ST007~ST009)에서는 ST009 이후는 멈춤
            if ("ST009".equals(stageId)) {
                log.info("[ListeningGame][activate] ST009(듣기게임 마지막) 이후 단계 없음 — 다음 Stage 활성화 중단");
                return;
            }

            Optional<LearningFruit> nextOpt = learningFruitRepository.findByStageIdAndSequenceInStage(stageId, nextSeq);
            if (nextOpt.isPresent()) {
                LearningFruit next = nextOpt.get();
                upsertChildFruitActive(childId, next.getFruitId()); // 👈 여기서 INSERT 또는 UPDATE 수행
                log.info("[ListeningGame][activate] 다음 열매 활성화 완료(child별) childId={} nextFruitId={}", childId, next.getFruitId()); //changed
            } else {
                // 다음 열매가 없으면 다음 Stage의 첫 열매(SEQ=1) 활성화 시도 //changed
                String nextStageId = tryIncrementStageId(stageId); //changed
                if (nextStageId != null) { //changed
                    Optional<LearningFruit> nextStageFirst = learningFruitRepository.findByStageIdAndSequenceInStage(nextStageId, 1);
                    if (nextStageFirst.isPresent()) {
                        upsertChildFruitActive(childId, nextStageFirst.get().getFruitId()); //changed
                        log.info("[ListeningGame][activate] 다음 Stage 첫 열매 활성화(child별) childId={} nextStageId={} fruitId={}",
                                childId, nextStageId, nextStageFirst.get().getFruitId()); //changed
                    } else {
                        log.info("[ListeningGame][activate] 다음 Stage 첫 열매 없음 nextStageId={}", nextStageId); //changed
                    }
                } else {
                    log.info("[ListeningGame][activate] 다음 Stage 계산 불가 stageId={}", stageId); //changed
                }
            }
        } catch (Exception e) {
            log.warn("[ListeningGame][activate] 예외 발생(child별): {}", e.getMessage()); //changed
        }
    }

    // Child_Fruit_Status upsert helper (INSERT or UPDATE TRUE) //changed
    private void upsertChildFruitActive(String childId, String fruitId) { //changed
        Optional<ChildFruitStatus> cur = childFruitStatusRepository.findByChildIdAndFruitId(childId, fruitId);
        if (cur.isPresent()) {
            if (!cur.get().isActive()) {
                childFruitStatusRepository.activateFruit(childId, fruitId); // UPDATE is_active=TRUE
            }
        } else {
            ChildFruitStatus entity = ChildFruitStatus.builder()
                    .id(new ChildFruitStatusId(childId, fruitId))
                    .isActive(true)
                    .openedAt(LocalDateTime.now())
                    .build();
            childFruitStatusRepository.save(entity);   // INSERT (새 행 생성)
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

    // 👇👇👇 [추가] StudyCompletionService에서 복사해 온 헬퍼 메서드 👇👇👇
    /**
     * 게임 완료 시 ChildProgress 테이블을 업데이트하는 메서드
     * @param childId 완료한 자녀 ID
     * @param category 완료한 카테고리 (Category Enum 타입)
     * @param completedFruitId 완료한 열매 ID
     */
    private void updateChildProgress(String childId, Category category, String completedFruitId) {
        log.debug("[ListeningGameService] ChildProgress 업데이트 시작: childId={}, category={}, completedFruitId={}",
                childId, category, completedFruitId);

        // 1. 완료된 열매 정보 조회 (비교를 위해 필요)
        LearningFruit completedFruit = learningFruitRepository.findById(completedFruitId)
                .orElseThrow(() -> {
                    log.error("[ListeningGameService] ChildProgress 업데이트 실패: 완료된 열매 정보 없음 (fruitId={})", completedFruitId);
                    return new RuntimeException("Fruit not found: " + completedFruitId);
                });

        // 2. 기존 ChildProgress 정보 조회 또는 새로 생성
        ChildProgressId progressId = new ChildProgressId(childId, category); // 복합키 생성
        ChildProgress progress = childProgressRepository.findById(progressId)
                .orElseGet(() -> { // 없으면 새로 만들기
                    log.debug("[ListeningGameService] ChildProgress 없음. 새로 생성: childId={}, category={}", childId, category);
                    Child childRef = childRepository.getReferenceById(childId);
                    return ChildProgress.builder()
                            .childId(childId)     // ID 클래스 필드 직접 설정
                            .category(category) // ID 클래스 필드 직접 설정
                            .child(childRef)
                            .build();
                });

        // 3. last_fruit_id 업데이트
        progress.setLastFruitId(completedFruitId);

        // 4. best_fruit_id 업데이트 (필요 시)
        String currentBestFruitId = progress.getBestFruitId();
        boolean updateBest = false;

        if (currentBestFruitId == null) {
            updateBest = true;
            log.debug("[ListeningGameService] 기존 BestFruit 없음. 업데이트 필요.");
        } else {
            LearningFruit currentBestFruit = learningFruitRepository.findById(currentBestFruitId)
                    .orElse(null);

            if (currentBestFruit == null) {
                updateBest = true;
                log.warn("[ListeningGameService] 기존 BestFruit ID({})에 해당하는 열매 정보 없음. 업데이트 필요.", currentBestFruitId);
            } else {
                int stageCompare = completedFruit.getStageId().compareTo(currentBestFruit.getStageId());
                if (stageCompare > 0) {
                    updateBest = true;
                } else if (stageCompare == 0 && completedFruit.getSequenceInStage() > currentBestFruit.getSequenceInStage()) {
                    updateBest = true;
                }
                log.debug("[ListeningGameService] BestFruit 비교: completed(stage={}, seq={}) vs currentBest(stage={}, seq={}). UpdateNeeded={}",
                        completedFruit.getStageId(), completedFruit.getSequenceInStage(),
                        currentBestFruit.getStageId(), currentBestFruit.getSequenceInStage(), updateBest);
            }
        }

        if (updateBest) {
            progress.setBestFruitId(completedFruitId);
            log.info("[ListeningGameService] BestFruit 업데이트: childId={}, category={}, newBestFruitId={}",
                    childId, category, completedFruitId);
        }

        // 5. ChildProgress 저장 (INSERT or UPDATE)
        childProgressRepository.save(progress);
        log.info("[ListeningGameService] ChildProgress 업데이트 완료: childId={}, category={}", childId, category);
    }
}