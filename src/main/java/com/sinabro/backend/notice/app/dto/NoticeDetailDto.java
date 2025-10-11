package com.sinabro.backend.notice.app.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * [모바일 앱 > 공지사항] 상세(드롭다운) DTO
 * - 리스트 아이템을 탭했을 때 본문을 내려줄 용도
 * - 서비스에서 increaseView=true일 경우 조회수 +1 처리 후 반환
 *
 * 매핑 가이드(Notice 엔티티)
 *  - id         ← notice_id (PK)
 *  - title      ← title
 *  - content    ← content (TEXT)
 *  - author     ← (엔티티에 없음) → 서비스에서 고정값/설정값으로 세팅
 *  - createdAt  ← notice_created_date
 *  - viewCount  ← view_count
 *  - urgent     ← (notice_type == "긴급")
 */
@Getter
@AllArgsConstructor
@Builder
public class NoticeDetailDto {

    private Long id;                 // 공지 ID

    private String title;            // 제목

    private String content;          // 본문(멀티라인 텍스트)

    private String author;           // 작성자/팀명 (예: "팀 시나브로")

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime createdAt; // 작성일

    private Long viewCount;          // 조회수

    private boolean urgent;          // 긴급 공지 여부
}