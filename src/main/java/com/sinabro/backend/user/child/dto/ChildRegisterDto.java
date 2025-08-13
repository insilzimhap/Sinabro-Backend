package com.sinabro.backend.user.child.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChildRegisterDto {
    private String childId;
    private String childName;
    private String childNickname; // ✅ 컬럼명 변경 반영
    private String childBirth;
    private Integer childAge;
    private String childPw;
    private Integer childLevel; // ✅ String → Integer 타입 변경
    private Integer timeLimitMinutes;
    private String role;
    private String userId; // 부모 ID (FK)
}
