package com.sinabro.backend.mypage.parent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParentDeleteRequestDto {
    /** 탈퇴 확인용 비밀번호 */
    @NotBlank(message = "현재 비밀번호를 입력하세요.")
    private String currentPassword;
}
