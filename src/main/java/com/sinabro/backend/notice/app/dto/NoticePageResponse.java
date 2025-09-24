package com.sinabro.backend.notice.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * [공통] 페이지네이션 응답 래퍼
 * - Flutter에서 무한 스크롤/페이지 단위로 소비하기 쉽게 메타 정보 포함
 * - content에는 리스트 DTO(예: NoticeListItemDto)를 담아서 반환
 */
@Getter
@AllArgsConstructor
@Builder
public class NoticePageResponse<T> {

    private List<T> content;   // 실제 데이터(예: NoticeListItemDto 리스트)

    private int page;          // 현재 페이지 (0-base)

    private int size;          // 페이지 크기

    private long totalElements; // 전체 개수

    private int totalPages;    // 전체 페이지 수

    private boolean hasNext;   // 다음 페이지 존재 여부
}
