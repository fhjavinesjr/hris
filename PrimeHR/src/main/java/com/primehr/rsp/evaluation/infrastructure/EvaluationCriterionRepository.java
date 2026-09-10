package com.primehr.rsp.evaluation.infrastructure;
import com.primehr.rsp.evaluation.domain.EvaluationCriterion; import org.springframework.data.jpa.repository.*; import java.util.*;
public interface EvaluationCriterionRepository extends JpaRepository<EvaluationCriterion,String>{List<EvaluationCriterion> findByAgencyIdAndPolicyIdOrderByDisplayOrderAsc(String agency,String policy);List<EvaluationCriterion> findByAgencyIdAndStageIdOrderByDisplayOrderAsc(String agency,String stage);void deleteByAgencyIdAndPolicyId(String agency,String policy);}
