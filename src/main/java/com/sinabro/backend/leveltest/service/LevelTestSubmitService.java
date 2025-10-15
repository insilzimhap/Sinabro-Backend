package com.sinabro.backend.leveltest.service;

import com.sinabro.backend.leveltest.dto.LevelTestChoiceDTO;
import com.sinabro.backend.leveltest.entity.LevelTestChoice;
import com.sinabro.backend.leveltest.entity.LevelTestOption;
import com.sinabro.backend.leveltest.entity.LevelTestQuestion;
import com.sinabro.backend.leveltest.repository.LevelTestChoiceRepository;
import com.sinabro.backend.leveltest.repository.LevelTestOptionRepository;
import com.sinabro.backend.leveltest.repository.LevelTestQuestionRepository;
import com.sinabro.backend.stage.service.FruitActivationService; // 우리가 만든 열매 활성화 서비스
import com.sinabro.backend.user.entity.Child;
import com.sinabro.backend.user.repository.ChildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gemini
 * @description 레벨 테스트 제출과 관련된 모든 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
public class LevelTestSubmitService {

    // --- 의존성 주입 ---
    private final LevelTestChoiceRepository choiceRepo;
    private final LevelTestQuestionRepository questionRepo;
    private final LevelTestOptionRepository optionRepo;
    private final ChildRepository childRepo;
    private final FruitActivationService fruitActivationService; // ✅ 열매 활성화 서비스 주입!

    /**
     * 프론트엔드로부터 받은 답안을 처리하고, 채점, 레벨 부여, 초기 열매 활성화를 수행합니다.
     * @param childId 자녀 ID
     * @param choices 프론트에서 보낸 답안 목록 DTO
     * @return 채점 결과 Map
     */
    @Transactional
    public Map<String, Object> submitAndProcessAnswers(String childId, List<LevelTestChoiceDTO> choices) {

        // 1. 자녀 정보 조회
        Child child = childRepo.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("해당 자녀를 찾을 수 없습니다. childId=" + childId));

        int totalScore = 0;
        int l1Correct = 0, l2Correct = 0, l3Correct = 0;

        // 2. 답안 채점 및 저장
        for (LevelTestChoiceDTO dto : choices) {
            LevelTestOption option = optionRepo.findById(dto.getOptionId())
                    .orElseThrow(() -> new IllegalArgumentException("옵션 ID가 유효하지 않습니다."));

            boolean isCorrect = option.isCorrect();

            LevelTestChoice answer = LevelTestChoice.builder()
                    .childId(childId)
                    .question(option.getQuestion())
                    .option(option)
                    .isCorrect(isCorrect)
                    .selectedAt(LocalDateTime.now())
                    .build();
            choiceRepo.save(answer);

            if (isCorrect) {
                totalScore++;
                int qLevel = option.getQuestion().getLevel();
                if (qLevel == 1) l1Correct++;
                else if (qLevel == 2) l2Correct++;
                else if (qLevel == 3) l3Correct++;
            }
        }

        // 3. 점수에 따른 최종 레벨 산정
        int finalLevelInt;
        if (totalScore <= 2) finalLevelInt = 1;
        else if (totalScore <= 5) finalLevelInt = 2;
        else finalLevelInt = 3;

        // 4. 자녀 레벨 정보 업데이트
        child.setChildLevel(finalLevelInt);
        childRepo.save(child);

        // ✅ 5. (핵심) 확정된 레벨에 맞춰 초기 열매 활성화 로직 호출!
        fruitActivationService.activateFruitsForLevel(child, finalLevelInt);

        // 6. 프론트엔드에 전달할 결과 데이터 생성
        Map<String, Object> result = new HashMap<>();
        result.put("message", "답안 저장 완료! 자녀 레벨: L" + finalLevelInt);
        result.put("totalScore", totalScore);
        result.put("byLevel", Map.of("L1", l1Correct, "L2", l2Correct, "L3", l3Correct));
        result.put("finalLevel", finalLevelInt);
        result.put("finalLevelLabel", "L" + finalLevelInt);

        return result;
    }
}