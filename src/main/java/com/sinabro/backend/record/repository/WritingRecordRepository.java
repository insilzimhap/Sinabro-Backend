package com.sinabro.backend.record.repository;

import com.sinabro.backend.record.entity.WritingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WritingRecordRepository extends JpaRepository<WritingRecord, String> {

    // ✅ AI 리포트 서비스에서 필요한 메서드 추가!
    List<WritingRecord> findByWsChildIdAndWsLearningDateBetween(String childId, LocalDateTime start, LocalDateTime end);
}