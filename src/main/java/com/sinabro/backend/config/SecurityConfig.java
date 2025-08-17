package com.sinabro.backend.config;

import com.sinabro.backend.admin.user.service.AdminUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    // =========================
    // 기존(모바일 등) 공개 체인
    // - 현재처럼 permitAll 유지
    // =========================
    @Bean
    @Order(2) // ✅ admin 다음 우선순위
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())   // REST + 모바일 앱이라면 CSRF 비활성화
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // 전체 허용(기존 동작 그대로)
                );
        return http.build();
    }

    // =========================
    // 공통: BCrypt 비밀번호 인코더
    // =========================
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt(평균 60자) — 현재 VARCHAR(255) 컬럼이면 충분
        return new BCryptPasswordEncoder();
    }


    // =========================
    // 관리자 전용 필터 체인 (/api/admin/**)
    // - 세션 기반 로그인 보호
    // - ROLE_ADMIN 만 접근 허용
    // =========================
    @Bean
    @Order(1) // ✅ /api/admin/** 에 먼저 매칭되도록 높은 우선순위
    public SecurityFilterChain adminFilterChain(HttpSecurity http,
                                                AuthenticationManager authenticationManager) throws Exception {
        http
                .securityMatcher("/api/admin/**")          // 이 체인은 관리자 API에만 적용
                .csrf(csrf -> csrf.disable())              // REST API면 보통 비활성화
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // 로그인·로그아웃·상태확인만 익명 허용
                        .requestMatchers(
                                "/api/admin/auth/login",
                                "/api/admin/auth/logout",
                                "/api/admin/auth/me"
                        ).permitAll()
                        // 나머지는 전부 관리자만 접근
                        .anyRequest().hasRole("ADMIN")
                )
                .authenticationManager(authenticationManager) // DaoAuthenticationProvider 사용
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())                  // 폼 UI 안 씀(REST)
                .logout(l -> l.disable())                     // 로그아웃은 전용 엔드포인트로 처리
                .sessionManagement(sess -> sess.sessionFixation().migrateSession()); // 세션 하이재킹 방지
        return http.build();
    }

    // =========================
    // 관리자 인증에 사용할 AuthenticationManager
    // - AdminUserDetailsService + BCrypt
    // =========================
    @Bean
    public AuthenticationManager authenticationManager(AdminUserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService); // DB에서 사용자 로드
        provider.setPasswordEncoder(passwordEncoder);       // BCrypt 검증
        return new ProviderManager(provider);
    }
}
