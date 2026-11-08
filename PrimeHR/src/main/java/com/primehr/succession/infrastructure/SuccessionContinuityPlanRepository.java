package com.primehr.succession.infrastructure;
import com.primehr.succession.domain.SuccessionContinuityPlan;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface SuccessionContinuityPlanRepository extends JpaRepository<SuccessionContinuityPlan,String>{Optional<SuccessionContinuityPlan>findByIdAndAgencyId(String id,String agency);Optional<SuccessionContinuityPlan>findByAgencyIdAndRiskReviewId(String agency,String risk);List<SuccessionContinuityPlan>findByAgencyIdOrderByCreatedAtDesc(String agency);}
