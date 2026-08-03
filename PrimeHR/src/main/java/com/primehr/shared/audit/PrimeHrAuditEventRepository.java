package com.primehr.shared.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrimeHrAuditEventRepository extends JpaRepository<PrimeHrAuditEvent, String> {
    Page<PrimeHrAuditEvent> findByAgencyIdAndAggregateTypeAndAggregateId(
            String agencyId, String aggregateType, String aggregateId, Pageable pageable);
}
