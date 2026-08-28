package com.primehr.assessment.infrastructure;

import com.primehr.assessment.domain.AssessmentCycle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssessmentCycleRepository extends JpaRepository<AssessmentCycle, String> {
    Optional<AssessmentCycle> findByAgencyIdAndId(String agencyId, String id);
    boolean existsByAgencyIdAndCodeIgnoreCase(String agencyId, String code);
    Page<AssessmentCycle> findByAgencyId(String agencyId, Pageable pageable);
    Page<AssessmentCycle> findByAgencyIdAndStatus(String agencyId, String status, Pageable pageable);
}
