package com.primehr.learningdevelopment.evaluation.infrastructure;
import com.primehr.learningdevelopment.evaluation.domain.LdEvaluationResult;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface LdEvaluationResultRepository extends JpaRepository<LdEvaluationResult,String>{Optional<LdEvaluationResult>findByIdAndAgencyId(String id,String agency);List<LdEvaluationResult>findByAgencyIdAndTaskIdOrderByResultRevisionDesc(String agency,String task);List<LdEvaluationResult>findByAgencyIdAndStatus(String agency,LdEvaluationResult.Status status);}
