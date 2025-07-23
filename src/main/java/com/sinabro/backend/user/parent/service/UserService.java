package com.sinabro.backend.user.parent.service;

import com.sinabro.backend.user.parent.dto.UserRegisterDto;
import com.sinabro.backend.user.parent.entity.User;
import com.sinabro.backend.user.exception.DuplicateUserException;
import com.sinabro.backend.user.parent.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 일반 회원가입
    public UserRegisterDto registerUser(UserRegisterDto dto) {
        // userId 중복 체크
        if (userRepository.existsByUserId(dto.getUserId())) {
            throw new DuplicateUserException("이미 존재하는 사용자입니다.");
        }


        // 엔티티로 변환
        User user = toEntity(dto);

        // 저장
        User saved = userRepository.save(user);

        // DTO로 변환해서 반환
        return toDto(saved);
    }

    // 소셜 회원가입
    public UserRegisterDto registerSocialUser(UserRegisterDto dto) {
        // 소셜 ID 중복 체크 (userEmail + socialType 조합 등으로 체크 가능)
        if (userRepository.existsByUserId(dto.getUserId())) {
            Optional<User> existing = userRepository.findByUserId(dto.getUserId());
            return existing.map(this::toDto).orElse(null);
        }

        User user = toEntity(dto);
        User saved = userRepository.save(user);
        return toDto(saved);
    }

    // 로그인
    public UserRegisterDto login(String userId, String userPw) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isPresent() && userOpt.get().getUserPw().equals(userPw)) {
            return toDto(userOpt.get());
        }
        return null;
    }

    // 엔티티 → DTO 변환
    private UserRegisterDto toDto(User user) {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setUserId(user.getUserId());
        dto.setUserEmail(user.getUserEmail());
        dto.setUserPw(user.getUserPw());
        dto.setUserName(user.getUserName());
        dto.setUserPhoneNum(user.getUserPhoneNum());
        dto.setUserLanguage(user.getUserLanguage());
        dto.setRole(user.getRole());
        dto.setSocialType(user.getSocialType());
        dto.setSocialId(user.getSocialId());
        return dto;
    }

    // DTO → 엔티티 변환
    private User toEntity(UserRegisterDto dto) {
        return User.builder()
                .userId(dto.getUserId())
                .userEmail(dto.getUserEmail())
                .userPw(dto.getUserPw())
                .userName(dto.getUserName())
                .userPhoneNum(dto.getUserPhoneNum())
                .userLanguage(dto.getUserLanguage())
                .role(dto.getRole())
                .socialType(dto.getSocialType())
                .socialId(dto.getSocialId())
                .build();
    }
}
