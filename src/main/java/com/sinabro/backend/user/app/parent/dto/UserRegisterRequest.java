package com.sinabro.backend.user.app.parent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserRegisterRequest {
    @NotBlank private String userId;
    @NotBlank private String userEmail;

    @NotBlank
    @Size(min = 8, max = 16, message = "비밀번호는 8~16자여야 합니다.")
    private String userPw;

    @NotBlank
    @Size(min = 8, max = 16, message = "비밀번호 확인은 8~16자여야 합니다.")
    private String confirmPw;

    private String userName;
    private String userPhoneNum;
    private String userLanguage; // 기본값은 서비스/엔티티에서 Korea
    private String role;         // parent
    private String socialType;   // local

    /** ✅ 회원가입 화면의 수신동의/개인정보동의 묶음 */
    private ParentSettingSignupRequestDto settings;
}