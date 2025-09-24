package com.sinabro.backend.user.repository;

import com.sinabro.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;


/**
 * [공용 레포] User 엔티티
 * - 관리자/앱 공통 사용
 * - 목록 정렬/검색용 스펙 인터페이스 포함
 */
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

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

    // admin - 전체 부모 (사용자) 목록 최신순
    List<User> findAllByOrderByUserCreateDateDesc();
}
