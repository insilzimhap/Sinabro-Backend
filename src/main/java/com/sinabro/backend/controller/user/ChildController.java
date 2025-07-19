package com.sinabro.backend.controller.user;

import com.sinabro.backend.dto.user.ChildRegisterDto;
import com.sinabro.backend.service.ChildService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/child")
public class ChildController {

    private final ChildService childService;

    public ChildController(ChildService childService) {
        this.childService = childService;
    }

    @PostMapping("/register")
    public ResponseEntity<ChildRegisterDto> registerChild(@RequestBody ChildRegisterDto dto) {
        ChildRegisterDto saved = childService.registerChild(dto);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginChild(@RequestBody ChildRegisterDto dto) {
        boolean success = childService.loginChild(dto.getChildId(), dto.getChildPw());
        if (success) {
            return ResponseEntity.ok(dto.getChildId());
        } else {
            return ResponseEntity.status(401).body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    // ✅ Service를 통해 childId로 닉네임, 캐릭터ID 조회
    @GetMapping("/info")
    public Map<String, Object> getChildInfo(@RequestParam String childId) {
        return childService.getChildInfo(childId);
    }
}
