package com.sinabro.backend.config;

import com.sinabro.backend.user.entity.User;
import com.sinabro.backend.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [JWT 인증 필터]
 * - 모든 요청마다 1회 실행(OncePerRequestFilter)
 * - 1) 요청 헤더의 Authorization에서 "Bearer <JWT>" 추출
 * - 2) JwtUtil로 토큰 검증 → userId(subject) 획득
 * - 3) DB에서 사용자 로드 → SecurityContext에 인증 정보 저장
 * - 4) 화이트리스트(회원가입/로그인/문서 등)는 필터를 건너뜀
 *
 * 참고:
 * - 이 필터는 "인증"만 수행한다. 인가(권한 체크)는 SecurityConfig에서 처리함
 * - 토큰이 없거나 검증 실패해도 바로 응답을 막지 않음
 *   → SecurityConfig가 authenticated()인 엔드포인트에서 최종 401/403 처리됨
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final AntPathMatcher matcher = new AntPathMatcher();

    // ✅ 필터를 적용하지 않을 경로(permitAll 대상)
    //    필요 시 여기에 추가하면 된다.
    private static final String[] WHITELIST = {
            "/api/users/login",
            "/api/users/register",
            "/api/users/social-register",
            // 문서/헬스체크 등
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health"
    };

    public JwtAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** 화이트리스트 경로는 필터를 건너뛴다. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String pattern : WHITELIST) {
            if (matcher.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        final String uri = request.getRequestURI();
        final String method = request.getMethod();

        // ── 1) 시작 로그 ─────────────────────────────────────────────
        log.info("[JWT필터] 시작 method={} uri={}", method, uri);

        // 이미 인증이 설정되어 있으면 패스
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.debug("[JWT필터] 이미 인증 정보 존재 → 통과");
            chain.doFilter(request, response);
            return;
        }

        // ── 2) Authorization 헤더에서 토큰 추출 ──────────────────────
        String header = request.getHeader("Authorization");
        if (header == null) {
            log.debug("[JWT필터] Authorization 헤더 없음 → 통과");
            chain.doFilter(request, response);
            return;
        }
        if (!header.startsWith("Bearer ")) {
            log.warn("[JWT필터] Authorization 포맷 불일치(Expect: Bearer <token>) → 통과");
            chain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);

        // ── 3) 토큰 검증 및 userId 추출 ──────────────────────────────
        String userId = JwtUtil.validateToken(token); // 유효하면 subject(userId), 실패/null 반환
        if (userId == null) {
            log.warn("[JWT필터] 토큰 검증 실패 또는 만료 → 인증 미설정 상태로 통과");
            chain.doFilter(request, response);
            return;
        }
        log.info("[JWT필터] 토큰 검증 성공 userId={}", userId);

        // ── 4) DB에서 사용자 조회 ────────────────────────────────────
        User user = userRepository.findByUserId(userId).orElse(null);
        if (user == null) {
            log.warn("[JWT필터] 사용자 미존재 userId={} → 통과", userId);
            chain.doFilter(request, response);
            return;
        }

        // 권한 매핑: User.role 문자열을 Spring 권한으로 변환(예: parent → ROLE_PARENT)
        List<SimpleGrantedAuthority> authorities = (user.getRole() == null || user.getRole().isBlank())
                ? List.of()
                : List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));

        // ── 5) SecurityContext에 인증 설정 ──────────────────────────
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getUserId(), null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("[JWT필터] SecurityContext 설정 완료 userId={} authorities={}",
                user.getUserId(),
                authorities.stream().map(Object::toString).collect(Collectors.toList()));

        // ── 6) 다음 필터로 진행 ─────────────────────────────────────
        chain.doFilter(request, response);

        // ── 7) 종료 로그 ────────────────────────────────────────────
        log.debug("[JWT필터] 종료 method={} uri={}", method, uri);
    }
}
