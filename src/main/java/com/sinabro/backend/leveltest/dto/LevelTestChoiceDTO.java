package com.sinabro.backend.leveltest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Schema(description = "레벨테스트 문항 선택 DTO")
@NoArgsConstructor
@AllArgsConstructor
public class LevelTestChoiceDTO {

    @Schema(description = "선택한 문제의 ID", example = "1")
    private Long questionId;   // 문제 ID

    @Schema(description = "선택한 보기의 ID", example = "10")
    private Long optionId;     // 선택한 보기 ID

    @Schema(description = "선택한 보기가 정답인지 여부", example = "true")
    private boolean isCorrect; // 정답 여부
}
