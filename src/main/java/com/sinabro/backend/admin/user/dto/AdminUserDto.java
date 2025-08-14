package com.sinabro.backend.admin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminUserDto {
    private String id;  // 사용
    private String name;   // 사용
    private String email; // 사용
    private String phoneNumber;
    private String role;
    private LocalDateTime createDate;
}
