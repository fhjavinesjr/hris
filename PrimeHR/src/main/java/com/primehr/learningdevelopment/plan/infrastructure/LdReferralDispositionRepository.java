package com.primehr.learningdevelopment.plan.infrastructure;
import com.primehr.learningdevelopment.plan.domain.LdReferralDisposition;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface LdReferralDispositionRepository extends JpaRepository<LdReferralDisposition,String>{Optional<LdReferralDisposition>findByAgencyIdAndReferralIdAndReferralVersion(String agency,String referral,long version);List<LdReferralDisposition>findByAgencyIdOrderByDecidedAtDesc(String agency);}
