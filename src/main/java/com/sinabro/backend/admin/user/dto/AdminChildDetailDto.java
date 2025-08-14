package com.sinabro.backend.admin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminChildDetailDto {
    private String childId;   // child_id
    private String name;      // child_name
    private Integer age;      // child_age
    private Integer level;    // child_level
    private String password;  // child_pw
}