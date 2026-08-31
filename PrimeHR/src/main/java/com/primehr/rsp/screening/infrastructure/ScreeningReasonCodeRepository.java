package com.primehr.rsp.screening.infrastructure;
import com.primehr.rsp.screening.domain.ScreeningReasonCode; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ScreeningReasonCodeRepository extends JpaRepository<ScreeningReasonCode,String>{List<ScreeningReasonCode> findByAgencyIdAndPolicyIdOrderByDisplayOrderAsc(String agency,String policyId);void deleteByAgencyIdAndPolicyId(String agency,String policyId);}
