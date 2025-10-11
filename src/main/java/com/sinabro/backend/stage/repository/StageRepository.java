package com.sinabro.backend.stage.repository;

import com.sinabro.backend.stage.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StageRepository extends JpaRepository<Stage, String> {
    List<Stage> findByCategoryOrderByStageIdAsc(String category);
    List<Stage> findByCategoryAndLevelOrderByStageIdAsc(String category, String level);
}
