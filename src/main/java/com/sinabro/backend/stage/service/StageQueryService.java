package com.sinabro.backend.stage.service;

import com.sinabro.backend.stage.dto.FruitStatusDto;
import com.sinabro.backend.stage.dto.StageWithFruitsDto;
import com.sinabro.backend.stage.entity.ChildFruitStatus;
import com.sinabro.backend.stage.repository.ChildFruitStatusRepository;
import com.sinabro.backend.stage.repository.StageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 🌳 StageQueryService
 * - 자녀별 전체 스테이지(나무)와 열매의 활성 상태를 조회하는 서비스
 * - 프론트엔드의 '월드맵' 또는 '스테이지 선택' 화면 구성을 위한 데이터를 제공함
 * - 각 열매의 활성 여부(isActive)를 포함하여 반환함
 */
@Service
@RequiredArgsConstructor
public class StageQueryService {

    private final StageRepository stageRepository;
    private final ChildFruitStatusRepository childFruitStatusRepository;

    /**
     * 특정 자녀의 모든 스테이지 및 열매 상태를 조회하여 DTO 리스트로 반환합니다.
     * @param childId 조회할 자녀의 ID
     * @return 스테이지별로 그룹화된 열매 상태 정보 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<StageWithFruitsDto> getStageStatusForChild(String childId) {
        // 1. 특정 자녀의 모든 열매 상태 정보를 DB에서 가져온다.
        List<ChildFruitStatus> statuses = childFruitStatusRepository.findById_ChildId(childId);

        // 2. 가져온 상태 정보를 Stage(나무) ID 별로 그룹핑한다.
        return statuses.stream()
                .collect(Collectors.groupingBy(status -> status.getLearningFruit().getStageId()))
                .entrySet().stream()
                .map(entry -> {
                    String stageId = entry.getKey();
                    List<ChildFruitStatus> fruitsInStage = entry.getValue();

                    // 3. 각 Stage(나무)에 속한 열매들을 DTO(FruitStatusDto)로 변환한다.
                    List<FruitStatusDto> fruitDtos = fruitsInStage.stream()
                            .map(status -> new FruitStatusDto(
                                    status.getId().getFruitId(),
                                    status.getLearningFruit().getTitle(),
                                    status.getLearningFruit().getSequenceInStage(),
                                    status.isActive()
                            ))
                            .sorted(Comparator.comparingInt(FruitStatusDto::getSequenceInStage))
                            .collect(Collectors.toList());

                    // 4. Stage 정보와 가공된 열매 DTO 리스트를 합쳐 최종 DTO(StageWithFruitsDto)를 만든다.
                    String stageLevel = stageRepository.findById(stageId).get().getLevel();
                    return new StageWithFruitsDto(stageId, stageLevel, fruitDtos);
                })
                .sorted(Comparator.comparing(StageWithFruitsDto::getStageId)) // stageId 순서로 정렬
                .collect(Collectors.toList());
    }
}