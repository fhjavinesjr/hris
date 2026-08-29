package com.primehr.learning.referral.infrastructure;

import com.primehr.learning.referral.domain.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LdReferralRepository extends JpaRepository<LdReferral, String> {
    Optional<LdReferral> findByIdAndAgencyId(String id, String agencyId);
    Page<LdReferral> findByAgencyIdAndSubjectEmployeeNoContainingIgnoreCaseAndStatus(
            String agencyId, String employeeNo, LdReferralStatus status, Pageable pageable);
    Page<LdReferral> findByAgencyIdAndSubjectEmployeeNoContainingIgnoreCase(
            String agencyId, String employeeNo, Pageable pageable);
    Page<LdReferral> findByAgencyIdAndStatus(String agencyId, LdReferralStatus status, Pageable pageable);
    Page<LdReferral> findByAgencyId(String agencyId, Pageable pageable);
}

