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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
     */
    public ChildStageStatusResponseDto getChildStageStatusAll(String childId) { //changed
        log.info("[ChildStageStatusService] getChildStageStatusAll 호출 childId={}", childId);

        // 1️⃣ 자녀 존재 검증
        childRepository.findById(childId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "자녀를 찾을 수 없습니다."));

        // 2️⃣ 모든 Stage 조회
        List<Stage> stages = stageRepository.findAll();

        // 3️⃣ Stage별로 열매 + 상태 매핑
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
                .build();
    }


    /**
     * ② 현재 최고 활성 열매 + 스테이지(Level) 정보만 반환
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
        int maxStageOrder = -1;

        for (Stage stage : stages) {
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

            // ✅ Stage 내 최신 활성 열매 찾기
            Optional<ChildFruitStatusDto> latestActiveFruit = fruitStatuses.stream()
                    .filter(ChildFruitStatusDto::isActive)
                    .max(Comparator.comparing(f -> f.getOpenedAt() != null ? f.getOpenedAt() : java.time.LocalDateTime.MIN));

            // ✅ 최고 Stage/Fruit 갱신 //changed
            int currentOrder = getLevelOrder(stage.getLevel()); //changed
            if (latestActiveFruit.isPresent() && currentOrder > maxStageOrder) { //changed
                maxStageOrder = currentOrder; //changed
                highestStageId = stage.getStageId();
                highestFruitId = latestActiveFruit.get().getFruitId();
                highestLevel = stage.getLevel();
            }
        }

        log.info("[ChildStageStatusService] highestStageId={} highestFruitId={} level={}",
                highestStageId, highestFruitId, highestLevel);

        return ChildStageStatusResponseDto.builder()
                .childId(childId)
                .currentStageId(highestStageId)
                .currentFruitId(highestFruitId)
                .currentLevel(highestLevel)
                .build();
    }


    // ✅ Stage level("초급","중급","고급")을 숫자로 변환하는 헬퍼 메서드 //changed
    private int getLevelOrder(String level) { //changed
        if (level == null) return 0;
        return switch (level) { //changed
            case "초급" -> 1;
            case "중급" -> 2;
            case "고급" -> 3;
            default -> 0;
        };
    } //changed
}
