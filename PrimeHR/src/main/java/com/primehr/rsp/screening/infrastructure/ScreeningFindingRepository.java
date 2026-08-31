package com.primehr.rsp.screening.infrastructure;
import com.primehr.rsp.screening.domain.ScreeningFinding; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ScreeningFindingRepository extends JpaRepository<ScreeningFinding,String>{List<ScreeningFinding> findByAgencyIdAndCaseIdOrderByDisplayOrderAsc(String agencyId,String caseId);Optional<ScreeningFinding> findByAgencyIdAndCaseIdAndCriterionId(String agencyId,String caseId,String criterionId);}
