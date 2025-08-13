package com.sinabro.backend.leveltest.controller;

import com.sinabro.backend.user.child.entity.Child;
import com.sinabro.backend.leveltest.dto.LevelTestChoiceDTO;
import com.sinabro.backend.leveltest.entity.LevelTestChoice;
import com.sinabro.backend.leveltest.entity.LevelTestOption;
import com.sinabro.backend.leveltest.entity.LevelTestQuestion;
import com.sinabro.backend.leveltest.repository.LevelTestChoiceRepository;
import com.sinabro.backend.leveltest.repository.LevelTestOptionRepository;
import com.sinabro.backend.leveltest.repository.LevelTestQuestionRepository;
import com.sinabro.backend.user.child.repository.ChildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/level-test")
@RequiredArgsConstructor
public class LevelTestSubmitController {

    private final LevelTestChoiceRepository choiceRepo;
    private final LevelTestQuestionRepository questionRepo;
    private final LevelTestOptionRepository optionRepo;
    private final ChildRepository childRepo;

    // 🟤 자녀의 레벨 테스트 답안을 저장하고, 점수를 계산해서 레벨을 업데이트함
    @PostMapping("/submit")
    public ResponseEntity<?> submitAnswers(
            @RequestParam("childId") String childId,                    // 자녀 ID (쿼리 파라미터로 전달)
            @RequestBody List<LevelTestChoiceDTO> choices              // 자녀가 고른 모든 문항 리스트
    ) {
        int totalScore = 0;

        // 레벨별 맞은 개수 집계 (Binary 채점)
        int l1Correct = 0;
        int l2Correct = 0;
        int l3Correct = 0;

        for (LevelTestChoiceDTO dto : choices) {
            // 🟤 각 질문과 선택지를 DB에서 조회
            LevelTestQuestion question = questionRepo.findById(dto.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException("문제 ID가 유효하지 않습니다."));
            LevelTestOption option = optionRepo.findById(dto.getOptionId())
                    .orElseThrow(() -> new IllegalArgumentException("옵션 ID가 유효하지 않습니다."));

            // 🟤 답안 엔티티 생성 (정답 여부는 서버에서 판정)
            boolean isCorrect = option.isCorrect();

            LevelTestChoice answer = new LevelTestChoice();
            answer.setChildId(childId);
            answer.setQuestion(question);
            answer.setOption(option);
            answer.setCorrect(isCorrect);
            answer.setSelectedAt(LocalDateTime.now());

            choiceRepo.save(answer); // 저장

            if (isCorrect) {
                totalScore += 1; // 정답일 경우 점수 +1

                // 레벨별 집계
                int qLevel = question.getLevel();
                if (qLevel == 1) l1Correct += 1;
                else if (qLevel == 2) l2Correct += 1;
                else if (qLevel == 3) l3Correct += 1;
            }
        }

        // 🟤 사진 기준(총 7문항, Binary)으로 최종 레벨 산정
        // L1: 0~2점, L2: 3~5점, L3: 6~7점
        int finalLevelInt;
        if (totalScore <= 2) finalLevelInt = 1;
        else if (totalScore <= 5) finalLevelInt = 2;
        else finalLevelInt = 3;

        String finalLevelLabel = "L" + finalLevelInt; // 호환용 라벨

        // 🟤 자녀 레벨 업데이트 (정수로 저장)
        Child child = childRepo.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("해당 자녀를 찾을 수 없습니다."));
        child.setChildLevel(finalLevelInt); // ✅ ERD: INT
        childRepo.save(child);

        // 🟤 결과 반환 (디버깅/확인용 세부 집계 포함)
        Map<String, Object> result = new HashMap<>();
        result.put("message", "답안 저장 완료! 자녀 레벨: " + finalLevelLabel);
        result.put("totalScore", totalScore);
        result.put("byLevel", Map.of(
                "L1", l1Correct,   // L1 최대 3
                "L2", l2Correct,   // L2 최대 2
                "L3", l3Correct    // L3 최대 2
        ));
        // 호환성을 위해 둘 다 제공
        result.put("finalLevel", finalLevelInt);     // 🔢 1/2/3
        result.put("finalLevelLabel", finalLevelLabel); // 🔤 "L1"/"L2"/"L3"

        return ResponseEntity.ok(result);
    }
}
