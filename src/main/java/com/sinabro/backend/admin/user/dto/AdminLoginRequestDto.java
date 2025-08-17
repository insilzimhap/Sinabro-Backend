package com.sinabro.backend.admin.user.dto;

import lombok.*;

// 관리자 로그인 요청 DTO (세션 생성용)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginRequestDto {
    // 로그인 ID = users.user_id
    private String userId;   // 필수
    private String password; // 필수 (BCrypt 비교)
}
