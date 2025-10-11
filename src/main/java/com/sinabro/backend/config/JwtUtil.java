package com.sinabro.backend.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * [JWT 유틸리티 클래스]
 * - 토큰 발급 및 검증 담당
 * - 현재는 HS256 + secretKey 기반 (졸작 수준이면 충분)
 */
public class JwtUtil {
    // ✅ 실제 서비스에서는 환경변수/설정파일에 두는 게 맞음
    // 최소 32바이트 이상 문자열 필요 (HS256 요구사항)
    private static final String SECRET_KEY = "sinabro-secret-key-very-strong-and-long-secret-key";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    // 토큰 유효기간 (예: 1시간 = 3600000 ms)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60;

    /** JWT 생성 */
    public static String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)              // 토큰 주제(여기서는 userId)
                .setIssuedAt(new Date())         // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 만료 시간
                .signWith(KEY, SignatureAlgorithm.HS256) // ✅ Key 객체로 서명
                .compact();
    }

    /** JWT 검증 */
    public static String validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)   // ✅ 같은 Key 사용
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject(); // userId 반환
        } catch (Exception e) {
            return null; // 실패 시 null
        }
    }
}