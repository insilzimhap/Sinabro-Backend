package com.sinabro.backend.leveltest.controller;

import com.sinabro.backend.leveltest.dto.ParentChoiceDTO;
import com.sinabro.backend.leveltest.entity.ParentChoice;
import com.sinabro.backend.leveltest.entity.ParentOption;
import com.sinabro.backend.leveltest.entity.ParentQuestion;
import com.sinabro.backend.leveltest.repository.ParentChoiceRepository;
import com.sinabro.backend.leveltest.repository.ParentOptionRepository;
import com.sinabro.backend.leveltest.repository.ParentQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ── Swagger OpenAPI ────────────────────────────────────────────────────────────
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
// ──────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/parent-choice")
@RequiredArgsConstructor
@Tag(
        name = "Parent Choice",
        description = "부모 체크리스트 응답 저장 API"
)
public class ParentChoiceController {

    private final ParentChoiceRepository choiceRepo;
    private final ParentQuestionRepository questionRepo;
    private final ParentOptionRepository optionRepo;

    // 🟤 부모 응답 저장
    @PostMapping("/submit")
    @Operation(
            summary = "부모 체크리스트 응답 저장",
            description = """
부모 체크리스트에서 선택한 응답을 저장.

**처리 방식**
- 각 항목의 `questionId`, `optionId`를 기준으로 유효성을 검증.
- 유효하지 않은 ID가 포함될 경우 400 에러 반환.
- 검증 통과 시 `parent_choice` 테이블에 저장.
"""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "부모 응답 저장 성공",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 questionId 또는 optionId",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<?> submitParentChoices(
            @Parameter(
                    description = "자녀 ID (child.child_id)",
                    required = true,
                    example = "child001"
            )
            @RequestParam("childId") String childId,
            @RequestBody(
                    description = "부모 체크리스트 응답 목록",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ParentChoiceDTO.class)))
            )
            @org.springframework.web.bind.annotation.RequestBody List<ParentChoiceDTO> choices
    ) {
        for (ParentChoiceDTO dto : choices) {
            ParentQuestion question = questionRepo.findById(dto.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException("문항 ID가 유효하지 않음"));
            ParentOption option = optionRepo.findById(dto.getOptionId())
                    .orElseThrow(() -> new IllegalArgumentException("옵션 ID가 유효하지 않음"));

            ParentChoice choice = new ParentChoice();
            choice.setChildId(childId);
            choice.setQuestion(question);
            choice.setOption(option);

            choiceRepo.save(choice); // DB에 저장!
        }

        return ResponseEntity.ok("부모 응답 저장 완료!");
    }
}
