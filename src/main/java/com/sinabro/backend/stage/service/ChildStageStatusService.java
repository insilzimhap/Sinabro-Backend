package com.sinabro.backend.stage.service;

import com.sinabro.backend.stage.dto.ChildFruitStatusDto;
import com.sinabro.backend.stage.dto.ChildStageStatusResponseDto;
import com.sinabro.backend.stage.entity.*;
import com.sinabro.backend.stage.repository.*;
import com.sinabro.backend.user.repository.ChildRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 🌳 ChildStageStatusService
 * - 자녀별 Stage/Fruit 활성 상태 전체 조회 서비스
 * - 현재 자녀의 가장 높은 활성 열매 + 해당 Stage(Level) 포함
 * - 듣기/쓰기/게임 공용 진행도 API
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChildStageStatusService {

    private final ChildRepository childRepository;
    private final StageRepository stageRepository;
    private final LearningFruitRepository learningFruitRepository;
    private final ChildFruitStatusRepository childFruitStatusRepository;

    /**
     * ① 전체 진행 상태 조회 (모든 Stage/Fruit)
     *   - category 필터 파라미터 지원 (null 시 전체 반환)
     */
    public ChildStageStatusResponseDto getChildStageStatusAll(String childId, String categoryFilter) { //changed
        log.info("[ChildStageStatusService] getChildStageStatusAll 호출 childId={} category={}", childId, categoryFilter);

        // 1️⃣ 자녀 존재 검증
        childRepository.findById(childId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "자녀를 찾을 수 없습니다."));

        // 2️⃣ Stage 조회 (카테고리 필터 적용)
        List<Stage> stages;
        if (categoryFilter != null && !categoryFilter.isBlank()) {
            try {
                var categoryEnum = Stage.Category.valueOf(categoryFilter);
                stages = stageRepository.findByCategory(categoryEnum);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 category 값입니다: " + categoryFilter);
            }
        } else {
            stages = stageRepository.findAll();
        }

        // 3️⃣ Stage별 열매 + 상태 매핑
        List<ChildStageStatusResponseDto.StageStatus> stageStatuses = stages.stream().map(stage -> {
            List<LearningFruit> fruits = learningFruitRepository
                    .findByStageIdOrderBySequenceInStage(stage.getStageId());

            List<ChildFruitStatusDto> fruitStatuses = fruits.stream()
                    .map(fruit -> {
                        Optional<ChildFruitStatus> statusOpt =
                                childFruitStatusRepository.findByChildIdAndFruitId(childId, fruit.getFruitId());
                        return statusOpt.map(status -> ChildFruitStatusDto.builder()
                                        .childId(childId)
                                        .fruitId(fruit.getFruitId())
                                        .isActive(status.isActive())
                                        .openedAt(status.getOpenedAt())
                                        .build())
                                .orElse(ChildFruitStatusDto.builder()
                                        .childId(childId)
                                        .fruitId(fruit.getFruitId())
                                        .isActive(false)
                                        .openedAt(null)
                                        .build());
                    })
                    .collect(Collectors.toList());

            return ChildStageStatusResponseDto.StageStatus.builder()
                    .stageId(stage.getStageId())
                    .category(stage.getCategory().name())
                    .level(stage.getLevel())
                    .fruits(fruitStatuses)
                    .build();
        }).collect(Collectors.toList());

        return ChildStageStatusResponseDto.builder()
                .childId(childId)
                .stages(stageStatuses)
                .category(categoryFilter)
                .schemaVersion(1)
                .build();
    }


    /**
     * ② 현재 최고 활성 열매 + 스테이지(Level) + 진행도 요약
     */
    public ChildStageStatusResponseDto getChildCurrentProgress(String childId) { //changed
        log.info("[ChildStageStatusService] getChildCurrentProgress 호출 childId={}", childId);

        // 1️⃣ 자녀 검증
        childRepository.findById(childId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "자녀를 찾을 수 없습니다."));

        // 2️⃣ 모든 Stage 조회
        List<Stage> stages = stageRepository.findAll();

        String highestStageId = null;
        String highestFruitId = null;
        String highestLevel = null;
        String category = null;
        int maxStageOrder = -1;
        Map<String, Integer> unlockedMap = new HashMap<>();

        for (Stage stage : stages) {
            List<LearningFruit> fruits = learningFruitRepository
                    .findByStageIdOrderBySequenceInStage(stage.getStageId());

            int activeCount = 0;
            LearningFruit highestFruitInStage = null;

            for (LearningFruit fruit : fruits) {
                Optional<ChildFruitStatus> statusOpt =
                        childFruitStatusRepository.findByChildIdAndFruitId(childId, fruit.getFruitId());
                if (statusOpt.isPresent() && statusOpt.get().isActive()) {
                    activeCount++;
                    highestFruitInStage = fruit;
                }
            }

            unlockedMap.put(stage.getStageId(), activeCount);

            // ✅ 최고 Stage 갱신
            int currentOrder = getLevelOrder(stage.getLevel());
            if (activeCount > 0 && currentOrder > maxStageOrder) {
                maxStageOrder = currentOrder;
                highestStageId = stage.getStageId();
                highestFruitId = (highestFruitInStage != null) ? highestFruitInStage.getFruitId() : null;
                highestLevel = stage.getLevel();
                category = stage.getCategory().name();
            }
        }

        // ✅ 최고 열매 sequence 정보 추출
        int highestSeq = 0;
        if (highestFruitId != null) {
            highestSeq = learningFruitRepository.findById(highestFruitId)
                    .map(LearningFruit::getSequenceInStage)
                    .orElse(0);
        }

        log.info("[ChildStageStatusService] highestStageId={} highestFruitId={} level={} seq={}",
                highestStageId, highestFruitId, highestLevel, highestSeq);

        // ✅ 최종 응답 생성
        return ChildStageStatusResponseDto.builder()
                .childId(childId)
                .currentStageId(highestStageId)
                .currentFruitId(highestFruitId)
                .currentLevel(highestLevel)
                .category(category)
                .highest(ChildStageStatusResponseDto.HighestFruit.builder()
                        .fruitId(highestFruitId)
                        .stageId(highestStageId)
                        .sequenceInStage(highestSeq)
                        .build())
                .unlockedUntilByStage(unlockedMap)
                .schemaVersion(1)
                .build();
    }

    /**
     * ③ [카테고리별] 현재 최고 활성 열매 + 진행도 요약
     */
    public ChildStageStatusResponseDto getChildCurrentProgressByCategory(String childId, String categoryFilter) {
        log.info("[ChildStageStatusService] getChildCurrentProgressByCategory 호출 childId={} category={}", childId, categoryFilter);

        // 1️⃣ 자녀 검증
        childRepository.findById(childId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "자녀를 찾을 수 없습니다."));

        // 2️⃣ category 필수 파라미터 확인
        if (categoryFilter == null || categoryFilter.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "category 파라미터는 필수입니다.");
        }

        // 3️⃣ Stage 조회 (해당 카테고리만)
        Stage.Category categoryEnum;
        try {
            categoryEnum = Stage.Category.valueOf(categoryFilter);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 category 값입니다: " + categoryFilter);
        }
        List<Stage> stages = stageRepository.findByCategory(categoryEnum);

        // 4️⃣ 열매 활성 상태 계산
        String highestStageId = null;
        String highestFruitId = null;
        String highestLevel = null;
        int maxStageOrder = -1;
        Map<String, Integer> unlockedMap = new HashMap<>();

        for (Stage stage : stages) {
            List<LearningFruit> fruits = learningFruitRepository
                    .findByStageIdOrderBySequenceInStage(stage.getStageId());

            int activeCount = 0;
            LearningFruit highestFruitInStage = null;

            for (LearningFruit fruit : fruits) {
                Optional<ChildFruitStatus> statusOpt =
                        childFruitStatusRepository.findByChildIdAndFruitId(childId, fruit.getFruitId());
                if (statusOpt.isPresent() && statusOpt.get().isActive()) {
                    activeCount++;
                    highestFruitInStage = fruit;
                }
            }

            unlockedMap.put(stage.getStageId(), activeCount);

            int currentOrder = getLevelOrder(stage.getLevel());
            if (activeCount > 0 && currentOrder > maxStageOrder) {
                maxStageOrder = currentOrder;
                highestStageId = stage.getStageId();
                highestFruitId = (highestFruitInStage != null) ? highestFruitInStage.getFruitId() : null;
                highestLevel = stage.getLevel();
            }
        }

        // 5️⃣ 최고 열매 sequence
        int highestSeq = 0;
        if (highestFruitId != null) {
            highestSeq = learningFruitRepository.findById(highestFruitId)
                    .map(LearningFruit::getSequenceInStage)
                    .orElse(0);
        }

        // 6️⃣ 최종 응답
        return ChildStageStatusResponseDto.builder()
                .childId(childId)
                .currentStageId(highestStageId)
                .currentFruitId(highestFruitId)
                .currentLevel(highestLevel)
                .category(categoryFilter)
                .highest(ChildStageStatusResponseDto.HighestFruit.builder()
                        .fruitId(highestFruitId)
                        .stageId(highestStageId)
                        .sequenceInStage(highestSeq)
                        .build())
                .unlockedUntilByStage(unlockedMap)
                .schemaVersion(1)
                .build();
    }


    // ✅ Stage level("초급","중급","고급") → 정렬용 숫자 변환
    private int getLevelOrder(String level) {
        if (level == null) return 0;
        return switch (level) {
            case "초급" -> 1;
            case "중급" -> 2;
            case "고급" -> 3;
            default -> 0;
        };
    }
}
