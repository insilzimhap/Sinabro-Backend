package com.sinabro.backend.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChildRegisterDto {
    private String childId;
    private String childName;
    private String childNickName;
    private String childBirth;
    private Integer childAge;
    private String childPw;
    private String childLevel;
    private Integer timeLimitMinutes;
    private String role;
    private String userId; // 부모 ID (FK)
}
