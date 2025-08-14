package com.sinabro.backend.leveltest.repository;

import com.sinabro.backend.leveltest.entity.ParentChoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParentChoiceRepository extends JpaRepository<ParentChoice, Long> {
    // 특정 자녀의 부모 응답 전체 조회
    List<ParentChoice> findByChildId(String childId);
}
