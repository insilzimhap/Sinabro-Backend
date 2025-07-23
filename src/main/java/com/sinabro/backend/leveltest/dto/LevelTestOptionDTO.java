package com.sinabro.backend.leveltest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LevelTestOptionDTO {
    private Long id;
    private String optionText;
    private String imageUrl;
    private boolean isCorrect;
}
