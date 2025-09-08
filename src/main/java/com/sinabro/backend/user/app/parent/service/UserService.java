package com.sinabro.backend.user.app.parent.service;

import com.sinabro.backend.user.app.parent.dto.*;
import com.sinabro.backend.user.entity.ParentSetting;
import com.sinabro.backend.user.repository.ParentSettingRepository; // ✅ 추가
import com.sinabro.backend.user.entity.User;
import com.sinabro.backend.user.app.exception.DuplicateUserException;
import com.sinabro.backend.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ParentSettingRepository parentSettingRepository; // ✅ 추가
    private final PasswordEncoder passwordEncoder;

    // 🔽 나중에 켜서 사용할 화이트리스트(서비스 전역)
    // TODO: 언어팩 추가/수정 시 이 리스트 업데이트
//    private static final Set<String> SUPPORTED_LANG =
//            Set.of("Korea", "English", "Vietnamese", "Chinese");
//
//    private static boolean isSupported(String lang) {
//        return lang != null && SUPPORTED_LANG.contains(lang);
//    }

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
        log.info("[회원가입-로컬] 시작 userId={}", dto.getUserId());


        if (userRepository.existsByUserId(dto.getUserId())) {
            log.warn("[회원가입-로컬] 중복 아이디 userId={}", dto.getUserId());
            throw new DuplicateUserException("이미 존재하는 사용자입니다.");
        }
        if (dto.getUserPw() == null || dto.getUserPw().isBlank()) {
            log.warn("[회원가입-로컬] 비밀번호 누락 userId={}", dto.getUserId());
            throw new IllegalArgumentException("비밀번호가 없습니다.");
        }

        if (dto.getRole() == null || dto.getRole().isBlank()) dto.setRole("parent");
        if (dto.getUserLanguage() == null || dto.getUserLanguage().isBlank()) dto.setUserLanguage("Korea");
        // TODO: 언어 값 검증 켜려면 주석 해제
        // if (!isSupported(dto.getUserLanguage())) {
        //     throw new IllegalArgumentException("지원하지 않는 언어입니다: " + dto.getUserLanguage());
        // }

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
            log.warn("[회원가입-로컬] 개인정보 수집·이용 동의 미체크 userId={}", saved.getUserId());
            throw new IllegalArgumentException("개인정보 수집·이용에 동의해야 가입할 수 있습니다.");
        }

        ParentSetting setting = ParentSetting.builder()
                //.userId(saved.getUserId()) // PK = FK
                .parent(saved)             // @OneToOne @MapsId 매핑
                .allowNotifications(Boolean.TRUE.equals(s.getAllowNotifications()))
                .emailSubscription(Boolean.TRUE.equals(s.getEmailSubscription()))
                .privacyConsent(true)
                .build();

        parentSettingRepository.save(setting);

        log.info("[회원가입-로컬] 완료 userId={} (parent_setting 저장 완료)", saved.getUserId());
        return toDto(saved);

    }



    // 소셜 회원가입(업서트): 비밀번호는 절대 저장/덮어쓰지 않음 + settings 오면 업서트
    @Transactional
    public UserRegisterDto registerSocialUser(UserRegisterDto dto) {
        log.info("[소셜가입-업서트] 시작 userId={} socialType={}", dto.getUserId(), dto.getSocialType());


        if (dto.getRole() == null || dto.getRole().isBlank()) dto.setRole("parent");


        Optional<User> existingOpt = userRepository.findByUserId(dto.getUserId());
        User saved;

        if (existingOpt.isPresent()) {
            // ⬇️ 기존 계정 업데이트(비번은 건드리지 않음)
            User u = existingOpt.get();
            log.info("[소셜가입-업서트] 기존 사용자 업데이트 userId={}", u.getUserId());

            if (dto.getUserEmail() != null)     u.setUserEmail(dto.getUserEmail());
            if (dto.getUserName() != null)      u.setUserName(dto.getUserName());
            if (dto.getUserPhoneNum() != null)  u.setUserPhoneNum(dto.getUserPhoneNum());
            if (dto.getUserLanguage() != null && !dto.getUserLanguage().isBlank()){
                // TODO: 필요 시 언어 값 검증
                // if (!isSupported(newUser.getUserLanguage())) {
                //     throw new IllegalArgumentException("지원하지 않는 언어입니다: " + newUser.getUserLanguage());
                // }
                u.setUserLanguage(dto.getUserLanguage());
            }// 언어는 값이 왔을 때만 업데이트
            if (dto.getRole() != null)          u.setRole(dto.getRole());
            if (dto.getSocialType() != null)    u.setSocialType(dto.getSocialType());
            if (dto.getSocialId() != null)      u.setSocialId(dto.getSocialId());
            // u.setUserPw(...) 절대 X -> 정보 수정, 탈퇴 시 사용할 비밀번호 받기로 수정
            // ⬇️ 새로 받은 비밀번호가 있으면 해시 저장
            if (dto.getUserPw() != null && !dto.getUserPw().isBlank()) {
                u.setUserPw(passwordEncoder.encode(dto.getUserPw()));
                log.info("[소셜가입-업서트] 비밀번호 설정(기존 사용자) userId={}", u.getUserId());
            }

            saved = userRepository.save(u);
            log.info("[소셜가입-업서트] 업데이트 완료 userId={}", saved.getUserId());

        } else {
            log.info("[소셜가입-업서트] 신규 사용자 생성 userId={} socialType={}",
                    dto.getUserId(), dto.getSocialType());

            //신규 가입
            User newUser = toEntitySocial(dto);

            // ⬇ 소셜 추가정보에서 비밀번호를 받았다면 해시 저장
            if (dto.getUserPw() != null && !dto.getUserPw().isBlank()) {
                newUser.setUserPw(passwordEncoder.encode(dto.getUserPw()));
                log.info("[소셜가입-업서트] 비밀번호 설정(신규 사용자) userId={}", dto.getUserId());

            }

            // ✅ 신규 생성일 때만 기본 언어 보정
            if (newUser.getUserLanguage() == null || newUser.getUserLanguage().isBlank()) {
                newUser.setUserLanguage("Korea");
                log.info("[소셜가입-업서트] 언어 미지정 → Korea 기본값 적용 userId={}", dto.getUserId());
            }
            // TODO: 필요 시 언어 값 검증
            // if (!isSupported(newUser.getUserLanguage())) {
            //     throw new IllegalArgumentException("지원하지 않는 언어입니다: " + newUser.getUserLanguage());
            // }

            // 역할 기본값 보정
            if (newUser.getRole() == null || newUser.getRole().isBlank()) {
                newUser.setRole("parent");
            }
            saved = userRepository.save(newUser);
            log.info("[소셜가입-업서트] 생성 완료 userId={}", saved.getUserId());
        }

        // settings가 오면 parent_setting 업서트(없으면 건너뜀)
        if (dto.getSettings() != null) {
            upsertParentSetting(saved, dto.getSettings());
            log.info("[소셜가입-업서트] parent_setting 업서트 완료 userId={}", saved.getUserId());
        }

        return toDto(saved);
    }

    // ===== 헬퍼 =====
    private void upsertParentSetting(User user, ParentSettingSignupRequestDto s) {
        // 1) 존재 여부 확인
        ParentSetting ps = parentSettingRepository.findById(user.getUserId()).orElse(null);

        if (ps == null) {
            // 신규 생성: @MapsId 매핑 사용 → userId 세팅 X
            ps = new ParentSetting();
            ps.setParent(user); // @MapsId
            // 기본값은 false; 필요한 필드만 셋팅
            ps.setAllowNotifications(Boolean.TRUE.equals(s.getAllowNotifications()));
            ps.setEmailSubscription(Boolean.TRUE.equals(s.getEmailSubscription()));
            ps.setPrivacyConsent(Boolean.TRUE.equals(s.getPrivacyConsent()));
        }else{
            // ✅ null 아닌 값만 반영 (부분 업데이트)
            if (s.getAllowNotifications() != null) {
                ps.setAllowNotifications(s.getAllowNotifications());
            }
            if (s.getEmailSubscription() != null) {
                ps.setEmailSubscription(s.getEmailSubscription());
            }
            if (s.getPrivacyConsent() != null) { // 소셜 추가정보 단계에서도 동의 체크가 들어올 수 있으니 반영
                ps.setPrivacyConsent(s.getPrivacyConsent());
            }
        }


        parentSettingRepository.save(ps); // PK 동일하면 merge 동작
        log.info("[설정-업서트] 저장 완료 userId={} (allowNoti={}, emailSub={}, privacy={})",
                user.getUserId(), ps.isAllowNotifications(), ps.isEmailSubscription(), ps.isPrivacyConsent());
    }

    // ✅ 로그인: BCrypt 비교
    public UserRegisterDto login(String userId, String rawPw) {
        log.info("[로그인-로컬] 시도 userId={}", userId);

        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()){
            log.warn("[로그인-로컬] 존재하지 않는 아이디 userId={}", userId);
            return null;
        }

        User user = userOpt.get();

        // ⬇️ 소셜 계정이면 비밀번호가 있어도 로컬 로그인 차단
        if (user.getSocialType() != null && !"local".equalsIgnoreCase(user.getSocialType())) {
            log.warn("[로그인-로컬] 소셜 계정은 로컬 로그인 불가 userId={} socialType={}",
                    userId, user.getSocialType());
            return null;
        }

        String storedHash = user.getUserPw(); // local은 해시, social은 null
        if (storedHash == null){
            log.warn("[로그인-로컬] 저장된 비밀번호 없음 userId={}", userId);
            return null;  // 소셜 계정은 PW 로그인 불가
        }

        boolean ok = passwordEncoder.matches(rawPw, storedHash);
        if (ok) {
            log.info("[로그인-로컬] 성공 userId={}", userId);
            return toDto(user);
        } else {
            log.warn("[로그인-로컬] 실패(비밀번호 불일치) userId={}", userId);
            return null;
        }
        //return ok ? toDto(user) : null;
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