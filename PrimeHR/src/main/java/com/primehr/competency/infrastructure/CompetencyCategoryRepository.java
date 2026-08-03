package com.primehr.competency.infrastructure;

import com.primehr.competency.domain.CompetencyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface CompetencyCategoryRepository extends JpaRepository<CompetencyCategory, String>,
        JpaSpecificationExecutor<CompetencyCategory> {

    boolean existsByAgencyIdAndCodeIgnoreCase(String agencyId, String code);
    Optional<CompetencyCategory> findByIdAndAgencyId(String id, String agencyId);
    boolean existsByAgencyIdAndCodeIgnoreCaseAndStatus(String agencyId, String code, String status);
}
