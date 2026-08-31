package com.primehr.rsp.screening.infrastructure;
import com.primehr.rsp.screening.domain.ScreeningCriterion; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ScreeningCriterionRepository extends JpaRepository<ScreeningCriterion,String>{List<ScreeningCriterion> findByAgencyIdAndPolicyIdOrderByDisplayOrderAsc(String agency,String policyId);long countByAgencyIdAndPolicyId(String agency,String policyId);void deleteByAgencyIdAndPolicyId(String agency,String policyId);}
