package com.sinabro.backend.leveltest.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LevelTestChoiceDTO {
    private Long questionId;   // 문제 ID
    private Long optionId;     // 선택한 보기 ID
    private boolean isCorrect; // 정답 여부
}
