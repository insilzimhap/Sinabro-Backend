package com.sinabro.backend.mypage.child.controller;

import com.sinabro.backend.mypage.child.dto.*;
import com.sinabro.backend.mypage.child.service.ChildMyPageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app/mypage")
public class ChildMyPageController {

    private final ChildMyPageService childMyPageService;

    /** 자녀 프로필 프리필 조회 */
    @GetMapping("/children/{childId}")
    public ResponseEntity<ChildProfileResponseDto> getChild(
            @PathVariable String childId
    ) {
        return ResponseEntity.ok(childMyPageService.getChildProfile(childId));
    }

    /** 자녀 프로필 수정 (닉네임/생일/비밀번호-선택) */
    @PatchMapping("/children/{childId}")
    public ResponseEntity<ChildProfileResponseDto> updateChild(
            @PathVariable String childId,
            @Valid @RequestBody ChildUpdateRequestDto req
    ) {
        var updated = childMyPageService.updateChild(childId, req);
        return ResponseEntity.ok(updated);
    }

    /**
     * [1단계] 자녀 삭제 사전 검증(부모 비밀번호 확인)
     * - 204 → 200 + ChildDeleteResponseDto 로 변경 (모달용 이름 반환)
     */
    @PostMapping("/parent/{parentUserId}/children/{childId}/verify-delete")
    public ResponseEntity<ChildDeleteResponseDto> verifyDelete(
            @PathVariable String parentUserId,
            @PathVariable String childId,
            @Valid @RequestBody ChildDeleteRequestDto req
    ) {
        var dto = childMyPageService.verifyChildDelete(parentUserId, childId, req);
        return ResponseEntity.ok(dto); // 200 OK + { childId, childName, verified=true, deleted=false }
    }

    /**
     * [2단계] 자녀 계정 삭제 (부모 비밀번호 다시 전달)
     * - 204 → 200 + ChildDeleteResponseDto 로 변경 (토스트용 이름/상태 반환)
     */
    @DeleteMapping("/parent/{parentUserId}/children/{childId}")
    public ResponseEntity<ChildDeleteResponseDto> deleteChild(
            @PathVariable String parentUserId,
            @PathVariable String childId,
            @Valid @RequestBody ChildDeleteRequestDto req
    ) {
        var dto = childMyPageService.deleteChildAndReturn(parentUserId, childId, req);
        return ResponseEntity.ok(dto); // 200 OK + { childId, childName, verified=true, deleted=true }
    }


}