package com.sinabro.backend.user.admin.controller;

import com.sinabro.backend.user.admin.dto.*;
import com.sinabro.backend.user.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService adminUserService;


    // 부모 전체 목록 띄우기
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }


    // 특정 부모의 자녀 리스트 조회
    @GetMapping("/users/{parentUserId}/children")
    public ResponseEntity<List<AdminChildDto>> getChildrenByParent(
            @PathVariable String parentUserId
    ) {
        return ResponseEntity.ok(adminUserService.getChildrenByParent(parentUserId));
    }

    // 부모 상세 + 자녀 목록
    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserDetailDto> getUserDetail(@PathVariable String userId) {
        return ResponseEntity.ok(adminUserService.getUserDetail(userId));
    }

    // 자녀 상세
    @GetMapping("/children/{childId}")
    public ResponseEntity<AdminChildDetailDto> getChildDetail(@PathVariable String childId) {
        return ResponseEntity.ok(adminUserService.getChildDetail(childId));
    }

    // 부모 정보 수정: 200 OK (body 없음)
    @PutMapping("/users/{userId}")
    public ResponseEntity<Void> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody AdminUserUpdateRequest req
    ) {
        adminUserService.updateUser(userId, req);
        return ResponseEntity.ok().build(); // 200
    }

    // 부모 삭제: 204 No Content
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        boolean deleted = adminUserService.deleteUser(userId);
        return deleted ? ResponseEntity.noContent().build()    // 204
                : ResponseEntity.notFound().build();     // 404
    }

    // 자녀 삭제: 204 No Content (없으면 404)
    @DeleteMapping("/children/{childId}")
    public ResponseEntity<Void> deleteChild(@PathVariable String childId) {
        boolean ok = adminUserService.deleteChild(childId);
        return ok ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // 🔎 검색 (부모 목록 반환)
    @GetMapping("/users/search")
    public ResponseEntity<List<AdminUserDto>> searchUsers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "all") String field, // 기본값 all | parentName | childName | email | all
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(adminUserService.searchUsers(q, field, startDate, endDate));
    }

}
