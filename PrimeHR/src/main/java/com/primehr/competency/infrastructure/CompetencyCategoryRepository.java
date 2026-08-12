package com.primehr.competency.infrastructure;

import com.primehr.competency.domain.CompetencyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface CompetencyCategoryRepository extends JpaRepository<CompetencyCategory, String>,
        JpaSpecificationExecutor<CompetencyCategory> {

    boolean existsByAgencyIdAndCodeIgnoreCase(String agencyId, String code);
    Optional<CompetencyCategory> findByIdAndAgencyId(String id, String agencyId);
    boolean existsByAgencyIdAndCodeIgnoreCaseAndStatus(String agencyId, String code, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<CompetencyCategory> findByAgencyIdAndCodeIgnoreCaseOrderByDefinitionVersionAsc(
            String agencyId, String code);
}
