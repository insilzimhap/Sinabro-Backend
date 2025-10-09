package com.sinabro.backend.study.repository;

import com.sinabro.backend.study.entity.StudyWritingContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyWritingContentRepository extends JpaRepository<StudyWritingContent, Long> {
    List<StudyWritingContent> findByFruit_FruitIdOrderByContentOrderAsc(String fruitId);
}
