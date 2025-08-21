package com.sinabro.backend.lobby.controller;

import com.sinabro.backend.lobby.service.ParentLobbyService;
import com.sinabro.backend.user.child.entity.Child;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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
public class ParentLobbyController {

    private final ParentLobbyService parentLobbyService;

    // ✅ 부모 이름 조회
    @GetMapping("/users/profile")
    @Operation(
            summary = "부모 프로필 조회",
            description = """
부모의 userId를 기준으로 부모 이름을 조회.
DB: `user.user_name` 값을 반환.
"""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "부모 이름 조회 성공",
                    content = @Content(schema = @Schema(implementation = ParentProfileResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "존재하지 않는 userId",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<ParentProfileResponse> getParentProfile(
            @Parameter(
                    description = "부모 ID (user.user_id)",
                    required = true,
                    example = "parent123"
            )
            @RequestParam("userId") String userId
    ) {
        String name = parentLobbyService.getParentName(userId);
        return ResponseEntity.ok(new ParentProfileResponse(name));
    }

    // ✅ 부모 기준 자녀 목록 조회
    @GetMapping("/children")
    @Operation(
            summary = "부모 자녀 목록 조회",
            description = """
부모 userId를 기준으로 연결된 모든 자녀 정보를 조회.
DB: `child` 테이블에서 childId, childName, childNickname, childAge 반환.
"""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "자녀 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ChildSummaryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "존재하지 않는 userId",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<List<ChildSummaryResponse>> getChildren(
            @Parameter(
                    description = "부모 ID (user.user_id)",
                    required = true,
                    example = "parent123"
            )
            @RequestParam("userId") String userId
    ) {
        List<Child> list = parentLobbyService.getChildren(userId);
        return ResponseEntity.ok(
                list.stream().map(c ->
                        new ChildSummaryResponse(
                                c.getChildId(),
                                c.getChildName(),
                                c.getChildNickname(),
                                c.getChildAge()
                        )
                ).toList()
        );
    }

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
