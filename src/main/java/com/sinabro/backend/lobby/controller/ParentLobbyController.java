package com.sinabro.backend.lobby.controller;

import com.sinabro.backend.lobby.service.ParentLobbyService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ── Swagger OpenAPI ────────────────────────────────────────────────────────────
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// ──────────────────────────────────────────────────────────────────────────────

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(
        name = "Parent Lobby",
        description = "부모 로비 화면 API (프로필, 자녀 목록 조회)"
)
@SecurityRequirement(name = "bearerAuth") // 🔐 스웨거: Authorization 헤더(bearer) 필요 표기
@Slf4j
public class ParentLobbyController {

    private final ParentLobbyService parentLobbyService;

    /**
     * ✅ JWT 주체에서 userId를 읽어 부모 프로필(이름) 조회
     * - 더 이상 쿼리 파라미터로 userId를 받지 않습니다.
     * - JWT 클레임 'userId'가 없으면 subject(sub)를 폴백으로 사용합니다.
     */
    @GetMapping("/users/profile")
    @Operation(
            summary = "부모 프로필 조회 (JWT 주체 기반)",
            description = """
JWT의 주체에서 userId를 읽어 부모 이름을 반환합니다.
- 우선순위: claim.userId → sub(주체)
- DB: user.user_name 컬럼 값을 반환
"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "부모 이름 조회 성공",
                    content = @Content(schema = @Schema(implementation = ParentProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 또는 JWT에서 userId를 확인할 수 없음",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<ParentProfileResponse> getParentProfile(
            @AuthenticationPrincipal String userId   // ✅ 필터가 넣은 userId 그대로 주입
    ) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(UNAUTHORIZED, "인증 정보가 없습니다.");
        }
        log.info("[부모-프로필] 요청 수신 userId={}", userId);
        String name = parentLobbyService.getParentName(userId);
        log.info("[부모-프로필] 조회 완료 userId={} userName={}", userId, name);
        return ResponseEntity.ok(new ParentProfileResponse(name));
    }

    /**
     * ✅ JWT 주체에서 userId를 읽어 자녀 목록 조회
     * - 더 이상 쿼리 파라미터로 userId를 받지 않습니다.
     * - 해당 부모(userId)에 연결된 모든 자녀를 반환합니다.
     */
    @GetMapping("/children")
    @Operation(
            summary = "부모 자녀 목록 조회 (JWT 주체 기반)",
            description = """
JWT의 주체에서 userId를 읽어, 해당 부모에 연결된 자녀 목록을 반환합니다.
DB: child 테이블에서 childId, childName, childNickname, childAge 반환
"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "자녀 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ChildSummaryResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 또는 JWT에서 userId를 확인할 수 없음",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<List<ChildSummaryResponse>> getChildren(
            @AuthenticationPrincipal String userId   // ✅ 동일
    ) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(UNAUTHORIZED, "인증 정보가 없습니다.");
        }
        log.info("[부모-자녀목록] 요청 수신 userId={}", userId);
        var list = parentLobbyService.getChildren(userId);
        var body = list.stream().map(c ->
                new ChildSummaryResponse(
                        c.getChildId(),
                        c.getChildName(),
                        c.getChildNickname(),
                        c.getChildAge()
                )
        ).toList();
        log.info("[부모-자녀목록] 조회 완료 userId={} count={}", userId, body.size());
        return ResponseEntity.ok(body);
    }

    // ───────────────────────── 내부 DTO ─────────────────────────

    @Data
    @AllArgsConstructor
    static class ParentProfileResponse {
        @Schema(description = "부모 이름", example = "홍길동")
        private String userName; // user.user_name
    }

    @Data
    @AllArgsConstructor
    static class ChildSummaryResponse {
        @Schema(description = "자녀 ID (PK)", example = "child001")
        private String childId;

        @Schema(description = "자녀 이름", example = "김철수")
        private String childName;

        @Schema(description = "자녀 닉네임", example = "뽀로로")
        private String childNickname;

        @Schema(description = "자녀 나이", example = "6")
        private Integer childAge;
    }

}