package com.sinabro.backend.leveltest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Schema(description = "부모 문항 DTO")
@Data
@AllArgsConstructor
public class ParentQuestionDTO {

    @Schema(description = "부모문제 ID", example = "1")
    private Long id;

    @Schema(description = "문항 순서", example = "3")
    private Integer questionOrder; // 🔥 정렬/매핑용 (Flutter에서도 사용)

    @Schema(description = "부모문제 TEXT", example = "평소 자녀와 주로 어떤 언어로 대화하시나요?")
    private String questionText;

    private List<ParentOptionDTO> options;
}
