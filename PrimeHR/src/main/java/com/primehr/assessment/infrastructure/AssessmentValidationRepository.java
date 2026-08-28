package com.primehr.assessment.infrastructure;
import com.primehr.assessment.domain.AssessmentValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface AssessmentValidationRepository extends JpaRepository<AssessmentValidation,String>{
 Optional<AssessmentValidation> findByAgencyIdAndAssessmentCaseId(String agencyId,String caseId);
 boolean existsByAssessmentCaseId(String caseId);
}
