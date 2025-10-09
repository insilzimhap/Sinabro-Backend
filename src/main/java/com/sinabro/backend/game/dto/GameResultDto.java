package com.sinabro.backend.game.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GameResultDto {
    private String childId;
    private String fruitId;
    private int score;
    private int totalQuestions;
    private boolean isSuccess;
    private int timeSpentSecs;
}