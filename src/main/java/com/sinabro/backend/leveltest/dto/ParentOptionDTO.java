package com.sinabro.backend.leveltest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "부모 문항 보기 DTO")
@Data
@AllArgsConstructor
public class ParentOptionDTO {

    @Schema(description = "부모문제 보기 ID", example = "1")
    private Long id;

    @Schema(description = "부모문제 보기 TEXT", example = "1. 대부분 한국어")
    private String optionText;
}
