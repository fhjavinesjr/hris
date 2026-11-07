package com.administrative.repositories;

import com.administrative.entitymodels.MinimumWageRate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MinimumWageRateRepository extends JpaRepository<MinimumWageRate, Long> {
    List<MinimumWageRate> findAllByOrderByRegionCodeAscEffectiveFromDesc();
    List<MinimumWageRate> findByRegionCodeIgnoreCaseOrderByEffectiveFromAsc(String regionCode);
}
