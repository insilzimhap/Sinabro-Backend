package com.sinabro.backend.mypage.child.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDate;

/**
 * [자녀 프로필 프리필 응답 DTO]
 * - 자녀 수정 화면 진입 시 현재값을 채워주기 위한 용도
 * - 비밀번호는 절대 포함하지 않음
 */
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChildProfileResponseDto {
    private String childId;        // Child.childId (PK)
    private String childName;      // Child.childName (표시용, 수정불가)
    private String childNickname;  // Child.childNickname (수정 가능)
    private String childBirth;      // "yyyy-MM-dd" (String) (수정 가능)
}
