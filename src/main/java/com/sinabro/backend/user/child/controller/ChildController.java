package com.sinabro.backend.user.child.controller;

import com.sinabro.backend.user.child.dto.ChildRegisterDto;
import com.sinabro.backend.user.child.entity.Child;
import com.sinabro.backend.user.child.repository.ChildRepository;
import com.sinabro.backend.user.child.service.ChildService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/child")
public class ChildController {

    private final ChildService childService;
    private final ChildRepository childRepository; // ✅ level 추가 위해 주입

    public ChildController(ChildService childService, ChildRepository childRepository) {
        this.childService = childService;
        this.childRepository = childRepository;
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
