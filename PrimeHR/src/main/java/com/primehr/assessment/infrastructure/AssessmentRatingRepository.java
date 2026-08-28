package com.primehr.assessment.infrastructure;

import com.primehr.assessment.domain.AssessmentRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface AssessmentRatingRepository extends JpaRepository<AssessmentRating, String> {
    Optional<AssessmentRating> findByAgencyIdAndAssignmentIdAndCompetencyId(
            String agencyId, String assignmentId, String competencyId);
    Optional<AssessmentRating> findByAgencyIdAndId(String agencyId, String id);
    List<AssessmentRating> findByAssignmentIdAndActiveTrueOrderByCompetencyCode(String assignmentId);
    long countByAssignmentIdAndActiveTrue(String assignmentId);
    boolean existsByAssignmentId(String assignmentId);
    @Query("select rating from AssessmentRating rating " +
            "where rating.assignment.assessmentCase.id = :caseId and rating.active = true " +
            "order by rating.competency.code, rating.assignment.id")
    List<AssessmentRating> findActiveForCase(@Param("caseId") String caseId);
}
