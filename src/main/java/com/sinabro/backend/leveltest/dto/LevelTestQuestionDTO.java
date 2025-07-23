package com.sinabro.backend.leveltest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class LevelTestQuestionDTO {
    private Long id;
    private int level;
    private String type;
    private String prompt;
    private String questionImageUrl;  // 문제 본문에 띄울 이미지 (예: "가.png")
    private String audioUrl;
    private List<LevelTestOptionDTO> options;
}
