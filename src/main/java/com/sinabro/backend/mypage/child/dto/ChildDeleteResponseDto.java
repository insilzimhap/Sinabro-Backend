package com.sinabro.backend.mypage.child.dto;

import lombok.*;

// 응답 전용 (검증/삭제 둘 다 이걸로 반환)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChildDeleteResponseDto {
    private String childId;
    private String childName;  // 모달/토스트에 사용할 표시 이름
    private boolean verified;  // 검증 성공 여부 (verify-delete에서 true)
    private boolean deleted;   // 실제 삭제 여부 (delete에서 true)
}
