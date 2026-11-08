package com.primehr.rewardsrecognition.infrastructure;

import com.primehr.rewardsrecognition.domain.RrCommitteeCharter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RrCommitteeCharterRepository extends JpaRepository<RrCommitteeCharter, String> {
    Optional<RrCommitteeCharter> findByIdAndAgencyId(String id, String agencyId);
    boolean existsByAgencyIdAndNormalizedCode(String agencyId, String normalizedCode);
}
