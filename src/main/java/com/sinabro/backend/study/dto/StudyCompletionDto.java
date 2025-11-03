package com.sinabro.backend.study.dto;

import com.fasterxml.jackson.annotation.JsonProperty; // ⭐️⭐️⭐️ 이거 Import 해주고! ⭐️⭐️⭐️
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StudyCompletionDto {
    private String childId;
    private String fruitId;
    private int timeSpentSecs;

    // ⬇️ ⬇️ ⬇️ ⬇️ ⬇️ 여기가 핵심! ⬇️ ⬇️ ⬇️ ⬇️ ⬇️
    // JSON의 "isCompleted" 키를 이 필드에 매핑하라고 Jackson에게 명시!
    @JsonProperty("isCompleted")
    private boolean completed; // ⭐️ 필드 이름을 'is' 떼고 'completed'로 바꿔!
}