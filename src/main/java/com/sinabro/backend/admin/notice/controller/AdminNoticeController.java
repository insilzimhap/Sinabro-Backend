package com.sinabro.backend.admin.notice.controller;

import com.sinabro.backend.admin.notice.dto.*;
import com.sinabro.backend.admin.notice.service.AdminNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    // 목록
    @GetMapping
    public ResponseEntity<List<AdminNoticeListItemDto>> getNoticeList() {
        return ResponseEntity.ok(adminNoticeService.getNoticeList());
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
