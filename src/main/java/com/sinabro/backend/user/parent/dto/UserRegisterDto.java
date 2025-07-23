package com.sinabro.backend.user.parent.dto;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "회원가입 요청 DTO")
public class UserRegisterDto {

    @Schema(description = "사용자 ID (일반 회원가입 ID)", example = "dongdong123")
    private String userId;

    @Schema(description = "사용자 이메일", example = "dong@dong.com")
    private String userEmail;

    @Schema(description = "사용자 비밀번호", example = "securePw123!")
    private String userPw;

    @Schema(description = "사용자 이름", example = "동동이")
    private String userName;

    @Schema(description = "전화번호", example = "01012345678")
    private String userPhoneNum;

    @Schema(description = "사용 언어", example = "Korean")
    private String userLanguage;

    @Schema(description = "사용자 역할 (ex: USER)", example = "USER")
    private String role;

    @Schema(description = "소셜 타입 (local, kakao, google 등)", example = "local")
    private String socialType;

    @Schema(description = "소셜 식별자", example = "kakao-123456")
    private String socialId;
}
