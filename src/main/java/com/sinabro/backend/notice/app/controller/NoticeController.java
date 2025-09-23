package com.sinabro.backend.notice.app.controller;

import com.sinabro.backend.notice.app.dto.*;
import com.sinabro.backend.notice.app.dto.NoticePageResponse;
import com.sinabro.backend.notice.app.service.NoticeQueryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * [모바일 앱 > 공지사항 API]
 * - GET /api/app/notices            : 리스트 (긴급 우선 + 최신순, 페이지네이션)
 * - GET /api/app/notices/{id}       : 상세 (increaseView=true면 조회수 +1)
 */
@RestController
@RequestMapping("/api/app/notices")
@RequiredArgsConstructor
@Validated
@Slf4j
public class NoticeController {

    private final NoticeQueryService noticeQueryService;

    // 공지 리스트
    // [모바일 앱 > 공지사항 API] 리스트/상세 조회 엔드포인트 (파라미터 검증 @Validated 적용)
    @GetMapping
    public ResponseEntity<NoticePageResponse<NoticeListItemDto>> getNotices(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        log.info("[NoticeController] GET /api/app/notices page={} size={}", page, size); // 호출
        var body = noticeQueryService.getNoticeList(page, size);
        log.info("[NoticeController] return list page={} size={} count={}",
                body.getPage(), body.getSize(), body.getContent().size()); // 성공
        return ResponseEntity.ok(body);
    }

    // 공지 상세 (+ 조회수 증가)
    @GetMapping("/{id}")
    public ResponseEntity<NoticeDetailDto> getNoticeDetail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean increaseView
    ) {
        log.info("[NoticeController] GET /api/app/notices/{}?increaseView={}", id, increaseView); // 호출
        var body = noticeQueryService.getNoticeDetail(id, increaseView);
        log.info("[NoticeController] return detail id={} viewCount={}", id, body.getViewCount()); // 성공
        return ResponseEntity.ok(body);
    }
}