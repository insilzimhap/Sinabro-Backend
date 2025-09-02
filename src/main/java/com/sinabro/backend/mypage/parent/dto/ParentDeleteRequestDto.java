package com.sinabro.backend.mypage.parent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParentDeleteRequestDto {
    /** 탈퇴 확인용 비밀번호 */
    @NotBlank
    private String currentPassword;
}
