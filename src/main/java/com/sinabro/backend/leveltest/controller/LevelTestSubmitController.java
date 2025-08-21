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

// ── Swagger OpenAPI ────────────────────────────────────────────────────────────
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
// ──────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/level-test")
@RequiredArgsConstructor
@Tag(name = "Level Test - Submit", description = "레벨 테스트 제출/채점/레벨 산정 API")
public class LevelTestSubmitController {

    private final LevelTestChoiceRepository choiceRepo;
    private final LevelTestQuestionRepository questionRepo;
    private final LevelTestOptionRepository optionRepo;
    private final ChildRepository childRepo;

    // 🟤 자녀의 레벨 테스트 답안을 저장하고, 점수를 계산해서 레벨을 업데이트함
    @PostMapping("/submit")
    @Operation(
            summary = "레벨 테스트 제출",
            description = """
                    자녀의 모든 문항 선택을 제출하면 서버가 각 보기의 정답 여부를 **서버 기준으로 판정**하고 점수를 계산.
                    총점(이진 채점, 7문항 가정)에 따라 **최종 레벨(1/2/3)** 을 산정한 뒤 `child.child_level`(INT)로 저장.
                    
                    ● 레벨 산정 규칙  
                    - 총점 0~2 → L1(=1)  
                    - 총점 3~5 → L2(=2)  
                    - 총점 6~7 → L3(=3)
                   
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "제출/채점 성공",
                    content = @Content(schema = @Schema(implementation = SubmitResultDoc.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청(유효하지 않은 childId / questionId / optionId 등)",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<?> submitAnswers(
            @Parameter(
                    description = "자녀 ID (PK). 예: `rami`",
                    required = true,
                    example = "rami"
            )
            @RequestParam("childId") String childId,                    // 자녀 ID (쿼리 파라미터로 전달)

            @RequestBody(
                    description = "자녀가 제출한 문항 선택 리스트",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LevelTestChoiceDTO.class)))
            )
            @org.springframework.web.bind.annotation.RequestBody
            List<LevelTestChoiceDTO> choices              // 자녀가 고른 모든 문항 리스트
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
        result.put("finalLevel", finalLevelInt);         // 🔢 1/2/3
        result.put("finalLevelLabel", finalLevelLabel);  // 🔤 "L1"/"L2"/"L3"

        return ResponseEntity.ok(result);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 🔎 Swagger 문서 전용 DTO (런타임 사용 X)
    @Schema(name = "LevelTestSubmitResult",
            description = "레벨 테스트 제출 결과 응답 스키마")
    static class SubmitResultDoc {

        @Schema(description = "결과 메시지", example = "답안 저장 완료! 자녀 레벨: L2")
        public String message;

        @Schema(description = "총점", example = "5")
        public Integer totalScore;

        @Schema(description = "레벨별 정답 개수 집계", example = "{\"L1\":2, \"L2\":2, \"L3\":1}")
        public Map<String, Integer> byLevel;

        @Schema(description = "최종 레벨(정수형: 1=L1, 2=L2, 3=L3)", example = "2")
        public Integer finalLevel;

        @Schema(description = "최종 레벨 라벨", example = "L2")
        public String finalLevelLabel;
    }
}
