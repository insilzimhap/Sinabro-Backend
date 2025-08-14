package com.sinabro.backend.admin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class AdminChildDto {
    private String parentUserId;  // 부모 고유 ID (user_id)
    private String childId;       // 자녀 고유 ID
    private String name;          // 자녀 이름
    private Integer age;          // 자녀 나이
    private String password;      // 자녀 비밀번호
    private Integer level;        // 자녀 레벨
}
