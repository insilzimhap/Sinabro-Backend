package com.sinabro.backend.admin.user.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AdminUserDetailDto {

    private ParentDto parent;                 // 상단 카드
    private List<AdminChildRowDto> children;  // 하단 자녀 테이블

    @Getter
    @AllArgsConstructor
    public static class ParentDto {
        private String id;        // user_id
        private String email;     // user_email
        private String name;      // user_name
        private String password;  // user_pw  (표시는 지양, 재설정 권장)
    }

    @Getter
    @AllArgsConstructor
    public static class AdminChildRowDto {
        private String childId;   // child_id
        private String name;      // child_name
        private Integer age;      // child_age
        private Integer level;    // child_level
    }
}