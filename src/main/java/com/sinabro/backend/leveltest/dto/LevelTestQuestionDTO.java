package com.sinabro.backend.leveltest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Schema(description = "레벨테스트 문제 DTO")

@Data
@AllArgsConstructor
public class LevelTestQuestionDTO {

    @Schema(description = "문제의 ID", example = "1")
    private Long id;

    @Schema(description = "문제의 LEVEL", example = "1~3")
    private int level;

    @Schema(description = "문제의 TYPE", example = "이야기 듣고 고르기")
    private String type;

    @Schema(description = "문제 설명(내용)", example = "아래 그림 중에서 '사과'를 골라보세요")
    private String prompt;

    @Schema(description = "문제의 Img URL", example = "사과 img")
    private String questionImageUrl;  // 문제 본문에 띄울 이미지 (예: "가.png")

    @Schema(description = "문제의 Audio URL", example = "이름 audio")
    private String audioUrl;

    private List<LevelTestOptionDTO> options;
}
