package com.sinabro.backend.game.listening.repository;

import com.sinabro.backend.game.listening.entity.ListeningGameQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListeningGameQuestionRepository extends JpaRepository<ListeningGameQuestion, String> {
    // 프론트에서 게임 시작 시, 특정 열매에 속한 모든 문제를 순서대로 불러오기 위한 메서드
    List<ListeningGameQuestion> findByFruitIdOrderByLgContentOrderAsc(String fruitId);
    // 한 열매 당 문제 개수
    long countByFruitId(String fruitId);

}