package com.primehr.gap.infrastructure;

import com.primehr.gap.domain.CompetencyGapItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompetencyGapItemRepository extends JpaRepository<CompetencyGapItem, String> {
    List<CompetencyGapItem> findByAnalysisIdOrderByDisplayOrderAscCompetencyCodeAsc(String analysisId);
}
