package com.sinabro.backend.record.repository;

import com.sinabro.backend.record.entity.WritingGameResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WritingGameResultRepository extends JpaRepository<WritingGameResult, String> {

    // WritingGameResultRepository.java
    @Query("SELECT wgr FROM WritingGameResult wgr " +
            "WHERE wgr.wgChildId = :childId AND wgr.fruitId = :fruitId " +
            "ORDER BY wgr.wgPlayDate DESC")
    List<WritingGameResult> findLatestByChildIdAndFruitId(
            @Param("childId") String childId,
            @Param("fruitId") String fruitId,
            Pageable pageable);

    // 편의 메서드: 최신 1건
    default Optional<WritingGameResult> findTop1ByWgChildIdAndFruitIdOrderByWgPlayDateDesc(
            String childId, String fruitId) {
        List<WritingGameResult> list =
                findLatestByChildIdAndFruitId(childId, fruitId, Pageable.ofSize(1));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }


    // AI 리포트 생성 시, 특정 날짜의 쓰기 게임 결과를 조회하기 위한 메서드
    List<WritingGameResult> findByWgChildIdAndWgPlayDateBetween(String childId, LocalDateTime start, LocalDateTime end);


    /**
     * [취약점 분석용 커스텀 쿼리]
     * 특정 아이가 특정 열매(fruit)에 대해 플레이한 쓰기 게임 결과 중 성공 여부(isSuccess)만 조회합니다.
     * @param childId 자녀 ID
     * @param fruitId 열매 ID
     * @param pageable 정렬 및 개수 제한 정보 (예: 최근 3개)
     * @return 성공 여부(true/false) 리스트
     */
    @Query("SELECT wgr.isSuccess FROM WritingGameResult wgr WHERE wgr.wgChildId = :childId AND wgr.fruitId = :fruitId")
    List<Boolean> findRecentSuccessByChildIdAndFruitId(
            @Param("childId") String childId,
            @Param("fruitId") String fruitId,
            Pageable pageable
    );
}