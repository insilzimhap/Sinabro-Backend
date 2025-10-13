package com.sinabro.backend.game.writing.repository;

import com.sinabro.backend.game.writing.entity.WritingGameChoices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 🧾 [쓰기 게임 선택 기록 레포지토리]
 * - 자녀의 문제별 필기 결과 및 정답 여부 저장
 * - 결과 집계 시 정답 개수 계산에 사용
 */
@Repository
public interface WritingGameChoicesRepository extends JpaRepository<WritingGameChoices, String> {

    /**
     * [결과 집계용]
     * 특정 세션(resultId)의 전체 선택 기록 중 정답 개수를 반환합니다.
     *
     * @param resultId 세션 결과 ID (Writing_Game_Result.wg_result_id)
     * @return 정답 개수 (is_correct = true)
     */
    @Query("SELECT COUNT(c) FROM WritingGameChoices c WHERE c.wgResultId = :resultId AND c.isCorrect = true")
    int countCorrectByResultId(@Param("resultId") String resultId);

    /**
     * [선택 기록 조회]
     * 세션 내 모든 문제의 선택 기록을 조회합니다.
     */
    List<WritingGameChoices> findByWgResultId(String wgResultId);
}
