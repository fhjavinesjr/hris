package com.humanresource.repositories;

import com.humanresource.entitymodels.ReportHeaderSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportHeaderSettingsRepository extends JpaRepository<ReportHeaderSettings, Long> {

    Optional<ReportHeaderSettings> findFirstByOrderBySettingsIdDesc();
}
