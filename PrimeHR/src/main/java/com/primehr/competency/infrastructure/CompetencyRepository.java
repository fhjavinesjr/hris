package com.primehr.competency.infrastructure;

import com.primehr.competency.domain.Competency;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public interface CompetencyRepository extends JpaRepository<Competency, String>, JpaSpecificationExecutor<Competency> {

    @Override
    @EntityGraph(attributePaths = {"category", "proficiencyScale"})
    Page<Competency> findAll(Specification<Competency> specification, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "proficiencyScale", "proficiencyScale.levels"})
    Optional<Competency> findByIdAndAgencyId(String id, String agencyId);

    boolean existsByAgencyIdAndCodeIgnoreCase(String agencyId, String code);
    boolean existsByAgencyIdAndCodeIgnoreCaseAndStatus(String agencyId, String code, String status);
}
