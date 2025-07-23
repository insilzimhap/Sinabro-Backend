package com.sinabro.backend.service;

import com.sinabro.backend.dto.user.ChildRegisterDto;
import com.sinabro.backend.entity.user.Child;
import com.sinabro.backend.entity.user.User;
import com.sinabro.backend.entity.user.CharacterSelection;
import com.sinabro.backend.repository.user.ChildRepository;
import com.sinabro.backend.repository.user.UserRepository;
import com.sinabro.backend.repository.user.CharacterSelectionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ChildService {

    private final ChildRepository childRepository;
    private final UserRepository userRepository;
    private final CharacterSelectionRepository characterSelectionRepository; // 추가

    public ChildService(
            ChildRepository childRepository,
            UserRepository userRepository,
            CharacterSelectionRepository characterSelectionRepository // 추가
    ) {
        this.childRepository = childRepository;
        this.userRepository = userRepository;
        this.characterSelectionRepository = characterSelectionRepository; // 추가
    }

    @Transactional
    public ChildRegisterDto registerChild(ChildRegisterDto dto) {
        User parent = userRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("부모 계정이 존재하지 않습니다."));

        Child child = Child.builder()
                .childId(dto.getChildId())
                .childName(dto.getChildName())
                .childNickName(dto.getChildNickName())
                .childBirth(dto.getChildBirth())
                .childAge(dto.getChildAge())
                .childPw(dto.getChildPw())
                .childLevel(dto.getChildLevel())
                .timeLimitMinutes(dto.getTimeLimitMinutes() == null ? 0 : dto.getTimeLimitMinutes())
                .role("child")
                .parent(parent)
                .build();

        childRepository.save(child);

        ChildRegisterDto result = new ChildRegisterDto();
        result.setChildId(child.getChildId());
        result.setChildName(child.getChildName());
        result.setChildNickName(child.getChildNickName());
        result.setChildBirth(child.getChildBirth());
        result.setChildAge(child.getChildAge());
        result.setChildPw(child.getChildPw());
        result.setChildLevel(child.getChildLevel());
        result.setTimeLimitMinutes(child.getTimeLimitMinutes());
        result.setRole(child.getRole());
        result.setUserId(parent.getUserId());
        return result;
    }

    // 로그인
    public boolean loginChild(String childId, String childPw) {
        Optional<Child> childOpt = childRepository.findById(childId);
        return childOpt.isPresent() && childOpt.get().getChildPw().equals(childPw);
    }

    // ✅ childId로 닉네임과 캐릭터ID 내려주는 메서드 추가
    public Map<String, Object> getChildInfo(String childId) {
        Map<String, Object> result = new HashMap<>();
        Child child = childRepository.findByChildId(childId).orElse(null);
        CharacterSelection selection = characterSelectionRepository.findByChildId(childId).orElse(null);

        result.put("nickname", child != null ? child.getChildNickName() : null);
        result.put("characterId", selection != null ? selection.getCharacterId() : null);
        return result;
    }
}
