package com.sinabro.backend.user.repository;

import com.sinabro.backend.user.entity.ParentSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentSettingRepository extends JpaRepository<ParentSetting, String> {
    // PK = user_id
}