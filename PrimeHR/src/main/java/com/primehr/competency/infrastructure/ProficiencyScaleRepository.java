package com.primehr.competency.infrastructure;

import com.primehr.competency.domain.ProficiencyScale;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ProficiencyScaleRepository extends JpaRepository<ProficiencyScale, String>,
        JpaSpecificationExecutor<ProficiencyScale> {

    @Override
    @EntityGraph(attributePaths = "levels")
    List<ProficiencyScale> findAll(org.springframework.data.jpa.domain.Specification<ProficiencyScale> specification,
                                   org.springframework.data.domain.Sort sort);

    boolean existsByAgencyIdAndCodeIgnoreCase(String agencyId, String code);
    @EntityGraph(attributePaths = "levels")
    Optional<ProficiencyScale> findByIdAndAgencyId(String id, String agencyId);
    boolean existsByAgencyIdAndCodeIgnoreCaseAndStatus(String agencyId, String code, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "levels")
    List<ProficiencyScale> findByAgencyIdAndCodeIgnoreCaseOrderByDefinitionVersionAsc(
            String agencyId, String code);
}
