package com.sinabro.backend.controller.user;

import com.sinabro.backend.dto.user.UserRegisterDto;
import com.sinabro.backend.exception.DuplicateUserException;
import com.sinabro.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 일반 회원가입
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterDto dto) {
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
