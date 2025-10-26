package com.sinabro.backend.game.writing.repository;

import com.sinabro.backend.game.writing.entity.WritingGameQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * ✍️ [쓰기 게임 문제 레포지토리]
 * - 특정 열매(fruitId)에 속한 문제 조회
 * - 랜덤 출제용 쿼리 포함
 */
@Repository
public interface WritingGameQuestionRepository extends JpaRepository<WritingGameQuestion, String> {

    /**
     * [랜덤 출제용 쿼리]
     * 특정 열매(fruitId)에 속한 문제 중 일부를 무작위로 반환합니다.
     *
     * @param fruitId 열매 ID (LearningFruit.fruit_id)
     * @param limit   반환할 문제 개수
     * @return 랜덤으로 선택된 문제 리스트
     */
    @Query(value = """
        SELECT * FROM writing_game_question
        WHERE fruit_id = :fruitId
        ORDER BY RAND()
        LIMIT :limit
        """, nativeQuery = true)
    List<WritingGameQuestion> findRandomQuestionsByFruitId(
            @Param("fruitId") String fruitId,
            @Param("limit") int limit
    );

    /**
     * [단순 전체 조회]
     * 열매별로 등록된 전체 문제 반환 (랜덤 아님)
     */
    List<WritingGameQuestion> findByFruitId(String fruitId);
}
