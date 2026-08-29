package com.primehr.gap.infrastructure;

import com.primehr.gap.domain.GapPriorityLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GapPriorityLevelRepository extends JpaRepository<GapPriorityLevel, String> {
    Optional<GapPriorityLevel> findByIdAndSchemeIdAndAgencyId(String id, String schemeId, String agencyId);
    List<GapPriorityLevel> findBySchemeIdAndAgencyIdOrderByDisplayOrderAscPriorityRankAsc(
            String schemeId, String agencyId);
    List<GapPriorityLevel> findBySchemeIdAndAgencyIdAndActiveTrueOrderByDisplayOrderAscPriorityRankAsc(
            String schemeId, String agencyId);
    boolean existsBySchemeIdAndCodeIgnoreCase(String schemeId, String code);
    boolean existsBySchemeIdAndPriorityRank(String schemeId, int priorityRank);
}
