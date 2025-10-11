package com.sinabro.backend.inquiry.admin.controller;

import com.sinabro.backend.inquiry.admin.dto.*;
import com.sinabro.backend.inquiry.admin.service.AdminInquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {
    private final AdminInquiryService inquiryService;

    //***** 포스트맨 해봐야 하는데 user 테이블+쿼리 수정 필요해서 일단 이정도 해놓고
    // 로그인, 회원 목록까지 완성하면 마지막에 inqury 테이블 만들어놓고 시도해보기!!! *********

    // 목록 (q=검색어, status=상태['전체'|'답변 전'|'답변 완료'])
    // 🔧 확장: field=all|title|writerId|writerName, startDate/endDate=YYYY-MM-DD
    @GetMapping
    public ResponseEntity<List<AdminInquiryListItemDto>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String field,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // 기존 동작 유지: 확장 파라미터가 하나도 없으면 이전 서비스 메서드 그대로 사용
        boolean hasExtended =
                (field != null && !field.isBlank())
                        || startDate != null
                        || endDate != null;

        if (!hasExtended) {
            return ResponseEntity.ok(inquiryService.getList(q, status));
        }

        // 확장 동작: 필드/날짜 범위 포함 검색
        return ResponseEntity.ok(inquiryService.getList(q, field, status, startDate, endDate));
    }

    // 상세
    @GetMapping("/{id}")
    public ResponseEntity<AdminInquiryDetailDto> detail(@PathVariable Long id) {
        return ResponseEntity.ok(inquiryService.getDetail(id));
    }

    // 답변 등록/수정 (단일)
    @PutMapping("/{id}/reply")
    public ResponseEntity<Long> upsertReply(
            @PathVariable Long id,
            @RequestBody AdminInquiryReplyUpsertRequestDto request
    ) {
        return ResponseEntity.ok(inquiryService.upsertReply(id, request));
    }

    // 답변 삭제
    @DeleteMapping("/{id}/reply")
    public ResponseEntity<Void> deleteReply(@PathVariable Long id) {
        inquiryService.deleteReply(id);
        return ResponseEntity.ok().build();
    }
}
