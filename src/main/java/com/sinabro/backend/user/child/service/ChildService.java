package com.sinabro.backend.user.child.service;

import com.sinabro.backend.user.child.dto.ChildRegisterDto;
import com.sinabro.backend.user.child.entity.Child;
import com.sinabro.backend.user.parent.entity.User;
import com.sinabro.backend.user.child.entity.CharacterSelection;
import com.sinabro.backend.user.child.repository.ChildRepository;
import com.sinabro.backend.user.parent.repository.UserRepository;
import com.sinabro.backend.user.child.repository.CharacterSelectionRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ChildService {

    private final ChildRepository childRepository;
    private final UserRepository userRepository;
    private final CharacterSelectionRepository characterSelectionRepository;
    private final PasswordEncoder passwordEncoder;

    public ChildService(
            ChildRepository childRepository,
            UserRepository userRepository,
            CharacterSelectionRepository characterSelectionRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.childRepository = childRepository;
        this.userRepository = userRepository;
        this.characterSelectionRepository = characterSelectionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ChildRegisterDto registerChild(ChildRegisterDto dto) {
        User parent = userRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("부모 계정이 존재하지 않습니다."));

        // ★ 평문 자녀 비밀번호 → 해시
        String encodedPw = passwordEncoder.encode(dto.getChildPw());

        Child child = Child.builder()
                .childId(dto.getChildId())
                .childName(dto.getChildName())
                .childNickname(dto.getChildNickname())   // DTO 필드 childNickname 사용
                .childBirth(dto.getChildBirth())
                .childAge(dto.getChildAge())
                .childPw(encodedPw)
                .childLevel(dto.getChildLevel())         // 초기 null 허용
                .timeLimitMinutes(dto.getTimeLimitMinutes() == null ? 0 : dto.getTimeLimitMinutes())
                .role("child")
                .parent(parent)
                .build();

        childRepository.save(child);

        ChildRegisterDto result = new ChildRegisterDto();
        result.setChildId(child.getChildId());
        result.setChildName(child.getChildName());
        result.setChildNickname(child.getChildNickname());
        result.setChildBirth(child.getChildBirth());
        result.setChildAge(child.getChildAge());
        // ★ 응답에 비밀번호(해시) 주지 않음
        result.setChildPw(null);
        result.setChildLevel(child.getChildLevel());
        result.setTimeLimitMinutes(child.getTimeLimitMinutes());
        result.setRole(child.getRole());
        result.setUserId(parent.getUserId());
        return result;
    }

    public boolean loginChild(String childId, String childPw) {
        Optional<Child> childOpt = childRepository.findById(childId);
        if (childOpt.isEmpty()) return false;
        Child child = childOpt.get();
        // ★ 평문 vs 해시 매칭
        return passwordEncoder.matches(childPw, child.getChildPw());
    }

    // childId로 닉네임/캐릭터/레벨 조회
    public Map<String, Object> getChildInfo(String childId) {
        Map<String, Object> result = new HashMap<>();

        Child child = childRepository.findByChildId(childId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유아를 찾을 수 없습니다."));

        Optional<CharacterSelection> selection = characterSelectionRepository.findByChildId(childId);

        result.put("nickname", child.getChildNickname());
        result.put("characterId", selection.map(CharacterSelection::getCharacterId).orElse(null));
        String level = child.getChildLevel() == null ? "-" : child.getChildLevel().toString();
        result.put("level", level);
        return result;
    }
}
