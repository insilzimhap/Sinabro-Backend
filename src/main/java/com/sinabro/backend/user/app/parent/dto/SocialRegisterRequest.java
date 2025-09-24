package com.sinabro.backend.user.app.parent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SocialRegisterRequest {

    // 기본 사용자 정보
    @NotBlank private String userId;
    @NotBlank private String userEmail;
    private String userName;
    private String userPhoneNum;

    // 언어/역할/소셜 식별
    private String userLanguage;        // null/빈값이면 서비스에서 "Korea" 보정
    private String role;                // 기본 parent
    @NotBlank private String socialType; // "kakao" | "google"
    @NotBlank private String socialId;

    // ⬇️ 소셜 추가정보: 이번에 새로 받는 비밀번호 + 재입력
    @Size(min = 8, max = 16)
    private String newPassword;

    @Size(min = 8, max = 16)
    private String confirmPw;

    // 설정(동의/수신/수신 등)
    @Valid
    private ParentSettingSignupRequestDto settings;
}
