package com.primehr.assessment.infrastructure;

import com.primehr.assessment.domain.AssessorAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssessorAssignmentRepository extends JpaRepository<AssessorAssignment, String> {
    Optional<AssessorAssignment> findByAgencyIdAndId(String agencyId, String id);
    List<AssessorAssignment> findByAssessmentCaseIdOrderByCreatedAtAsc(String caseId);
    boolean existsByAssessmentCaseIdAndMethodCodeAndAssessorEmployeeId(
            String caseId, String methodCode, Long assessorEmployeeId);
    boolean existsByAssessmentCaseIdAndActiveTrue(String caseId);
    List<AssessorAssignment> findByAssessmentCaseIdAndActiveTrueOrderByCreatedAtAsc(String caseId);
    org.springframework.data.domain.Page<AssessorAssignment> findByAgencyIdAndAssessorEmployeeNoIgnoreCaseAndActiveTrue(
            String agencyId, String employeeNo, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<AssessorAssignment>
    findByAgencyIdAndAssessorEmployeeNoIgnoreCaseAndActiveTrueAndStatusIn(
            String agencyId, String employeeNo, java.util.Collection<String> statuses,
            org.springframework.data.domain.Pageable pageable);
    long countByAssessmentCaseIdAndActiveTrue(String caseId);
    long countByAssessmentCaseIdAndActiveTrueAndStatus(String caseId, String status);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update AssessorAssignment assignment set assignment.version = assignment.version + 1, " +
            "assignment.status = case when assignment.status in ('ASSIGNED', 'RETURNED') " +
            "then 'IN_PROGRESS' else assignment.status end, assignment.submittedBy = null, " +
            "assignment.submittedAt = null where assignment.agencyId = :agencyId and assignment.id = :id " +
            "and upper(assignment.assessorEmployeeNo) = upper(:actor) and assignment.active = true " +
            "and assignment.status in ('ASSIGNED', 'IN_PROGRESS', 'RETURNED') " +
            "and assignment.version = :expected")
    int beginWork(@Param("agencyId") String agencyId, @Param("id") String id,
                  @Param("actor") String actor, @Param("expected") long expected);
}
