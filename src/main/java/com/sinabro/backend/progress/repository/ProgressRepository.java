package com.sinabro.backend.progress.repository;

import com.sinabro.backend.progress.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressRepository extends JpaRepository<Progress, String> {
}
