package com.payroll.repositories;

import com.payroll.entitymodels.AnnualizationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnualizationAuditLogRepository extends JpaRepository<AnnualizationAuditLog, Long> {
}
