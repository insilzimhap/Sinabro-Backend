package com.sinabro.backend.mypage.child.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

/**
 * [자녀 프로필 수정 요청 DTO]
 * - 부분 업데이트: 필드가 null이면 해당 항목은 변경하지 않음
 * - 닉네임/생일은 값이 오면 형식 검증
 * - 비밀번호는 두 칸 모두 비워두면 변경하지 않음
 *   (둘 중 하나라도 채우면 길이/일치 여부를 서비스단에서 검증)
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChildUpdateRequestDto {

    /** 닉네임 (선택) */
    @Size(min = 1, max = 20, message = "닉네임은 1~20자 이내여야 합니다.")
    private String childNickname;

    // "yyyy-MM-dd" 포맷만 허용 (예: 2018-01-01)
    @Pattern(
            regexp = "^\\d{4}-\\d{2}-\\d{2}$",
            message = "생년월일은 yyyy-MM-dd 형식이어야 합니다."
    )
    private String childBirth;

    /** 새 비밀번호 (선택) */
    @Size(min = 8, max = 16)
    private String newPassword;

    /** 새 비밀번호 재입력 (선택) */
    @Size(min = 8, max = 16)
    private String newPasswordConfirm;

    /** 제한 시간 (분 단위, 선택) */
    @Min(0)
    @Max(90)  // 예: 최대 1시간 반 제한
    private Integer timeLimitMinutes;
}
