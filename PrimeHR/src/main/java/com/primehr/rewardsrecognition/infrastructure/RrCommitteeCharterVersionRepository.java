package com.primehr.rewardsrecognition.infrastructure;

import com.primehr.rewardsrecognition.domain.RrCommitteeCharterVersion;
import com.primehr.rewardsrecognition.domain.RrDefinitionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RrCommitteeCharterVersionRepository extends JpaRepository<RrCommitteeCharterVersion, String> {
    Optional<RrCommitteeCharterVersion> findByIdAndAgencyId(String id, String agencyId);
    List<RrCommitteeCharterVersion> findByAgencyIdOrderByCreatedAtDesc(String agencyId);
    List<RrCommitteeCharterVersion> findByAgencyIdAndCharterIdOrderByDefinitionVersionDesc(String agencyId, String charterId);
    List<RrCommitteeCharterVersion> findByAgencyIdAndStatus(String agencyId, RrDefinitionStatus status);
}
