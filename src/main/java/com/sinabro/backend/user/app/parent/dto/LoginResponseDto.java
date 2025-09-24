package com.sinabro.backend.user.app.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * [로그인 응답 DTO]
 * - 로그인 성공 시 사용자 정보 + JWT 토큰을 함께 반환
 * - user: 로그인된 사용자 정보(UserRegisterDto)
 * - token: 인증용 JWT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {
    private UserRegisterDto user; // 사용자 정보
    private String token;         // JWT 토큰
}

