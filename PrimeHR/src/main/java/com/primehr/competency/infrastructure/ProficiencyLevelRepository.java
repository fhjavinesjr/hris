package com.primehr.competency.infrastructure;

import com.primehr.competency.domain.ProficiencyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ProficiencyLevelRepository extends JpaRepository<ProficiencyLevel, String> {
    Optional<ProficiencyLevel> findByIdAndScaleIdAndAgencyId(String id, String scaleId, String agencyId);
    Optional<ProficiencyLevel> findByIdAndAgencyId(String id, String agencyId);
    List<ProficiencyLevel> findByScaleIdAndActiveTrueOrderByLevelOrderAsc(String scaleId);
}
