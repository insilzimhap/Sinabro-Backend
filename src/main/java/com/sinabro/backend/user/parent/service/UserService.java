package com.sinabro.backend.user.parent.service;

import com.sinabro.backend.user.parent.dto.UserRegisterDto;
import com.sinabro.backend.user.parent.entity.User;
import com.sinabro.backend.user.exception.DuplicateUserException;
import com.sinabro.backend.user.parent.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {        // ⬅️ 주입
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 일반 회원가입 (패스워드 필수)
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

        // ⬇️ 여기서 해시
        String hashed = passwordEncoder.encode(dto.getUserPw());

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
        return toDto(saved);
    }

    // 소셜 회원가입(업서트): 비밀번호는 절대 저장/덮어쓰지 않음
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

        return toDto(saved);
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
}
