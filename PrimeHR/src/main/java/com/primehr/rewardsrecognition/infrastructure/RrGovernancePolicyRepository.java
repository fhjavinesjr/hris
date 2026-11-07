package com.primehr.rewardsrecognition.infrastructure;

import com.primehr.rewardsrecognition.domain.RrGovernancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RrGovernancePolicyRepository extends JpaRepository<RrGovernancePolicy, String> {
    Optional<RrGovernancePolicy> findByIdAndAgencyId(String id, String agencyId);
    boolean existsByAgencyIdAndNormalizedCode(String agencyId, String normalizedCode);
}
