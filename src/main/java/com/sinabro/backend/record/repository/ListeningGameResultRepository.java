package com.sinabro.backend.record.repository;

import com.sinabro.backend.record.entity.ListeningGameResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
// ListeningGameResult 엔티티의 PK인 lg_result_id가 String 타입이므로 String을 사용했어.
public interface ListeningGameResultRepository extends JpaRepository<ListeningGameResult, String> {

    // AI 리포트 생성 시, 특정 날짜의 듣기 게임 결과를 조회하기 위한 메서드
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