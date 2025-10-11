package com.sinabro.backend.user.app.parent.controller;

import com.sinabro.backend.config.JwtUtil;
import com.sinabro.backend.user.app.parent.dto.*;
import com.sinabro.backend.user.app.exception.DuplicateUserException;
import com.sinabro.backend.user.app.parent.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
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

@Slf4j
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
        dto.setSettings(req.getSettings()); // ✅ 이 줄 추가

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
- 신규: 비밀번호 '필수' (추가정보 단계에서 받은 값)
- 기존: 비밀번호 playload가 온 경우에만 처리 (+ 이메일/이름/전화/소셜정보 갱신)
      - 이미 비밀번호가 있으면 409(충돌)
      - 비밀번호가 없던(legacy) 계정이면 '처음 설정' 허용

기본값
- `role` = `parent`
- `userLanguage` = `Korea`
"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업서트 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "이미 비밀번호 설정된 계정",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<?> socialRegister(
            @RequestBody(
                    description = "소셜 회원정보(추가 정보 포함, 업서트 대상)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SocialRegisterRequest.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody SocialRegisterRequest req
    ) {
        // 1) 비밀번호 유효성: payload가 들어온 경우에만 일치 검사(부분 유효성)
        if (org.springframework.util.StringUtils.hasText(req.getNewPassword()) ||
                org.springframework.util.StringUtils.hasText(req.getConfirmPw())) {
            if (!org.springframework.util.StringUtils.hasText(req.getNewPassword()) ||
                    !org.springframework.util.StringUtils.hasText(req.getConfirmPw()) ||
                    !req.getNewPassword().equals(req.getConfirmPw())) {
                return ResponseEntity.badRequest().body("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            }
        }

        // 2) 서비스 DTO로 매핑 (비밀번호는 userPw 로 전달 → 서비스에서 해시 저장)
        UserRegisterDto dto = new UserRegisterDto();
        dto.setUserId(req.getUserId());
        dto.setUserEmail(req.getUserEmail());
        dto.setUserPw(req.getNewPassword());     // ⬅️ 신규 필수 / 기존은 optional
        dto.setUserName(req.getUserName());
        dto.setUserPhoneNum(req.getUserPhoneNum());
        dto.setUserLanguage(req.getUserLanguage());
        dto.setRole(req.getRole());
        dto.setSocialType(req.getSocialType());
        dto.setSocialId(req.getSocialId());
        dto.setSettings(req.getSettings());

        try {
            var saved = userService.registerSocialUser(dto);
            saved.setUserPw(null); // 응답 안전 처리

            // ✅ JWT 발급 + 함께 반환
            String token = JwtUtil.generateToken(saved.getUserId());
            LoginResponseDto response = LoginResponseDto.builder()
                    .user(saved)
                    .token(token)
                    .build();

            log.info("[소셜가입-컨트롤러] 업서트 완료 + JWT 발급 userId={}", saved.getUserId());
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            // 이미 비밀번호가 설정된 기존 계정에 비번 payload 보낸 케이스
            log.warn("[소셜가입-컨트롤러] 충돌(이미 비번 설정) userId={} msg={}", req.getUserId(), e.getMessage());
            return ResponseEntity.status(409).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            // 신규인데 비번 누락 등
            log.warn("[소셜가입-컨트롤러] 잘못된 요청 userId={} msg={}", req.getUserId(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 로컬 로그인
    // 기존 UserController.java 내 /login 엔드포인트 수정
    @PostMapping("/login")
    @Operation(
            summary = "로컬 로그인",
            description = """
로컬 계정 로그인.

- 입력한 평문 비밀번호를 저장된 BCrypt 해시와 `matches()`로 비교합니다.
- 소셜 계정은 `userPw`가 `NULL`이라 이 엔드포인트로 로그인 불가합니다.
- ✅ 로그인 성공 시 JWT 토큰을 발급하여 응답에 포함합니다.
"""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))
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
        log.info("[로그인-컨트롤러] 호출됨 userId={}", dto.getUserId());

        UserRegisterDto user = userService.login(dto.getUserId(), dto.getUserPw());
        if (user != null) {
            log.info("[로그인-컨트롤러] 인증 성공 userId={}", user.getUserId());
            user.setUserPw(null); // 응답에서 비번 제거

            // ✅ JWT 발급
            String token = com.sinabro.backend.config.JwtUtil.generateToken(user.getUserId());

            // ✅ 사용자 정보 + 토큰을 묶어서 반환
            LoginResponseDto response = LoginResponseDto.builder()
                    .user(user)
                    .token(token)
                    .build();

            log.info("[로그인-컨트롤러] 응답 완료 userId={}", user.getUserId());
            return ResponseEntity.ok(response);
        } else {
            log.warn("[로그인-컨트롤러] 로그인 실패 userId={}", dto.getUserId());
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

    // src/main/java/.../UserController.java
    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "서버는 별도 인증 상태를 보관하지 않습니다. 이 엔드포인트는 로그만 남기고 204를 반환합니다."
    )
    public ResponseEntity<Void> logout() {
        org.slf4j.LoggerFactory.getLogger(getClass())
                .info("[로그아웃] 요청 수신 (서버 보관 상태 없음) → 204 반환");
        return ResponseEntity.noContent().build(); // 204
    }

}

