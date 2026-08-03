package com.primehr.competency.infrastructure;

import com.primehr.competency.domain.BehavioralIndicator;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BehavioralIndicatorRepository extends JpaRepository<BehavioralIndicator, String> {

    @EntityGraph(attributePaths = "proficiencyLevel")
    List<BehavioralIndicator> findByCompetencyIdAndAgencyIdOrderByProficiencyLevelLevelOrderAscDisplayOrderAsc(
            String competencyId, String agencyId);

    Optional<BehavioralIndicator> findByIdAndCompetencyIdAndAgencyId(
            String id, String competencyId, String agencyId);
}
