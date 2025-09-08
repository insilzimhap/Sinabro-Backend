package com.sinabro.backend.study.repository;

import com.sinabro.backend.study.entity.StudyListeningContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyListeningContentRepository extends JpaRepository<StudyListeningContent, String> {
    List<StudyListeningContent> findByStage_StageIdOrderByLsContentOrderAsc(String stageId);
}
