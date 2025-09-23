package com.sinabro.backend.user.admin.dto;

import lombok.*;

// 로그인 상태 조회(/api/admin/auth/me) 응답
@Getter
@Builder
@AllArgsConstructor
public class AdminMeResponseDto {
    private String userId;
    private String name;
    private String role; // "admin"
}
