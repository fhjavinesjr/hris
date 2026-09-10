package com.primehr.rsp.evaluation.infrastructure;
import com.primehr.rsp.evaluation.domain.EvaluationStage; import org.springframework.data.jpa.repository.*; import java.util.*;
public interface EvaluationStageRepository extends JpaRepository<EvaluationStage,String>{List<EvaluationStage> findByAgencyIdAndPolicyIdOrderByDisplayOrderAsc(String agency,String policy);void deleteByAgencyIdAndPolicyId(String agency,String policy);long countByAgencyIdAndPolicyId(String agency,String policy);}
