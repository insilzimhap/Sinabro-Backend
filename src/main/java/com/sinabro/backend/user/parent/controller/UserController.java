package com.sinabro.backend.user.parent.controller;

import com.sinabro.backend.user.exception.DuplicateUserException;
import com.sinabro.backend.user.parent.dto.UserCheckDto;
import com.sinabro.backend.user.parent.dto.UserRegisterDto;
import com.sinabro.backend.user.parent.dto.UserRegisterRequest;
import com.sinabro.backend.user.parent.service.UserService;
import jakarta.validation.Valid;
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

    // 일반 회원가입 (요청 DTO 분리 + 비밀번호 확인)
    @PostMapping("/register")
    @Operation(
            summary = "일반 회원가입(로컬)",
            description = """
로컬 계정으로 부모 회원가입을 진행합니다.

- 요청 바디에 `userPw`와 `confirmPw`가 동일해야 합니다.
- 서버는 비밀번호를 BCrypt 해시로 저장합니다.
- 응답 DTO에는 비밀번호가 포함되지 않습니다.

기본값
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
                    responseCode = "400",
                    description = "비밀번호 확인 불일치",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "중복 사용자(ID/Email 등)",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<?> registerUser(
            @RequestBody(
                    description = "부모 회원가입 요청",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRegisterRequest.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody UserRegisterRequest req
    ) {
        // 1) 비밀번호 확인
        if (!req.getUserPw().equals(req.getConfirmPw())) {
            return ResponseEntity.badRequest().body("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 2) 서비스가 기대하는 DTO로 매핑
        UserRegisterDto dto = new UserRegisterDto();
        dto.setUserId(req.getUserId());
        dto.setUserEmail(req.getUserEmail());
        dto.setUserPw(req.getUserPw());
        dto.setUserName(req.getUserName());
        dto.setUserPhoneNum(req.getUserPhoneNum());
        dto.setUserLanguage(req.getUserLanguage()); // null이면 서비스/엔티티 @PrePersist로 기본값 처리 가능
        dto.setRole(req.getRole());                 // null이면 서비스에서 'parent'로 처리해도 됨
        dto.setSocialType("local");

        try {
            UserRegisterDto saved = userService.registerUser(dto);
            // 보안상 응답에서 비밀번호 제거
            saved.setUserPw(null);
            return ResponseEntity.ok(saved);
        } catch (DuplicateUserException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    // 소셜 회원가입 업서트
    @PostMapping("/social-register")
    @Operation(
            summary = "소셜 회원가입 업서트(Google/Kakao)",
            description = """
소셜 계정으로 부모 가입/업서트를 수행합니다.

처리 규칙
- 신규: `user_pw = NULL` 로 저장
- 기존: 비밀번호는 변경/저장하지 않고 이메일/이름/전화/소셜정보만 갱신

기본값
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

    // 로컬 로그인
    @PostMapping("/login")
    @Operation(
            summary = "로컬 로그인",
            description = """
로컬 계정 로그인.

- 입력한 평문 비밀번호를 저장된 BCrypt 해시와 `matches()`로 비교합니다.
- 소셜 계정은 `userPw`가 `NULL`이라 이 엔드포인트로 로그인 불가합니다.
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
            user.setUserPw(null); // 응답에서 비번 제거
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(401).body("로그인 실패: 아이디 또는 비밀번호 불일치");
        }
    }

    // 중복 아이디 체크
    @GetMapping("/check-id")
    @Operation(
            summary = "아이디 중복 확인",
            description = "쿼리스트링 ?userId=xxx 로 요청. available=true 이면 사용 가능."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확인 성공",
                    content = @Content(schema = @Schema(implementation = UserCheckDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<?> checkUserId(@RequestParam("userId") String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body("userId는 필수입니다.");
        }
        boolean available = userService.isUserIdAvailable(userId);
        return ResponseEntity.ok(
                UserCheckDto.builder()
                        .field("userId")
                        .value(userId)
                        .available(available)
                        .build()
        );
    }
}
