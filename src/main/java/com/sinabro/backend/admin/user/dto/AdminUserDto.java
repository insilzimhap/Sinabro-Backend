package com.sinabro.backend.admin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.sql.Timestamp;

//목록 테이블용(“부모 이름 (아이디) 이메일”)
@Getter
@AllArgsConstructor
public class AdminUserDto {
    private String id;  // 사용
    private String name;   // 사용
    private String email; // 사용
    private String phoneNumber;
    private String role;
    private Timestamp createDate;
}
