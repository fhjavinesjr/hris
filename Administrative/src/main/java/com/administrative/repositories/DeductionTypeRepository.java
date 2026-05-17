package com.administrative.repositories;

import com.administrative.entitymodels.DeductionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeductionTypeRepository extends JpaRepository<DeductionType, Long> {
}
