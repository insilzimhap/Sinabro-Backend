package com.sinabro.backend.record.repository;

import com.sinabro.backend.record.entity.ListeningRecord;
import org.springframework.data.domain.Pageable; // 👈 이거 임포트 추가!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // 👈 이거 임포트 추가!
import org.springframework.data.repository.query.Param; // 👈 이거 임포트 추가!
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
// ⚠️ extends JpaRepository<ListeningRecord, String>, ListeningRecordRepositoryCustom
// 👆 여기서 , ListeningRecordRepositoryCustom 이 부분 일단 지워봐! (아마 이게 에러 원인일수도)
public interface ListeningRecordRepository extends JpaRepository<ListeningRecord, String> {

    // ✅ AI 리포트 서비스에서 필요한 메서드
    List<ListeningRecord> findByLsChildIdAndLsLearningDateBetween(String childId, LocalDateTime start, LocalDateTime end);

    // ⬇️ ⬇️ ⬇️ ⬇️ ⬇️ 이거 추가!!! ⬇️ ⬇️ ⬇️ ⬇️ ⬇️
    /**
     * [취약점 분석용]
     * 특정 자녀 + 열매의 최근 학습 결과(isCompleted) 3개를 날짜 내림차순으로 조회
     * * ⚠️ 엔티티(ListeningRecord)에 lsCompleted, lsChildId, fruitId 필드가 있어야 함!
     */
    @Query("SELECT lr.lsCompleted FROM ListeningRecord lr " +
            "WHERE lr.lsChildId = :childId AND lr.fruitId = :fruitId")
    List<Boolean> findRecentCompletedByChildIdAndFruitId(
            @Param("childId") String childId,
            @Param("fruitId") String fruitId,
            Pageable pageable); // 👈 Pageable이 정렬(Sort)과 제한(limit 3)을 처리해 줌
}