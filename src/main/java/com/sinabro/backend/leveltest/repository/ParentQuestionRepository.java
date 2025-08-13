package com.sinabro.backend.leveltest.repository;

import com.sinabro.backend.leveltest.entity.ParentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParentQuestionRepository extends JpaRepository<ParentQuestion, Long> {
    List<ParentQuestion> findAllByOrderByQuestionOrder();
}