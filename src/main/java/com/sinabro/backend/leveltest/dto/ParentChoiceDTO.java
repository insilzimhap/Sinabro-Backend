package com.sinabro.backend.leveltest.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParentChoiceDTO {
    private Long questionId;  // 체크한 질문 ID
    private Long optionId;    // 선택한 보기 ID
}
