package com.sinabro.backend.admin.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminChildDetailDto {
    private String childId;            // child_id
    private String name;               // child_name
    private Integer age;               // child_age   (화면에서 안 쓰면 null 가능)
    private Integer level;             // child_level

    private String password;           // ⚠️ 보안: 서비스에서 항상 null 로 내려줌
    private String nickname;           // child_nickname
    private String birth;              // child_birth
    private Integer timeLimitMinutes;  // time_limit_minutes
}