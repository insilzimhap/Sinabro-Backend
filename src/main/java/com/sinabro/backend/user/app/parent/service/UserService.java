package com.sinabro.backend.user.app.parent.service;

import com.sinabro.backend.user.app.parent.dto.*;
import com.sinabro.backend.user.entity.ParentSetting;
import com.sinabro.backend.user.repository.ParentSettingRepository; // ✅ 추가
import com.sinabro.backend.user.entity.User;
import com.sinabro.backend.user.app.exception.DuplicateUserException;
import com.sinabro.backend.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ParentSettingRepository parentSettingRepository; // ✅ 추가
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       ParentSettingRepository parentSettingRepository, // ✅ 추가
                       PasswordEncoder passwordEncoder) {        // ⬅️ 주입
        this.userRepository = userRepository;
        this.parentSettingRepository = parentSettingRepository;        // ✅ 추가
        this.passwordEncoder = passwordEncoder;
    }

    // 일반 회원가입 (패스워드 필수)
    @Transactional
    public UserRegisterDto registerUser(UserRegisterDto dto) {
        if (userRepository.existsByUserId(dto.getUserId())) {
            throw new DuplicateUserException("이미 존재하는 사용자입니다.");
        }
        if (dto.getUserPw() == null || dto.getUserPw().isBlank()) {
            throw new IllegalArgumentException("비밀번호가 없습니다.");
        }
        if (dto.getRole() == null || dto.getRole().isBlank()) dto.setRole("parent");
        if (dto.getUserLanguage() == null || dto.getUserLanguage().isBlank()) dto.setUserLanguage("Korea");
        dto.setSocialType("local");

        // ⬇️ 비밀번호 해시
        String hashed = passwordEncoder.encode(dto.getUserPw());

        // 1) User 저장
        User user = User.builder()
                .userId(dto.getUserId())
                .userEmail(dto.getUserEmail())
                .userPw(hashed)                            // ⬅️ 해시 저장
                .userName(dto.getUserName())
                .userPhoneNum(dto.getUserPhoneNum())
                .userLanguage(dto.getUserLanguage())
                .role(dto.getRole())
                .socialType("local")
                .socialId(null)
                .build();

        User saved = userRepository.save(user);

        // 2) parent_setting 저장 (개인정보 동의는 반드시 ture)
        ParentSettingSignupRequestDto s = dto.getSettings(); // ✅ DTO 안에 settings 있다고 했으니 그대로 사용
        if (s == null || Boolean.TRUE != s.getPrivacyConsent()) {
            throw new IllegalArgumentException("개인정보 수집·이용에 동의해야 가입할 수 있습니다.");
        }

        ParentSetting setting = ParentSetting.builder()
                .userId(saved.getUserId()) // PK = FK
                .parent(saved)             // @OneToOne @MapsId 매핑
                .allowNotifications(Boolean.TRUE.equals(s.getAllowNotifications()))
                .emailSubscription(Boolean.TRUE.equals(s.getEmailSubscription()))
                .privacyConsent(true)
                .build();

        parentSettingRepository.save(setting);

        return toDto(saved);

    }



    // 소셜 회원가입(업서트): 비밀번호는 절대 저장/덮어쓰지 않음 + settings 오면 업서트
    @Transactional
    public UserRegisterDto registerSocialUser(UserRegisterDto dto) {
        if (dto.getRole() == null || dto.getRole().isBlank()) dto.setRole("parent");
        if (dto.getUserLanguage() == null || dto.getUserLanguage().isBlank()) dto.setUserLanguage("Korea");

        Optional<User> existingOpt = userRepository.findByUserId(dto.getUserId());
        User saved;

        if (existingOpt.isPresent()) {
            // ⬇️ 기존 계정 업데이트(비번은 건드리지 않음)
            User u = existingOpt.get();
            if (dto.getUserEmail() != null)     u.setUserEmail(dto.getUserEmail());
            if (dto.getUserName() != null)      u.setUserName(dto.getUserName());
            if (dto.getUserPhoneNum() != null)  u.setUserPhoneNum(dto.getUserPhoneNum());
            if (dto.getUserLanguage() != null)  u.setUserLanguage(dto.getUserLanguage());
            if (dto.getRole() != null)          u.setRole(dto.getRole());
            if (dto.getSocialType() != null)    u.setSocialType(dto.getSocialType());
            if (dto.getSocialId() != null)      u.setSocialId(dto.getSocialId());
            // u.setUserPw(...) 절대 X
            saved = userRepository.save(u);
        } else {
            // ⬇️ 신규 소셜 가입: 비밀번호는 NULL 로 저장
            saved = userRepository.save(toEntitySocial(dto));
        }

        // settings가 오면 parent_setting 업서트(없으면 건너뜀)
        if (dto.getSettings() != null) {
            upsertParentSetting(saved, dto.getSettings());
        }

        return toDto(saved);
    }

    // ===== 헬퍼 =====
    private void upsertParentSetting(User user, ParentSettingSignupRequestDto s) {
        ParentSetting setting = ParentSetting.builder()
                .userId(user.getUserId())
                .parent(user)
                .allowNotifications(Boolean.TRUE.equals(s.getAllowNotifications()))
                .emailSubscription(Boolean.TRUE.equals(s.getEmailSubscription()))
                .privacyConsent(Boolean.TRUE.equals(s.getPrivacyConsent()))
                .build();
        parentSettingRepository.save(setting); // PK 동일하면 merge 동작
    }

    // ✅ 로그인: BCrypt 비교
    public UserRegisterDto login(String userId, String rawPw) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) return null;

        User user = userOpt.get();
        String storedHash = user.getUserPw(); // local은 해시, social은 null
        if (storedHash == null) return null;  // 소셜 계정은 PW 로그인 불가

        boolean ok = passwordEncoder.matches(rawPw, storedHash);
        return ok ? toDto(user) : null;
    }

    private User toEntitySocial(UserRegisterDto dto) {
        return User.builder()
                .userId(dto.getUserId())
                .userEmail(dto.getUserEmail())
                .userPw(null)                    // ⬅️ 핵심: 소셜은 NULL
                .userName(dto.getUserName())
                .userPhoneNum(dto.getUserPhoneNum())
                .userLanguage(dto.getUserLanguage())
                .role(dto.getRole())
                .socialType(dto.getSocialType())
                .socialId(dto.getSocialId())
                .build();
    }

    private UserRegisterDto toDto(User user) {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setUserId(user.getUserId());
        dto.setUserEmail(user.getUserEmail());
        dto.setUserPw(null);                    // ⬅️ 절대 비번/해시 노출 금지
        dto.setUserName(user.getUserName());
        dto.setUserPhoneNum(user.getUserPhoneNum());
        dto.setUserLanguage(user.getUserLanguage());
        dto.setRole(user.getRole());
        dto.setSocialType(user.getSocialType());
        dto.setSocialId(user.getSocialId());
        return dto;
    }

    public boolean isUserIdAvailable(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId가 비어있습니다.");
        }
        // UserRepository 가 JpaRepository<User, String> 이면 existsById 사용 가능
        return !userRepository.existsById(userId);
    }
}