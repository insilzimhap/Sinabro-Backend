package com.sinabro.backend.user.child.controller;

import com.sinabro.backend.user.child.dto.ChildRegisterDto;
import com.sinabro.backend.user.child.entity.Child;
import com.sinabro.backend.user.child.repository.ChildRepository;
import com.sinabro.backend.user.child.service.ChildService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

// ── Swagger OpenAPI ────────────────────────────────────────────────────────────
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
// ──────────────────────────────────────────────────────────────────────────────

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/child")
@Tag(
        name = "Child",
        description = "자녀 회원 관리 및 로비 정보 조회 API"
)
public class ChildController {

    private final ChildService childService;
    private final ChildRepository childRepository; // ✅ level 추가 위해 주입

    public ChildController(ChildService childService, ChildRepository childRepository) {
        this.childService = childService;
        this.childRepository = childRepository;
    }

    @PostMapping("/register")
    @Operation(
            summary = "자녀 회원 등록",
            description = """
자녀 회원 정보를 등록.

**처리 방식**
- 비밀번호는 서버에서 BCrypt 해시로 저장.
- 응답 DTO에는 비밀번호(혹은 해시)를 포함하지 않음.

**필드 규칙**
- `childLevel` 초기 null 허용
- `timeLimitMinutes` null 시 서버에서 0 적용
"""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "자녀 회원 등록 성공",
                    content = @Content(schema = @Schema(implementation = ChildRegisterDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 부모 ID 등으로 인한 등록 실패",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<ChildRegisterDto> registerChild(
            @RequestBody(
                    description = "자녀 회원 등록 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ChildRegisterDto.class))
            )
            @org.springframework.web.bind.annotation.RequestBody ChildRegisterDto dto
    ) {
        ChildRegisterDto saved = childService.registerChild(dto);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    @Operation(
            summary = "자녀 로그인 검증",
            description = """
자녀 로그인 요청을 검증.

**처리 방식**
- 입력한 평문 비밀번호를 저장된 BCrypt 해시와 `matches()`로 비교.
- 일치 시 `childId` 반환, 불일치 시 401 반환.
"""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 검증 성공",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "아이디 또는 비밀번호 불일치",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<?> loginChild(
            @RequestBody(
                    description = "자녀 로그인 정보(`childId`, `childPw`)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ChildRegisterDto.class))
            )
            @org.springframework.web.bind.annotation.RequestBody ChildRegisterDto dto
    ) {
        boolean success = childService.loginChild(dto.getChildId(), dto.getChildPw());
        if (success) {
            return ResponseEntity.ok(dto.getChildId());
        } else {
            return ResponseEntity.status(401).body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    // ✅ Service를 통해 childId로 닉네임, 캐릭터ID 조회
    @GetMapping("/info")
    @Operation(
            summary = "자녀 로비 정보 조회",
            description = """
자녀 ID를 기준으로 로비 표시용 정보를 조회.

**반환 필드**
- `nickname`: 자녀 닉네임
- `characterId`: 선택한 캐릭터 ID (없으면 null)
- `level`: 자녀 레벨(정수 1/2/3) 또는 `"-"` (없을 때)

**주의**
- `level`은 컨트롤러에서 최종 합성해 반환.
"""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "자녀 로비 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "존재하지 않는 childId",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public Map<String, Object> getChildInfo(
            @Parameter(
                    description = "자녀 ID (child.child_id)",
                    required = true,
                    example = "child001"
            )
            @RequestParam("childId") String childId // ← 이름 명시!
    ) {
        // 기존 서비스가 주는 맵 유지
        Map<String, Object> base = childService.getChildInfo(childId);

        // ✅ 여기서 childLevel만 안전하게 추가
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("해당 자녀를 찾을 수 없습니다."));
        Integer level = child.getChildLevel(); // INT, null 허용

        // 새로운 맵으로 합쳐서 반환 (기존 키 보존)
        Map<String, Object> result = new HashMap<>(base);
        result.put("level", level == null ? "-" : level); // ← 프론트는 이 키로 읽으면 됨

        return result;
    }
}
