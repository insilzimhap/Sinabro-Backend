package com.sinabro.backend.user.parent.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterDto {
    private String userId;
    private String userEmail;
    private String userPw;
    private String userName;
    private String userPhoneNum;
    private String userLanguage;
    private String role;
    private String socialType;
    private String socialId;
}
