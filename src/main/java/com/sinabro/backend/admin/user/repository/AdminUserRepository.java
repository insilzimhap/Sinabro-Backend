package com.sinabro.backend.admin.user.repository;

import com.sinabro.backend.admin.user.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminUserRepository extends JpaRepository<AdminUser, String> {
    // 기본 CRUD 제공됨
    List<AdminUser> findAllByOrderByCreateDateDesc();

    // 부모 목록 검색 준비(다음 단계에서 유용)
    List<AdminUser> findByNameContainingIgnoreCaseOrderByCreateDateDesc(String name);
    List<AdminUser> findByEmailContainingIgnoreCaseOrderByCreateDateDesc(String email);
}
