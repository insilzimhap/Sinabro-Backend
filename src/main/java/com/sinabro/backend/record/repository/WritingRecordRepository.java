package com.sinabro.backend.record.repository;

import com.sinabro.backend.record.entity.WritingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WritingRecordRepository extends JpaRepository<WritingRecord, String> {
}
