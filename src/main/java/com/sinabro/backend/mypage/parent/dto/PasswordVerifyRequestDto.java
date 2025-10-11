package com.sinabro.backend.mypage.parent.dto;

import jakarta.validation.constraints.*;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//부모 마이페이지 입장 전에 현재 비밀번호가 맞는지 서버에서 검증
public class PasswordVerifyRequestDto {
    @NotBlank(message = "현재 비밀번호를 입력하세요.")
    private String currentPassword;
}
