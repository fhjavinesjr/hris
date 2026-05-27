package com.administrative.repositories;

import com.administrative.entitymodels.PayrollSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayrollSettingsRepository extends JpaRepository<PayrollSettings, Long> {

    @Query("SELECT p FROM PayrollSettings p ORDER BY p.effectivityDate DESC LIMIT 1")
    Optional<PayrollSettings> findLatest();
}
