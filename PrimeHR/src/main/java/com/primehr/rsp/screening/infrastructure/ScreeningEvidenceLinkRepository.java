package com.primehr.rsp.screening.infrastructure;
import com.primehr.rsp.screening.domain.ScreeningEvidenceLink; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ScreeningEvidenceLinkRepository extends JpaRepository<ScreeningEvidenceLink,String>{List<ScreeningEvidenceLink> findByAgencyIdAndCaseId(String agencyId,String caseId);List<ScreeningEvidenceLink> findByAgencyIdAndFindingId(String agencyId,String findingId);void deleteByAgencyIdAndFindingId(String agencyId,String findingId);}
