package com.sinabro.backend.global.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * [글로벌 예외 처리기]
 * - 목적: 컨트롤러 전역의 예외를 공통 포맷으로 응답하고 적절한 HTTP 상태코드로 매핑
 * - 주요 처리:
 *    1) ConstraintViolationException  → 400 Bad Request  (@RequestParam/@PathVariable 검증 실패)
 *    2) MethodArgumentNotValidException → 400 Bad Request (@RequestBody @Valid 검증 실패)
 *    3) ResponseStatusException        → 선언된 상태코드 그대로 반환
 * - 로그 정책: 검증 실패는 warn, 예기치 못한 오류는 error
 */


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // @RequestParam/@PathVariable 검증 실패 → 400으로 변환
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleConstraint(jakarta.validation.ConstraintViolationException e) {
        log.warn("[400 Validation] {}", e.getMessage());  // '오류' 로그
        return Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", e.getMessage()
        );
    }

    // @RequestBody @Valid 바인딩 실패 → 400
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleMethodArgNotValid(org.springframework.web.bind.MethodArgumentNotValidException e) {
        var errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
                .toList();
        log.warn("[400 Validation] {} -> {}", e.getObjectName(), errors);
        return Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", "validation failed",
                "errors", errors
        );
    }

    // JSON 파싱 실패, 타입 불일치 등 → 400
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleUnreadable(org.springframework.http.converter.HttpMessageNotReadableException e) {
        log.warn("[400 Parse] {}", e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage());
        return Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", "malformed request body"
        );
    }

    // 서비스/컨트롤러에서 명시적으로 던진 ResponseStatusException 유지
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public org.springframework.http.ResponseEntity<Map<String, Object>> handleRse(org.springframework.web.server.ResponseStatusException e) {
        var status = e.getStatusCode();
        log.warn("[{}] {}", status.value(), e.getReason());
        return org.springframework.http.ResponseEntity.status(status).body(
                Map.of(
                        "status", status.value(),
                        "error", status.toString(),
                        "message", e.getReason()
                )
        );
    }

    // 잘못된 파라미터 등 → 400
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArg(IllegalArgumentException e) {
        log.warn("[400 IllegalArgument] {}", e.getMessage());
        return Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", e.getMessage()
        );
    }

    // 제약조건 위반(중복키 등) → 409
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleDataIntegrity(org.springframework.dao.DataIntegrityViolationException e) {
        log.error("[409 Conflict] {}", e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage());
        return Map.of(
                "status", 409,
                "error", "Conflict",
                "message", "data integrity violation"
        );
    }

    // 마지막 안전망 → 500
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleAny(Exception e) {
        log.error("[500] {}", e.getMessage(), e);
        return Map.of(
                "status", 500,
                "error", "Internal Server Error",
                "message", "unexpected error"
        );
    }

}
