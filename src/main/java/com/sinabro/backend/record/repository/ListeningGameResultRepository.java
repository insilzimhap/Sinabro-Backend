package com.sinabro.backend.record.repository;

import com.sinabro.backend.record.entity.ListeningGameResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 🎧 ListeningGameResultRepository
 * - 듣기 게임 결과 Repository
 * - 한 열매(세트) 단위의 플레이 결과를 저장·조회
 * - 자녀별, 열매별 최신/성공 기록 및 분석용 조회 제공
 */

@Repository
// ListeningGameResult 엔티티의 PK인 lg_result_id가 String 타입이므로 String을 사용했어.
public interface ListeningGameResultRepository extends JpaRepository<ListeningGameResult, String> {

    // === 🍎 게임 결과 조회용 ===

    /**
     * [자녀별 결과 조회]
     * - 특정 자녀(childId)의 모든 결과를 최신순으로 조회
     * - 예: 마이페이지나 리포트 이력용
     */
    List<ListeningGameResult> findByLgChildIdOrderByLgPlayDateDesc(String childId);

    /**
     * [자녀 + 열매별 최신 결과 조회]
     * - 자녀(childId)가 특정 열매(fruitId)에 대해 마지막으로 플레이한 결과 1건 조회
     * - ListeningGameTreeResponseDto.lastSuccess, lastScore 계산에 사용
     */
    Optional<ListeningGameResult> findTop1ByLgChildIdAndFruitIdOrderByLgPlayDateDesc(
            String childId,
            String fruitId
    );

    /**
     * [열매별 전체 결과 조회]
     * - 특정 열매(fruitId)에 대한 모든 플레이 결과를 최신순으로 조회
     * - 관리자 리포트나 통계용
     */
    List<ListeningGameResult> findByFruitIdOrderByLgPlayDateDesc(String fruitId);

    // === 🧠 AI 리포트 / 분석용 ===

    /**
     * [AI 리포트 조회]
     * - 특정 자녀가 특정 날짜 범위 내에 플레이한 결과를 모두 조회
     * - GPT 학습 리포트 생성 시 사용
     */
    List<ListeningGameResult> findByLgChildIdAndLgPlayDateBetween(String childId, LocalDateTime start, LocalDateTime end);

    /**
     * [취약점 분석용 커스텀 쿼리]
     * 특정 아이가 특정 열매(fruit)에 대해 플레이한 게임 결과 중 성공 여부(isSuccess)만 조회합니다.
     * Pageable 객체를 통해 "최근 3회" 같은 조건을 동적으로 처리할 수 있습니다.
     * @param childId 자녀 ID
     * @param fruitId 열매 ID
     * @param pageable 정렬 및 개수 제한 정보 (예: 최근 3개)
     * @return 성공 여부(true/false) 리스트
     */
    @Query("SELECT lgr.isSuccess FROM ListeningGameResult lgr WHERE lgr.lgChildId = :childId AND lgr.fruitId = :fruitId")
    List<Boolean> findRecentSuccessByChildIdAndFruitId(
            @Param("childId") String childId,
            @Param("fruitId") String fruitId,
            Pageable pageable
    );
}