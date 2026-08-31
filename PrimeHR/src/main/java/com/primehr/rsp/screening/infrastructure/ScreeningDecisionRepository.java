package com.primehr.rsp.screening.infrastructure;
import com.primehr.rsp.screening.domain.ScreeningDecision; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ScreeningDecisionRepository extends JpaRepository<ScreeningDecision,String>{Optional<ScreeningDecision> findByAgencyIdAndCaseId(String agencyId,String caseId);}
