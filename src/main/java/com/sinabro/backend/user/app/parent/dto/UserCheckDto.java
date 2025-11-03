package com.sinabro.backend.user.app.parent.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserCheckDto {
    private String field;     // "userId"
    private String value;     // 요청값
    private boolean available; // true = 사용 가능, false = 중복
}