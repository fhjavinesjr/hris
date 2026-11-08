package com.primehr.rewardsrecognition.infrastructure;

import com.primehr.rewardsrecognition.domain.RrAwardCriterion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RrAwardCriterionRepository extends JpaRepository<RrAwardCriterion, String> {
    List<RrAwardCriterion> findByAgencyIdAndCategoryVersionIdOrderByDisplayOrderAsc(String agencyId, String categoryVersionId);
    void deleteByAgencyIdAndCategoryVersionId(String agencyId, String categoryVersionId);
}
