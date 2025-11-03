package com.sinabro.backend.inquiry.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * [문의 리스트 페이지 응답 DTO]
 * - content + 페이지 메타 정보
 */
@Getter
@AllArgsConstructor
public class InquiryListResponseDto {
    private List<InquiryListItemDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
}
