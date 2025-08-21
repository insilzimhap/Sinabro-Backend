package com.sinabro.backend.admin.user.controller;

import com.sinabro.backend.admin.user.dto.AdminChildDetailDto;
import com.sinabro.backend.admin.user.dto.AdminUserDetailDto;
import com.sinabro.backend.admin.user.dto.AdminUserDto;
import com.sinabro.backend.admin.user.dto.AdminChildDto;
import com.sinabro.backend.admin.user.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService adminUserService;


    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }


    // ✅ 특정 부모의 자녀 리스트 조회
    @GetMapping("/users/{parentUserId}/children")
    public ResponseEntity<List<AdminChildDto>> getChildrenByParent(
            @PathVariable String parentUserId
    ) {
        return ResponseEntity.ok(adminUserService.getChildrenByParent(parentUserId));
    }

    // ✅ 두 번째 화면: 부모 상세 + 자녀 목록
    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserDetailDto> getUserDetail(@PathVariable String userId) {
        return ResponseEntity.ok(adminUserService.getUserDetail(userId));
    }

    // ✅ (옵션) 세 번째 화면: 자녀 상세
    @GetMapping("/children/{childId}")
    public ResponseEntity<AdminChildDetailDto> getChildDetail(@PathVariable String childId) {
        return ResponseEntity.ok(adminUserService.getChildDetail(childId));
    }
}
