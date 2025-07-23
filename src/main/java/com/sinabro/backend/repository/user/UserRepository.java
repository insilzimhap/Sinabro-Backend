package com.sinabro.backend.repository.user;

import com.sinabro.backend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    // userId로 찾기 (PK)
    Optional<User> findByUserId(String userId);

    // 이메일로 찾기
    Optional<User> findByUserEmail(String userEmail);

    // 소셜 타입 + 소셜 ID로 찾기 (소셜 로그인 중복 방지용)
    Optional<User> findBySocialTypeAndSocialId(String socialType, String socialId);

    // userId 존재 여부
    boolean existsByUserId(String userId);

    // 이메일 존재 여부
    boolean existsByUserEmail(String userEmail);

    // 소셜 타입 + 소셜 ID 조합 존재 여부
    boolean existsBySocialTypeAndSocialId(String socialType, String socialId);
}
