package com.sinabro.backend.mypage.parent.controller;

import com.sinabro.backend.mypage.parent.dto.*;
import com.sinabro.backend.mypage.parent.service.ParentMypageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * [앱 > 부모 마이페이지 API]
 * - GET  /api/app/mypage/parent/{userId}                 : 프로필 조회(프리필)
 * - POST /api/app/mypage/parent/{userId}/verify-password : 진입 전 비밀번호 검증
 * - PATCH/PUT /api/app/mypage/parent/{userId}            : 프로필 수정(이메일/전화번호 + 옵션 비번 변경)
 *
 * ※ 인증 연동 시 userId 대신 인증 주체에서 가져오는 방식으로 변경 가능
 */
@RestController
@RequestMapping("/api/app/mypage/parent")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ParentMypageController {

    private final ParentMypageService parentMypageService;

    /** 프로필 조회 (마이페이지 프리필) */
    @GetMapping("/{userId}")
    public ResponseEntity<ParentProfileResponseDto> getProfile(@PathVariable String userId) {
        var dto = parentMypageService.getProfile(userId);
        return ResponseEntity.ok(dto);
    }

    /** 마이페이지 진입 전 비밀번호 검증 */
    @PostMapping("/{userId}/verify-password")
    public ResponseEntity<Void> verifyPassword(
            @PathVariable String userId,
            @RequestBody @Valid PasswordVerifyRequestDto req
    ) {
        parentMypageService.verifyPassword(userId, req);
        return ResponseEntity.noContent().build(); // 204
    }

    /** 프로필 수정 (이메일/전화번호 + 선택적 비밀번호 변경) */
    @PatchMapping("/{userId}")
    public ResponseEntity<ParentProfileResponseDto> updateProfile(
            @PathVariable String userId,
            @RequestBody @Valid ParentUpdateRequestDto req
    ) {
        var updated = parentMypageService.updateProfile(userId, req);
        return ResponseEntity.ok(updated);
    }
}
