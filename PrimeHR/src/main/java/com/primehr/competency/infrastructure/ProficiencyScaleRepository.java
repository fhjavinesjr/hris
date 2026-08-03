package com.primehr.competency.infrastructure;

import com.primehr.competency.domain.ProficiencyScale;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProficiencyScaleRepository extends JpaRepository<ProficiencyScale, String>,
        JpaSpecificationExecutor<ProficiencyScale> {

    @Override
    @EntityGraph(attributePaths = "levels")
    List<ProficiencyScale> findAll(org.springframework.data.jpa.domain.Specification<ProficiencyScale> specification,
                                   org.springframework.data.domain.Sort sort);

    boolean existsByAgencyIdAndCodeIgnoreCase(String agencyId, String code);
}
