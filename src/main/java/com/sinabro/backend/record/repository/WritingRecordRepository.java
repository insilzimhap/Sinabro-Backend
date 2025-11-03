package com.sinabro.backend.record.repository;

import com.sinabro.backend.record.entity.WritingRecord;
import org.springframework.data.domain.Pageable; // 👈 이거 임포트 추가!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // 👈 이거 임포트 추가!
import org.springframework.data.repository.query.Param; // 👈 이거 임포트 추가!
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WritingRecordRepository extends JpaRepository<WritingRecord, String> {

    // ✅ AI 리포트 서비스에서 필요한 메서드
    List<WritingRecord> findByWsChildIdAndWsLearningDateBetween(String childId, LocalDateTime start, LocalDateTime end);

    // ⬇️ ⬇️ ⬇️ ⬇️ ⬇️ 이거 추가!!! ⬇️ ⬇️ ⬇️ ⬇️ ⬇️
    /**
     * [취약점 분석용]
     * 특정 자녀 + 열매의 최근 학습 결과(isCompleted) 3개를 날짜 내림차순으로 조회
     *
     * ⚠️ 엔티티(WritingRecord)에 wsCompleted, wsChildId, fruitId 필드가 있어야 함!
     */
    @Query("SELECT wr.wsCompleted FROM WritingRecord wr " +
            "WHERE wr.wsChildId = :childId AND wr.fruitId = :fruitId")
    List<Boolean> findRecentCompletedByChildIdAndFruitId(
            @Param("childId") String childId,
            @Param("fruitId") String fruitId,
            Pageable pageable); // 👈 Pageable이 정렬(Sort)과 제한(limit 3)을 처리해 줌
}