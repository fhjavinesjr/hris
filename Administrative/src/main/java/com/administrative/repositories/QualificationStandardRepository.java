package com.administrative.repositories;

import com.administrative.entitymodels.QualificationStandard;
import com.administrative.entitymodels.QualificationStandardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QualificationStandardRepository extends JpaRepository<QualificationStandard,Long> {
    List<QualificationStandard> findByJobPositionIdOrderByDefinitionVersionDesc(Long jobPositionId);
    Optional<QualificationStandard> findByIdAndJobPositionId(Long id,Long jobPositionId);
    boolean existsByJobPositionIdAndStatus(Long jobPositionId,QualificationStandardStatus status);
    Optional<QualificationStandard> findFirstByJobPositionIdAndStatusAndEffectiveFromLessThanEqualAndEffectiveToIsNullOrderByDefinitionVersionDesc(Long id,QualificationStandardStatus status,LocalDate date);
    List<QualificationStandard> findByJobPositionIdAndStatusOrderByDefinitionVersionDesc(Long id,QualificationStandardStatus status);
}
