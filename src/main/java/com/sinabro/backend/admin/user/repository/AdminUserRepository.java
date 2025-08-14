package com.sinabro.backend.admin.user.repository;

import com.sinabro.backend.admin.user.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser, String> {
    // 기본 CRUD 제공됨
}
