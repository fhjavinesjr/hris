package com.primehr.rewardsrecognition.infrastructure;

import com.primehr.rewardsrecognition.domain.RrAwardCategoryVersion;
import com.primehr.rewardsrecognition.domain.RrDefinitionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RrAwardCategoryVersionRepository extends JpaRepository<RrAwardCategoryVersion, String> {
    Optional<RrAwardCategoryVersion> findByIdAndAgencyId(String id, String agencyId);
    List<RrAwardCategoryVersion> findByAgencyIdOrderByCreatedAtDesc(String agencyId);
    List<RrAwardCategoryVersion> findByAgencyIdAndCategoryIdOrderByDefinitionVersionDesc(String agencyId, String categoryId);
    List<RrAwardCategoryVersion> findByAgencyIdAndStatus(String agencyId, RrDefinitionStatus status);
}
