package com.sinabro.backend.controller;

import com.sinabro.backend.dto.UserRegisterDto;
import com.sinabro.backend.exception.DuplicateUserException;
import com.sinabro.backend.global.response.CommonResponse;
import com.sinabro.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;




@Tag(name = "회원", description = "회원가입 및 로그인 API")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }



    // 일반 회원가입
    @Operation(
            summary = "일반 회원가입",
            description = """
            사용자가 직접 ID, 이메일, 비밀번호, 이름 등 기본 정보를 입력하여 가입하는 API입니다.  
            이미 존재하는 ID가 있는 경우, 409 에러를 반환합니다.  
            `socialType`은 내부에서 자동으로 "local"로 설정됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "409", description = "중복된 사용자 ID",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody(
                      description = "사용자 회원가입 정보",
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

    @PostMapping("/social-register")
    public ResponseEntity<UserRegisterDto> socialRegister(@RequestBody UserRegisterDto dto) {
        UserRegisterDto saved = userService.registerSocialUser(dto);
        return ResponseEntity.ok(saved);
    }


    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRegisterDto dto) {
        UserRegisterDto user = userService.login(dto.getUserId(), dto.getUserPw());
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(401).body("로그인 실패: 아이디 또는 비밀번호 불일치");
        }
    }
}
