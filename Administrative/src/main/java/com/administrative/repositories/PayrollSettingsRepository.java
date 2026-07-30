package com.administrative.repositories;

import com.administrative.entitymodels.PayrollSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayrollSettingsRepository extends JpaRepository<PayrollSettings, Long> {

    Optional<PayrollSettings> findFirstByOrderByEffectivityDateDesc();
}
