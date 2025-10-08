package com.sinabro.backend.game.repository;

import com.sinabro.backend.game.entity.ListeningGameOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListeningGameOptionRepository extends JpaRepository<ListeningGameOption, String> {
    // 특정 문제에 속한 모든 보기를 불러오기 위한 메서드
    List<ListeningGameOption> findByLgQuestionIdOrderByDisplayIndexAsc(String questionId);
}