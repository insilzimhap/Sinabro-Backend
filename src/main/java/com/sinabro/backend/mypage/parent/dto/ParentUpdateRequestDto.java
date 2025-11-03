package com.sinabro.backend.mypage.parent.dto;

import jakarta.validation.constraints.*;
import lombok.*;


/**
 * [부모 프로필 수정 요청 DTO]
 * - 이메일/전화번호: 프리필된 값을 항상 전송(필수). 형식 검증 적용.
 * - 비밀번호: 두 칸(newPassword/newPasswordConfirm) 모두 비우면 변경 없음.
 *            둘 중 하나라도 채우면 8~16자 길이 + 두 값 일치해야 함(서비스단에서 검증).
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentUpdateRequestDto {

    @NotBlank
    @Email(message = "이메일 형식이 올바르지 않습니다.", regexp = ".+@.+\\..+")
    private String userEmail;

    // 010-0000-0000 형식만 허용
    @NotBlank
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-0000-0000 형식이어야 합니다.")
    private String userPhoneNum;

    /** 바꾸고 싶을 때만 세팅: 값이 있으면 새 비밀번호로 교체 */
    @Size(min = 8, max = 16)
    private String newPassword;

    // 재입력 용
    @Size(min = 8, max = 16)
    private String newPasswordConfirm;

}
