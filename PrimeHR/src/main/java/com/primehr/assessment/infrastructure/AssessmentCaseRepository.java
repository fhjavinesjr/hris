package com.primehr.assessment.infrastructure;

import com.primehr.assessment.domain.AssessmentCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssessmentCaseRepository extends JpaRepository<AssessmentCase, String> {
    Optional<AssessmentCase> findByAgencyIdAndId(String agencyId, String id);
    boolean existsByToolIdAndSubjectEmployeeId(String toolId, Long subjectEmployeeId);
    Page<AssessmentCase> findByAgencyIdAndToolId(String agencyId, String toolId, Pageable pageable);
    java.util.List<AssessmentCase> findByAgencyIdAndToolIdAndActiveTrue(String agencyId, String toolId);
    boolean existsByToolId(String toolId);
    java.util.List<AssessmentCase> findByAgencyIdAndToolCycleIdAndActiveTrue(String agencyId, String cycleId);
    org.springframework.data.domain.Page<AssessmentCase> findByAgencyIdAndStatus(
            String agencyId, String status, Pageable pageable);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update AssessmentCase assessmentCase set assessmentCase.version = assessmentCase.version + 1 " +
            "where assessmentCase.agencyId = :agencyId and assessmentCase.id = :id " +
            "and assessmentCase.version = :expected")
    int bumpVersion(@Param("agencyId") String agencyId, @Param("id") String id,
                    @Param("expected") long expected);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update AssessmentCase assessmentCase set assessmentCase.version = assessmentCase.version + 1, " +
            "assessmentCase.status = case when assessmentCase.status in ('ASSIGNED', 'RETURNED') " +
            "then 'IN_PROGRESS' else assessmentCase.status end " +
            "where assessmentCase.agencyId = :agencyId and assessmentCase.id = :id " +
            "and assessmentCase.status in ('ASSIGNED', 'IN_PROGRESS', 'RETURNED') " +
            "and assessmentCase.version = :expected")
    int beginWork(@Param("agencyId") String agencyId, @Param("id") String id,
                  @Param("expected") long expected);
}
