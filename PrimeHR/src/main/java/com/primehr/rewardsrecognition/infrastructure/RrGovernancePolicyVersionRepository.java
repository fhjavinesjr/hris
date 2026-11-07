package com.primehr.rewardsrecognition.infrastructure;

import com.primehr.rewardsrecognition.domain.RrDefinitionStatus;
import com.primehr.rewardsrecognition.domain.RrGovernancePolicyVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RrGovernancePolicyVersionRepository extends JpaRepository<RrGovernancePolicyVersion, String> {
    Optional<RrGovernancePolicyVersion> findByIdAndAgencyId(String id, String agencyId);
    List<RrGovernancePolicyVersion> findByAgencyIdOrderByCreatedAtDesc(String agencyId);
    List<RrGovernancePolicyVersion> findByAgencyIdAndPolicyIdOrderByDefinitionVersionDesc(String agencyId, String policyId);
    List<RrGovernancePolicyVersion> findByAgencyIdAndStatus(String agencyId, RrDefinitionStatus status);
}
