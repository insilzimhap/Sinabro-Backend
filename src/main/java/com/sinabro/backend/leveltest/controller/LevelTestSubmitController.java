package com.sinabro.backend.leveltest.controller;

import com.sinabro.backend.leveltest.dto.LevelTestChoiceDTO;
import com.sinabro.backend.leveltest.service.LevelTestSubmitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/level-test")
@RequiredArgsConstructor
@Tag(name = "Level Test - Submit", description = "레벨 테스트 제출/채점/레벨 산정 API")
public class LevelTestSubmitController {

    // ✅ 비즈니스 로직을 담당할 Service만 주입받습니다.
    private final LevelTestSubmitService levelTestSubmitService;

    /**
     * 자녀의 레벨 테스트 답안을 저장하고, 점수를 계산해서 레벨을 업데이트합니다.
     * 모든 핵심 로직은 LevelTestSubmitService에 위임됩니다.
     */
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
            @RequestParam("childId") String childId,

            @RequestBody(
                    description = "자녀가 제출한 문항 선택 리스트",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LevelTestChoiceDTO.class)))
            )
            @org.springframework.web.bind.annotation.RequestBody
            List<LevelTestChoiceDTO> choices
    ) {
        // ✅ 모든 비즈니스 로직을 Service 클래스에 위임하고, 결과만 받아서 반환합니다.
        Map<String, Object> result = levelTestSubmitService.submitAndProcessAnswers(childId, choices);

        return ResponseEntity.ok(result);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 🔎 Swagger 문서 전용 DTO (런타임 사용 X)
    // ──────────────────────────────────────────────────────────────────────────
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