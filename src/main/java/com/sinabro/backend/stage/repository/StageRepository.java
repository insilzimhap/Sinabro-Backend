package com.sinabro.backend.stage.repository;

import com.sinabro.backend.stage.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StageRepository extends JpaRepository<Stage, String> {
    List<Stage> findByCategoryOrderByStageIdAsc(String category);
    List<Stage> findByCategoryAndLevelOrderByStageIdAsc(String category, String level);
    // ✅ enum 타입 category용 메서드 추가
    List<Stage> findByCategory(Stage.Category category);
    // StageRepository.java
    List<Stage> findByCategoryAndLevelOrderByStageIdAsc(Stage.Category category, String level);

}
