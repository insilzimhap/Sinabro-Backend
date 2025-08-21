package com.sinabro.backend.admin.user.repository;

import com.sinabro.backend.admin.user.entity.AdminChild;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminChildRepository extends JpaRepository<AdminChild, String> {
    // 부모(user_id)로 자녀 목록 조회

    List<AdminChild> findByParent_Id(String parentUserId);
    // 자녀 목록 정렬 필요 시
    List<AdminChild> findByParent_IdOrderByCreateDateDesc(String parentUserId);

}
