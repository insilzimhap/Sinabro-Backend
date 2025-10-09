package com.sinabro.backend.study.repository;

import com.sinabro.backend.study.entity.StudyListeningContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyListeningContentRepository extends JpaRepository<StudyListeningContent, String> {

    // LearningFruit와 연관관계 매핑을 걸었으니까 이렇게 사용 가능
    List<StudyListeningContent> findByFruit_FruitIdOrderByContentOrderAsc(String fruitId);
}
