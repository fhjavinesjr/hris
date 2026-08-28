package com.primehr.assessment.infrastructure;

import com.primehr.assessment.domain.AssessmentToolMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentToolMethodRepository extends JpaRepository<AssessmentToolMethod, String> {
    List<AssessmentToolMethod> findByToolIdAndActiveTrueOrderByMethodCode(String toolId);
}
