package com.sinabro.backend.user.parent.controller;

import com.sinabro.backend.user.parent.dto.UserRegisterDto;
import com.sinabro.backend.user.exception.DuplicateUserException;
import com.sinabro.backend.user.parent.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// ── Swagger OpenAPI ────────────────────────────────────────────────────────────
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// ──────────────────────────────────────────────────────────────────────────────

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/users")
@Tag(
        name = "Users (Parent)",
        description = "부모 사용자 회원가입/로그인 API"
)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 일반 회원가입
    @PostMapping("/register")
    @Operation(
            summary = "일반 회원가입(로컬)",
            description = """
로컬 계정으로 부모 회원가입을 진행.

**처리 방식**
- 서버는 비밀번호를 BCrypt 해시로 저장.
- 응답 DTO에는 `userPw`를 절대 포함 X -> social 로그인을 위해(null).

**기본값**
- `role` = `parent`
- `userLanguage` = `Korea`
- `socialType` = `local`
"""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = UserRegisterDto.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "중복 사용자(ID/Email 등)",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<?> registerUser(
            @RequestBody(
                    description = "부모 회원가입 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRegisterDto.class))
            )
            @org.springframework.web.bind.annotation.RequestBody UserRegisterDto dto
    ) {
        try {
            dto.setSocialType("local");
            UserRegisterDto saved = userService.registerUser(dto);
            return ResponseEntity.ok(saved);
        } catch (DuplicateUserException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    // ⬇️ 소셜은 userPw 안 받아도 됨
    @PostMapping("/social-register")
    @Operation(
            summary = "소셜 회원가입 업서트(Google/Kakao)",
            description = """
소셜 계정으로 부모 가입/업서트를 수행.

**처리 규칙**
- 신규: `user_pw = NULL` 로 저장
- 기존: 비밀번호는 변경/저장하지 않고 이메일/이름/전화/소셜정보만 갱신

**기본값**
- `role` = `parent`
- `userLanguage` = `Korea`
"""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "업서트 성공",
                    content = @Content(schema = @Schema(implementation = UserRegisterDto.class))
            )
    })
    public ResponseEntity<UserRegisterDto> socialRegister(
            @RequestBody(
                    description = "소셜 회원정보(업서트 대상)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRegisterDto.class))
            )
            @org.springframework.web.bind.annotation.RequestBody UserRegisterDto dto
    ) {
        return ResponseEntity.ok(userService.registerSocialUser(dto));
    }

    // 로그인
    @PostMapping("/login")
    @Operation(
            summary = "로컬 로그인",
            description = """
로컬 계정 로그인.

**처리 방식**
- 입력한 평문 비밀번호를 저장된 BCrypt 해시와 `matches()`로 비교.
- 소셜 계정은 `userPw`가 `NULL`이라 이 엔드포인트로 로그인 불가.
"""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = UserRegisterDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "아이디 또는 비밀번호 불일치(또는 소셜 계정)",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<?> login(
            @RequestBody(
                    description = "로그인 정보(`userId`, `userPw`)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRegisterDto.class))
            )
            @org.springframework.web.bind.annotation.RequestBody UserRegisterDto dto
    ) {
        UserRegisterDto user = userService.login(dto.getUserId(), dto.getUserPw());
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(401).body("로그인 실패: 아이디 또는 비밀번호 불일치");
        }
    }
}
