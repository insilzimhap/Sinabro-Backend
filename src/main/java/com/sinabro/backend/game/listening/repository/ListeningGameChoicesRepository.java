package com.sinabro.backend.game.listening.repository;

import com.sinabro.backend.game.listening.entity.ListeningGameChoices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *  듣기 게임 선택 기록 Repository
 * - 한 세션에서 자녀가 문제별로 선택한 보기 기록을 저장·조회
 * - 정답 수 계산 및 채점 로직에 활용
 */
@Repository
public interface ListeningGameChoicesRepository extends JpaRepository<ListeningGameChoices, String> {

    // 특정 결과(Result) ID에 속한 모든 선택 기록 조회
    List<ListeningGameChoices> findByLgResultId(String resultId);

    // 특정 결과(Result) 내에서 정답만 조회 (점수 집계용)
    List<ListeningGameChoices> findByLgResultIdAndIsCorrectTrue(String resultId);

    // 특정 문제(Question) 기준으로 모든 선택 기록 조회 (분석용)
    List<ListeningGameChoices> findByLgQuestionId(String questionId);
}
