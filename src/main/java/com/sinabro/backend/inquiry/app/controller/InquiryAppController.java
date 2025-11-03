package com.sinabro.backend.inquiry.app.controller;

import com.sinabro.backend.inquiry.app.dto.*;
import com.sinabro.backend.inquiry.app.service.InquiryAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/inquiries")
@Slf4j
public class InquiryAppController {

    private final InquiryAppService inquiryAppService;

    /** A. 목록 조회 */
    @GetMapping("/parent/{parentUserId}")
    public InquiryListResponseDto getList(
            @PathVariable String parentUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.debug("[API] GET /inquiries list parent={} page={} size={}", parentUserId, page, size);
        return inquiryAppService.getList(parentUserId, page, size);
    }

    /** B. 상세 조회 */
    @GetMapping("/parent/{parentUserId}/{inquiryId}")
    public InquiryDetailDto getDetail(
            @PathVariable String parentUserId,
            @PathVariable Long inquiryId
    ) {
        log.debug("[API] GET /inquiries detail parent={} id={}", parentUserId, inquiryId);
        return inquiryAppService.getDetail(parentUserId, inquiryId);
    }

    /** C. 등록 */
    @PostMapping("/parent/{parentUserId}")
    public ResponseEntity<Void> create(
            @PathVariable String parentUserId,
            @Valid @RequestBody InquiryCreateRequestDto req
    ) {
        log.debug("[API] POST /inquiries parent={}", parentUserId);
        Long id = inquiryAppService.create(parentUserId, req);
        return ResponseEntity
                .created(URI.create("/api/app/inquiries/parent/" + parentUserId + "/" + id))
                .build();
    }
}
