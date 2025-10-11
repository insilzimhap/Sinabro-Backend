package com.sinabro.backend.inquiry.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * [문의 등록 요청 DTO]
 * - PathVariable로 parentUserId 전달, 본문에는 제목/내용만
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class InquiryCreateRequestDto {
    @NotBlank
    @Size(max=255)
    private String title;

    @NotBlank
    private String content;
}
