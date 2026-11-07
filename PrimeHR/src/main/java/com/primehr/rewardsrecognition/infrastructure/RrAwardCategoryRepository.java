package com.primehr.rewardsrecognition.infrastructure;

import com.primehr.rewardsrecognition.domain.RrAwardCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RrAwardCategoryRepository extends JpaRepository<RrAwardCategory, String> {
    Optional<RrAwardCategory> findByIdAndAgencyId(String id, String agencyId);
    boolean existsByAgencyIdAndNormalizedCode(String agencyId, String normalizedCode);
}
