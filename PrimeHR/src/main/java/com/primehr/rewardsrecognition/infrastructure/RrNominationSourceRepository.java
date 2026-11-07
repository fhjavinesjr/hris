package com.primehr.rewardsrecognition.infrastructure;
import com.primehr.rewardsrecognition.domain.RrNominationSource;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface RrNominationSourceRepository extends JpaRepository<RrNominationSource,String>{List<RrNominationSource>findByAgencyIdAndNominationIdOrderByCreatedAtAsc(String agency,String nomination);void deleteByAgencyIdAndNominationId(String agency,String nomination);}
