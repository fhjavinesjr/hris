package com.primehr.evidence.infrastructure;

import com.primehr.evidence.domain.EvidenceReadinessSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvidenceReadinessSnapshotRepository extends JpaRepository<EvidenceReadinessSnapshot, String> {
    Optional<EvidenceReadinessSnapshot> findByIdAndAgencyId(String id, String agencyId);
    List<EvidenceReadinessSnapshot> findByAgencyIdAndAssessmentCaseIdOrderBySnapshotRevisionAsc(
            String agencyId, String assessmentCaseId);
    Optional<EvidenceReadinessSnapshot> findFirstByAgencyIdAndAssessmentCaseIdOrderBySnapshotRevisionDesc(
            String agencyId, String assessmentCaseId);
    List<EvidenceReadinessSnapshot> findByAgencyIdOrderBySignedAtDesc(String agencyId);
    long countByAgencyIdAndAssessmentCaseId(String agencyId, String assessmentCaseId);
}
