package com.sinabro.backend.notice.admin.controller;

import com.sinabro.backend.notice.admin.dto.*;
import com.sinabro.backend.notice.admin.dto.AdminNoticeCreateRequest;
import com.sinabro.backend.notice.admin.dto.AdminNoticeUpdateRequest;
import com.sinabro.backend.notice.admin.service.AdminNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    /** ✅ 목록(전체) */
    @GetMapping
    public ResponseEntity<List<AdminNoticeListItemDto>> list() {
        return ResponseEntity.ok(adminNoticeService.getNoticeList());
    }


    /** ✅ 검색/필터 전용 (프론트는 여기를 호출)
     *  - q: 제목 키워드(부분일치)
     *  - noticeType: 유형(정확일치, 없으면 전체)
     *  - date: 단일 날짜(YYYY-MM-DD) — 들어오면 start/end 를 같은 값으로 처리
     *  - startDate, endDate: 기간 검색(둘 중 하나만 와도 OK)
     */
    @GetMapping("/search")
    public ResponseEntity<List<AdminNoticeListItemDto>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, name = "noticeType") String noticeType,
            @RequestParam(required = false, defaultValue = "all") String field,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // 단일 날짜가 오면 기간으로 변환
        if (date != null) {
            startDate = date;
            endDate = date;
        }
        return ResponseEntity.ok(
                adminNoticeService.searchNotices(q, noticeType, startDate, endDate, field)
        );
    }

    // 상세
    @GetMapping("/{id}")
    public ResponseEntity<AdminNoticeDetailDto> getNoticeDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminNoticeService.getNoticeDetail(id));
    }

    // 등록
    @PostMapping
    public ResponseEntity<Long> createNotice(@RequestBody AdminNoticeCreateRequest request) {
        return ResponseEntity.ok(adminNoticeService.createNotice(request));
    }

    // 수정
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateNotice(
            @PathVariable Long id,
            @RequestBody AdminNoticeUpdateRequest request
    ) {
        adminNoticeService.updateNotice(id, request);
        return ResponseEntity.ok().build();
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        adminNoticeService.deleteNotice(id);
        return ResponseEntity.ok().build();
    }
}
