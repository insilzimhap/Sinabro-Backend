package com.sinabro.backend.mypage.parent.service;

import com.sinabro.backend.mypage.parent.dto.*;
import com.sinabro.backend.user.entity.User;
import com.sinabro.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ParentMypageService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** 프로필 조회 (마이페이지 프리필용) */
    public ParentProfileResponseDto getProfile(String userId) {
        log.info("[ParentProfile] load userId={}", userId);

        User u = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[ParentProfile] not-found userId={}", userId);
                    return new ResponseStatusException(NOT_FOUND, "사용자를 찾을 수 없습니다.");
                });

        return ParentProfileResponseDto.builder()
                .userId(u.getUserId())
                .userName(u.getUserName())
                .userEmail(u.getUserEmail())
                .userPhoneNum(u.getUserPhoneNum())
                .build();
    }

    /** 마이페이지 진입 전: 현재 비밀번호 검증 */
    public void verifyPassword(String userId, PasswordVerifyRequestDto req) {
        log.info("[ParentPasswordVerify] userId={}", userId);

        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (!matches(u.getUserPw(), req.getCurrentPassword())) {
            log.warn("[ParentPasswordVerify] mismatch userId={}", userId);
            throw new ResponseStatusException(UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        log.info("[ParentPasswordVerify] success userId={}", userId);
    }

    /** 프로필 수정 (이메일/전화번호 + 선택적 비밀번호 변경) */
    @Transactional
    public ParentProfileResponseDto updateProfile(String userId, ParentUpdateRequestDto req) {
        log.info("[ParentProfileUpdate] userId={} email={} phone={}", userId, req.getUserEmail(), req.getUserPhoneNum());

        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 1) 이메일 변경 시 중복 체크 (본인 이메일과 다른 값으로 바꿀 때만)
        if (!eq(u.getUserEmail(), req.getUserEmail())
                && userRepository.existsByUserEmail(req.getUserEmail())) {
            log.warn("[ParentProfileUpdate] email-dup userId={} email={}", userId, req.getUserEmail());
            throw new ResponseStatusException(CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        // 2) 이메일/전화번호 반영
        u.setUserEmail(req.getUserEmail());
        u.setUserPhoneNum(req.getUserPhoneNum());

        // 3) 비밀번호 변경(옵션)
        String npw = blankToNull(req.getNewPassword());
        String npw2 = blankToNull(req.getNewPasswordConfirm());

        if (npw != null || npw2 != null) {
            // 둘 다 채워져야 함
            if (npw == null || npw2 == null) {
                log.warn("[ParentProfileUpdate] password one-side filled userId={}", userId);
                throw new ResponseStatusException(BAD_REQUEST, "새 비밀번호와 재입력을 모두 입력하세요.");
            }
            if (!npw.equals(npw2)) {
                log.warn("[ParentProfileUpdate] password mismatch userId={}", userId);
                throw new ResponseStatusException(BAD_REQUEST, "새 비밀번호가 서로 일치하지 않습니다.");
            }
            // 기존과 동일하면 무시(변경 없음)
            if (matches(u.getUserPw(), npw)) {
                log.debug("[ParentProfileUpdate] password unchanged(same as current) userId={}", userId);
            } else {
                u.setUserPw(passwordEncoder.encode(npw));
                log.debug("[ParentProfileUpdate] password changed userId={}", userId);
            }
        }

        // 4) 저장
        userRepository.save(u);
        log.info("[ParentProfileUpdate] success userId={}", userId);

        // 변경 결과 리턴(프리필 갱신)
        return ParentProfileResponseDto.builder()
                .userId(u.getUserId())
                .userName(u.getUserName())
                .userEmail(u.getUserEmail())
                .userPhoneNum(u.getUserPhoneNum())
                .build();
    }

    // --- 내부 유틸 ---

    /** encoded/평문 모두 대응해서 비교 */
    private boolean matches(String encodedOrPlain, String raw) {
        if (encodedOrPlain == null || raw == null) return false;
        try {
            return passwordEncoder.matches(raw, encodedOrPlain);
        } catch (IllegalArgumentException e) {
            // encoded 포맷이 아니면 평문 비교
            return encodedOrPlain.equals(raw);
        }
    }

    private boolean eq(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
