package com.sinabro.backend.user.app.child.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildCheckDto {
    private String field;      // "childId"
    private String value;      // 요청값
    private boolean available; // true = 사용 가능, false = 중복
}
