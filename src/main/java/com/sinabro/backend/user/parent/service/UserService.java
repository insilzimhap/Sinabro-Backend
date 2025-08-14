package com.sinabro.backend.user.parent.service;

import com.sinabro.backend.user.parent.dto.UserRegisterDto;
import com.sinabro.backend.user.parent.entity.User;
import com.sinabro.backend.user.exception.DuplicateUserException;
import com.sinabro.backend.user.parent.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 일반 회원가입
    public UserRegisterDto registerUser(UserRegisterDto dto) {
        if (userRepository.existsByUserId(dto.getUserId())) {
            throw new DuplicateUserException("이미 존재하는 사용자입니다.");
        }
        User saved = userRepository.save(toEntity(dto));
        return toDto(saved);
    }

    // ✅ 소셜 회원가입/업데이트 (업서트)
    @Transactional
    public UserRegisterDto registerSocialUser(UserRegisterDto dto) {
        Optional<User> existingOpt = userRepository.findByUserId(dto.getUserId());

        if (existingOpt.isPresent()) {
            // 이미 존재 → 필요한 필드 업데이트
            User u = existingOpt.get();

            // 이메일: 비어있거나(또는 새 값이 들어왔을 때) 갱신
            if (dto.getUserEmail() != null && !dto.getUserEmail().isBlank()) {
                u.setUserEmail(dto.getUserEmail());
            }

            // 이름
            if (dto.getUserName() != null && !dto.getUserName().isBlank()) {
                u.setUserName(dto.getUserName());
            }

            // ✅ 전화번호: 추가정보 페이지에서 받은 값 저장
            if (dto.getUserPhoneNum() != null && !dto.getUserPhoneNum().isBlank()) {
                u.setUserPhoneNum(dto.getUserPhoneNum());
            }

            // 소셜 식별자/타입(초기 가입 때 못들어간 경우 보정)
            if (u.getSocialType() == null || u.getSocialType().isBlank()) {
                u.setSocialType(dto.getSocialType());
            }
            if (u.getSocialId() == null || u.getSocialId().isBlank()) {
                u.setSocialId(dto.getSocialId());
            }

            // 비밀번호(처음 소셜가입 시 임시 저장 용)
            if ((u.getUserPw() == null || u.getUserPw().isBlank()) &&
                    dto.getUserPw() != null && !dto.getUserPw().isBlank()) {
                u.setUserPw(dto.getUserPw());
            }

            // 역할 기본값 보정
            if (u.getRole() == null || u.getRole().isBlank()) {
                u.setRole(dto.getRole() != null ? dto.getRole() : "parent");
            }

            User saved = userRepository.save(u);
            return toDto(saved);
        } else {
            // 신규 소셜가입
            User user = toEntity(dto);
            User saved = userRepository.save(user);
            return toDto(saved);
        }
    }

    // 로그인
    public UserRegisterDto login(String userId, String userPw) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isPresent() && userOpt.get().getUserPw().equals(userPw)) {
            return toDto(userOpt.get());
        }
        return null;
    }

    // 엔티티 → DTO
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

    // DTO → 엔티티
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
