package com.sinabro.backend.leveltest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Schema(description = "부모 문항 선택 DTO")

@NoArgsConstructor
@AllArgsConstructor
public class ParentChoiceDTO {

    @Schema(description = "부모문제 ID", example = "1")
    private Long questionId;  // 체크한 질문 ID

    @Schema(description = "부모가 선택한 보기 ID", example = "1")
    private Long optionId;    // 선택한 보기 ID
}
