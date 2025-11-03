package com.sinabro.backend.stage.service;

import com.sinabro.backend.stage.entity.ChildFruitStatus;
import com.sinabro.backend.stage.entity.ChildFruitStatusId;
import com.sinabro.backend.stage.entity.LearningFruit;
import com.sinabro.backend.stage.entity.Stage;
import com.sinabro.backend.stage.repository.ChildFruitStatusRepository;
import com.sinabro.backend.stage.repository.LearningFruitRepository;
import com.sinabro.backend.stage.repository.StageRepository; // ✅ StageRepository 주입 추가
import com.sinabro.backend.user.entity.Child;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 🌱 FruitActivationService
 * - 자녀의 레벨에 따라 열매를 활성화하는 로직을 전담하는 서비스
 */
@Service
@RequiredArgsConstructor
public class FruitActivationService {

    private final LearningFruitRepository learningFruitRepository;
    private final ChildFruitStatusRepository childFruitStatusRepository;
    private final StageRepository stageRepository; // ✅ Stage 정보 조회를 위해 주입

    /**
     * 레벨테스트 직후, 자녀의 레벨에 맞춰 모든 열매의 초기 상태를 설정하고 DB에 저장합니다.
     * @param child 대상 자녀 엔티티
     * @param assignedLevel 부여받은 레벨
     */
    @Transactional
    public void activateFruitsForLevel(Child child, int assignedLevel) {
        // 1. 모든 열매와 모든 스테이지 정보를 미리 가져와서 Map으로 만들어두면 효율적이야.
        List<LearningFruit> allFruits = learningFruitRepository.findAll();
        Map<String, Stage> stageMap = stageRepository.findAll().stream()
                .collect(Collectors.toMap(Stage::getStageId, Function.identity()));

        List<ChildFruitStatus> statusesToSave = new ArrayList<>();

        // 2. 모든 열매를 순회하며, 각 열매의 실제 레벨을 기준으로 활성화 여부를 결정한다.
        for (LearningFruit fruit : allFruits) {
            boolean shouldBeActive = false;

            // 3. 열매가 속한 Stage 정보를 Map에서 찾는다.
            Stage fruitStage = stageMap.get(fruit.getStageId());
            if (fruitStage == null) continue; // 혹시 모를 null 방지

            // 4. Stage의 레벨('초급' 등)을 숫자(1 등)로 변환
            int fruitStageLevel = getLevelAsInt(fruitStage.getLevel());
            int sequence = fruit.getSequenceInStage();

            // 5. 네가 요청한 활성화 로직 (이제 모든 카테고리에 정확하게 적용됨!)
            if (assignedLevel == 1) {
                if (fruitStageLevel == 1 && sequence == 1) {
                    shouldBeActive = true;
                }
            } else if (assignedLevel == 2) {
                if (fruitStageLevel == 1 || (fruitStageLevel == 2 && sequence == 1)) {
                    shouldBeActive = true;
                }
            } else if (assignedLevel == 3) {
                if (fruitStageLevel <= 2 || (fruitStageLevel == 3 && sequence == 1)) {
                    shouldBeActive = true;
                }
            }

            // 6. 자녀-열매 상태 객체 생성
            ChildFruitStatusId statusId = new ChildFruitStatusId(child.getChildId(), fruit.getFruitId());
            ChildFruitStatus status = ChildFruitStatus.builder()
                    .id(statusId)
                    .child(child)
                    .learningFruit(fruit)
                    .isActive(shouldBeActive)
                    .build();

            statusesToSave.add(status);
        }

        // 7. 생성된 모든 상태 정보를 DB에 한 번에 저장
        childFruitStatusRepository.saveAll(statusesToSave);
    }

    /**
     * '초급', '중급', '고급' 문자열을 숫자 1, 2, 3으로 변환하는 헬퍼 메서드
     */
    private int getLevelAsInt(String level) {
        switch (level) {
            case "초급":
                return 1;
            case "중급":
                return 2;
            case "고급":
                return 3;
            default:
                return 0; // 예외 처리
        }
    }
}