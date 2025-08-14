package com.sinabro.backend.admin.notice.repository;

import com.sinabro.backend.admin.notice.entity.AdminNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminNoticeRepository extends JpaRepository<AdminNotice, Long> {
    // 기본 CRUD 자동 제공됨
}
