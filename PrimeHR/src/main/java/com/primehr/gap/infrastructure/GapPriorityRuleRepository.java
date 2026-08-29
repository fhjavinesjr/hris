package com.primehr.gap.infrastructure;

import com.primehr.gap.domain.GapPriorityRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GapPriorityRuleRepository extends JpaRepository<GapPriorityRule, String> {
    Optional<GapPriorityRule> findByIdAndSchemeIdAndAgencyId(String id, String schemeId, String agencyId);
    List<GapPriorityRule> findBySchemeIdAndAgencyIdOrderByDisplayOrderAsc(String schemeId, String agencyId);
    List<GapPriorityRule> findBySchemeIdAndAgencyIdAndActiveTrueOrderByDisplayOrderAsc(String schemeId, String agencyId);
    boolean existsBySchemeIdAndDisplayOrder(String schemeId, int displayOrder);
}
