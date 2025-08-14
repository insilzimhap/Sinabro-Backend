package com.sinabro.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())   // REST + 모바일 앱이라면 CSRF 비활성화
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // 전체 허용(기존 동작 그대로)
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt(평균 60자) — 현재 VARCHAR(255) 컬럼이면 충분
        return new BCryptPasswordEncoder();
    }
}
