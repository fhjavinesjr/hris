package com.primehr.rewardsrecognition.infrastructure;
import com.primehr.rewardsrecognition.domain.RrNominationEvidence;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface RrNominationEvidenceRepository extends JpaRepository<RrNominationEvidence,String>{Optional<RrNominationEvidence>findByIdAndAgencyIdAndNominationId(String id,String agency,String nomination);List<RrNominationEvidence>findByAgencyIdAndNominationIdOrderByUploadedAtAsc(String agency,String nomination);}
