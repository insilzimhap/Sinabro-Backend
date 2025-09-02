package com.sinabro.backend.mypage.child.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * [자녀 계정 삭제 요청 DTO]
 * - 부모 비밀번호로 보호된 삭제 흐름
 * - childId는 보통 PathVariable로 넘기고, 본문에는 부모 비번만 받는 형태 권장
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChildDeleteRequestDto {
    @NotBlank
    private String parentPassword;
}
