package com.primehr.rewardsrecognition.infrastructure;
import com.primehr.rewardsrecognition.domain.RrNominationMember;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface RrNominationMemberRepository extends JpaRepository<RrNominationMember,String>{List<RrNominationMember>findByAgencyIdAndNominationIdOrderByDisplayOrderAsc(String agency,String nomination);void deleteByAgencyIdAndNominationId(String agency,String nomination);}
