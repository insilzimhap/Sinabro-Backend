package com.sinabro.backend.mypage.child.service;

import com.sinabro.backend.mypage.child.dto.*;
import com.sinabro.backend.user.entity.Child;
import com.sinabro.backend.user.entity.User;
import com.sinabro.backend.user.repository.CharacterSelectionRepository;
import com.sinabro.backend.user.repository.ChildRepository;
import com.sinabro.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;


import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChildMyPageService {

    private final ChildRepository childRepository;
    private final UserRepository userRepository;
    private final CharacterSelectionRepository characterSelectionRepository;
    private final PasswordEncoder passwordEncoder;

    /** 자녀 프로필 프리필 */
    public ChildProfileResponseDto getChildProfile(String childId) {
        Child c = childRepository.findById(childId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "child not found: " + childId));
        log.info("[Child-Profile] load childId={}", childId);
        return toProfileDto(c);
    }

    /** 자녀 프로필 수정 (닉네임/생일/비번-선택) */
    @Transactional
    public ChildProfileResponseDto updateChild(String childId, ChildUpdateRequestDto req) {
        Child c = childRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("child not found: " + childId));

        // 닉네임
        if (StringUtils.hasText(req.getChildNickname())) {
            c.setChildNickname(req.getChildNickname());
        }

        // 생일 (String 그대로 저장하는 설계 유지)
        if (StringUtils.hasText(req.getChildBirth())) {
            c.setChildBirth(req.getChildBirth());
        }

        // 비밀번호 변경 로직 (둘 다 비어있으면 미변경)
        boolean hasNew = StringUtils.hasText(req.getNewPassword());
        boolean hasConfirm = StringUtils.hasText(req.getNewPasswordConfirm());
        if (hasNew || hasConfirm) {
            if (!(hasNew && hasConfirm)) {
                throw new IllegalArgumentException("새 비밀번호와 재입력은 함께 입력해야 합니다.");
            }
            if (!req.getNewPassword().equals(req.getNewPasswordConfirm())) {
                throw new IllegalArgumentException("새 비밀번호와 재입력이 일치하지 않습니다.");
            }
            c.setChildPw(passwordEncoder.encode(req.getNewPassword()));
        }

        // 변경 감지로 업데이트
        Child saved = childRepository.save(c);
        return toProfileDto(saved);
    }

    /**
     * [1단계] 자녀 삭제 권한 확인(소유권 + 부모 비밀번호)
     * - 삭제하지 않고 검증만 수행
     * - 성공 시 예외 없이 리턴
     * - 실패 시 401/404 던짐
     */
    public void verifyChildDeleteAuth(String parentUserId, String childId, String parentPassword) {
        // 소유권/대상 확인 (없거나 남의 자녀면 404로 통일)
        if (!childRepository.existsByChildIdAndParent_UserId(childId, parentUserId)) {
            throw new ResponseStatusException(NOT_FOUND, "자녀를 찾을 수 없거나 소유하지 않은 계정입니다.");
        }

        // 부모 계정/비밀번호 확인
        User parent = userRepository.findById(parentUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "부모 계정을 찾을 수 없습니다."));

        if (!passwordEncoder.matches(parentPassword, parent.getUserPw())) {
            throw new ResponseStatusException(UNAUTHORIZED, "부모 비밀번호가 올바르지 않습니다.");
        }
        // OK → 아무것도 하지 않고 리턴
    }

    /**
     * [2단계] 자녀 삭제 (보호 로직 그대로 유지)
     * - 안전을 위해 여기서도 같은 검증을 한 번 더 수행(아이디/비번 위변조 방지)
     */
    @Transactional
    public void deleteChild(String parentUserId, String childId, ChildDeleteRequestDto req) {
        // 1) 검증 재확인
        verifyChildDeleteAuth(parentUserId, childId, req.getParentPassword());

        // 2) 연관 데이터 정리 → 자녀 삭제
        characterSelectionRepository.deleteByChildId(childId);
        childRepository.deleteById(childId);
    }

    private ChildProfileResponseDto toProfileDto(Child c) {
        return ChildProfileResponseDto.builder()
                .childId(c.getChildId())
                .childName(c.getChildName())
                .childNickname(c.getChildNickname())
                .childBirth(c.getChildBirth())
                .build();
    }
}
