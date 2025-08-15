package com.sinabro.backend.user.parent.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterDto {
    private String userId;
    private String userEmail;
    private String userPw;  // ⬅️ 소셜에선 null 로 들어올 수 있음
    private String userName;
    private String userPhoneNum; // ⬅️ 추가정보 입력 페이지에서 받음(없으면 null)
    private String userLanguage;  // 기본값은 서비스에서 "Korea"로 처리
    private String role; // "parent"
    private String socialType; // "kakao" | "google" | "local"
    private String socialId;
}
