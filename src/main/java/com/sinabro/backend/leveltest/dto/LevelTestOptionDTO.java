package com.sinabro.backend.leveltest.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "레벨테스트 보기 DTO")
@Data
@AllArgsConstructor
public class LevelTestOptionDTO {

    @Schema(description = "보기 ID", example = "1")
    private Long id;

    @Schema(description = "보기 TEXT", example = "사과")
    private String optionText;

    @Schema(description = "문제 IMG", example = "사과 img")
    private String imageUrl;

    @Schema(description = "보기의 정답 여부", example = "1 or 0")
    private boolean isCorrect;
}
