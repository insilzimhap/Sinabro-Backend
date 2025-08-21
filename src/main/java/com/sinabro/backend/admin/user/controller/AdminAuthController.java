package com.sinabro.backend.admin.user.controller;


import com.sinabro.backend.admin.user.dto.AdminLoginRequestDto;
import com.sinabro.backend.admin.user.dto.AdminMeResponseDto;
import com.sinabro.backend.user.parent.entity.User;
import com.sinabro.backend.user.parent.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    private final AuthenticationManager authenticationManager; // 시큐리티 인증 수행
    private final UserRepository userRepository;               // /me 응답용 최소 조회

    /**
     * 관리자 로그인
     * - 성공 시 세션 생성, 본문 없이 204(No Content) 반환
     * - 실패 시 401/403
     *
     * - DB의 role 이 'admin' 인 계정만 로그인 허용
     * - role != admin 이면 비밀번호가 맞아도 403
     * - 소셜 계정(user_pw == null)은 401 (비번 로그인 불가)
     *
     */
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody AdminLoginRequestDto req, HttpServletRequest request) {
        //로그
        log.info("✅ Admin login attempt: userId={}", req.getUserId());

        // (A) 먼저 DB에서 사용자 존재/권한 선검사
        var userOpt = userRepository.findByUserId(req.getUserId());
        if (userOpt.isEmpty()) {
            log.warn("❌ Login failed - no such user: {}", req.getUserId());
            return ResponseEntity.status(401).build(); // 아이디 없음
        }

        var u = userOpt.get();

        // 소셜 계정 등 비밀번호가 없는 경우 → 비번 로그인 불가
        if (u.getUserPw() == null || u.getUserPw().isBlank()) {
            return ResponseEntity.status(401).build();
        }

        // role=admin 만 로그인 허용
        if (u.getRole() == null || !u.getRole().equalsIgnoreCase("admin")) {
            return ResponseEntity.status(403).build(); // 관리자 아님
        }

        // (B) 표준 인증 절차 수행 (BCrypt 검증 포함)
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUserId(), req.getPassword())
        );

        // (C) 방어적으로도 ROLE_ADMIN 권한 확인(이중 체크)
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        if (!isAdmin) {
            return ResponseEntity.status(403).build();
        }

        // (D) 세션에 인증 컨텍스트 저장
        SecurityContextHolder.getContext().setAuthentication(auth);
        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        log.info("✅ Admin login success: {}", req.getUserId());
        return ResponseEntity.noContent().build(); // 204
    }

    /**
     * 로그인 상태 확인
     * - 인증 상태가 아니면 401
     * - 인증 상태면 최소 정보 반환(프론트에서 “관리자” 표기만 써도 됨)
     */
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        log.info("📌 [ME] 요청 들어옴");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }

        String userId = auth.getName(); // = users.user_id
        Optional<User> opt = userRepository.findByUserId(userId);
        if (opt.isEmpty()) return ResponseEntity.status(401).build();

        User u = opt.get();
        AdminMeResponseDto body = AdminMeResponseDto.builder()
                .userId(u.getUserId())
                .name(u.getUserName())   // 화면에는 그냥 “관리자”라고 써도 무방
                .role(u.getRole())       // “admin” 기대
                .build();

        log.info("✅ [ME] 성공 - userId={}, role={}", u.getUserId(), u.getRole());
        return ResponseEntity.ok(body);
    }

    /**
     * 로그아웃
     * - 세션 무효화 후 204
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        log.info("📌 [LOGOUT] 요청 들어옴");

        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();

        log.info("✅ [LOGOUT] 성공 - 세션 무효화 완료");
        return ResponseEntity.noContent().build();
    }
}
