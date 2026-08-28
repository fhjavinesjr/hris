package com.primehr.assessment.infrastructure;

import com.primehr.assessment.domain.AssessmentEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface AssessmentEvidenceRepository extends JpaRepository<AssessmentEvidence, String> {
    Optional<AssessmentEvidence> findByAgencyIdAndId(String agencyId, String id);
    List<AssessmentEvidence> findByRatingIdAndActiveTrueOrderByEvidenceDateDescIdAsc(String ratingId);
    boolean existsByRatingIdAndActiveTrue(String ratingId);
}
