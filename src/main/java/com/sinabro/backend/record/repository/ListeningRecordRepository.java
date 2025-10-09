package com.sinabro.backend.record.repository;

import com.sinabro.backend.record.entity.ListeningRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListeningRecordRepository extends JpaRepository<ListeningRecord, String> {
}
