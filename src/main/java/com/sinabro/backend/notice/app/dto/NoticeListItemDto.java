package com.sinabro.backend.notice.app.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * [모바일 앱 > 공지사항] 리스트 아이템 DTO
 * - 부모 메인 첫 화면(공지 목록)에서 사용
 * - 본문(content)은 포함하지 않음 → 드롭다운 상세에서 별도 요청
 *
 * 매핑 가이드(Notice 엔티티)
 *  - id         ← notice_id (PK)
 *  - title      ← title
 *  - author     ← (엔티티에 없음) → 서비스에서 고정값/설정값(ex. "팀 시나브로")으로 세팅
 *  - createdAt  ← notice_created_date
 *  - viewCount  ← view_count (없으면 0)
 *  - urgent     ← (notice_type == "긴급") 여부
 */
@Getter
@AllArgsConstructor
@Builder
public class NoticeListItemDto {

    private Long id;                 // 공지 ID

    private String title;            // 제목

    private String author;           // 작성자/팀명 (예: "팀 시나브로")

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime createdAt; // 작성일 (KST 포맷)

    private Long viewCount;          // 조회수

    private boolean urgent;          // 긴급 공지 여부 (notice_type == "긴급")
}
