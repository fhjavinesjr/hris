package com.primehr.assessment.infrastructure;
import com.primehr.assessment.domain.AssessmentValidatedRating;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AssessmentValidatedRatingRepository extends JpaRepository<AssessmentValidatedRating,String>{
 List<AssessmentValidatedRating> findByValidationIdOrderByCompetencyCode(String validationId);
}
