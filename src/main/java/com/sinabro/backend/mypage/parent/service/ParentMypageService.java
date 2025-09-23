package com.sinabro.backend.mypage.parent.service;

import com.sinabro.backend.inquiry.entity.Inquiry;
import com.sinabro.backend.inquiry.repository.InquiryReplyRepository;
import com.sinabro.backend.inquiry.repository.InquiryRepository;
import com.sinabro.backend.mypage.parent.dto.*;
import com.sinabro.backend.user.entity.Child;
import com.sinabro.backend.user.entity.ParentSetting;
import com.sinabro.backend.user.entity.User;
import com.sinabro.backend.user.repository.CharacterSelectionRepository;
import com.sinabro.backend.user.repository.ChildRepository;
import com.sinabro.backend.user.repository.ParentSettingRepository;
import com.sinabro.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ParentMypageService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChildRepository childRepository;                     // ✅ 추가
    private final CharacterSelectionRepository characterRepo;          // ✅ 추가
    private final InquiryRepository inquiryRepository; // ✅ 주입
    private final InquiryReplyRepository inquiryReplyRepository;
    private final ParentSettingRepository parentSettingRepository;

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

    /** [1단계] 부모 탈퇴 사전 검증: parent 계정 + 비밀번호 확인 */
    public void verifyParentDeleteAuth(String userId, String currentPassword) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // role 보호: parent만 이 경로로 탈퇴 가능
        if (u.getRole() != null && !"parent".equalsIgnoreCase(u.getRole())) {
            throw new ResponseStatusException(FORBIDDEN, "부모가 아닌 계정은 이 경로로 탈퇴할 수 없습니다.");
        }

        if (u.getUserPw() == null || !matches(u.getUserPw(), currentPassword)) {
            throw new ResponseStatusException(UNAUTHORIZED, "현재 비밀번호가 올바르지 않습니다.");
        }
        log.info("[ParentDeleteVerify] success userId={}", userId);
    }

    /** [2단계] 부모 탈퇴: 검증 재수행 → 자녀 및 연관 정리 → 사용자 삭제 */
    @Transactional
    public void deleteParent(String userId, ParentDeleteRequestDto req) {
        // 1) 재검증(위변조 방지)
        verifyParentDeleteAuth(userId, req.getCurrentPassword());

        // 1) 자녀 및 연관 정리
        var children = childRepository.findAllByParent_UserId(userId);
        for (var c : children) {
            characterRepo.deleteByChildId(c.getChildId());
            childRepository.deleteById(c.getChildId());
        }

        // 2) 부모가 쓴 문의 목록
        var inquiries = inquiryRepository.findByParent_UserIdOrderByCreatedAtDesc(userId);
        var inquiryIds = inquiries.stream().map(Inquiry::getId /* 또는 getInquiryId */).toList();

        // 3) reply 선삭제 (직접 쓴 답글 + 문의에 달린 모든 답글)
        inquiryReplyRepository.deleteByUser_UserId(userId);
        if (!inquiryIds.isEmpty()) {
            inquiryReplyRepository.deleteByInquiry_IdIn(inquiryIds);
        }

        // 4) 문의 삭제
        inquiryRepository.deleteByParent_UserId(userId);

        // 5) 사용자 삭제 (parent_setting 은 DB CASCADE라면 자동 정리)
        userRepository.deleteById(userId);
    }

    /** 설정 프리필: parent_setting + user.user_language */
    public ParentSettingResponseDto getSettings(String userId) {
        log.info("[설정-프리필] 시작 userId={}", userId);

        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "사용자를 찾을 수 없습니다."));
        ParentSetting ps = parentSettingRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "부모 설정을 찾을 수 없습니다."));

        log.info("[설정-프리필] 로드 완료 userId={}", userId);

        return ParentSettingResponseDto.builder()
                .allowNotifications(ps.isAllowNotifications())
                .emailSubscription(ps.isEmailSubscription())
                .userLanguage(u.getUserLanguage())
                .build();
    }

    /**
     * 설정 저장(부분 업데이트)
     * - 대상: allowNotifications, emailSubscription, userLanguage
     * - privacy_consent 는 화면에서 다루지 않으므로 무시
     */
    @Transactional
    public ParentSettingResponseDto updateSettings(String userId, ParentSettingUpdateRequestDto req) {
        log.info("[설정-저장] 시작 userId={}", userId);

        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "사용자를 찾을 수 없습니다."));
        ParentSetting ps = parentSettingRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "부모 설정을 찾을 수 없습니다."));

        // --- 체크박스 (값이 왔을 때만 반영) ---
        if (req.getAllowNotifications() != null) {
            ps.setAllowNotifications(req.getAllowNotifications());
        }
        if (req.getEmailSubscription() != null) {
            ps.setEmailSubscription(req.getEmailSubscription());
        }

        // --- 언어 (값이 왔을 때만 반영) ---
        if (req.getUserLanguage() != null && !req.getUserLanguage().isBlank()) {
            u.setUserLanguage(req.getUserLanguage());
        }

        parentSettingRepository.save(ps);
        userRepository.save(u);

        log.info("[설정-저장] 완료 userId={}", userId);

        return ParentSettingResponseDto.builder()
                .allowNotifications(ps.isAllowNotifications())
                .emailSubscription(ps.isEmailSubscription())
                .userLanguage(u.getUserLanguage())
                .build();
    }

}
