package com.sinabro.backend.leveltest.repository;

import com.sinabro.backend.leveltest.entity.LevelTestChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface LevelTestChoiceRepository extends JpaRepository<LevelTestChoice, Long> {
    // 특정 자녀의 답안 전체 조회
    List<LevelTestChoice> findByChildId(String childId);

    @Modifying
    void deleteByChildId(String childId);

}
