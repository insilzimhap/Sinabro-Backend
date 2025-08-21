package com.sinabro.backend.admin.user.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;


//부모 카드 + 자녀 테이블 → 상세 화면용
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
        private String phoneNumber;  // ✅ 추가
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone="Asia/Seoul")  // 응답 포맷
        private LocalDateTime createDate;
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