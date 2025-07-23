package com.sinabro.backend.leveltest.repository;

import com.sinabro.backend.leveltest.entity.LevelTestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LevelTestQuestionRepository extends JpaRepository<LevelTestQuestion, Long> {
}