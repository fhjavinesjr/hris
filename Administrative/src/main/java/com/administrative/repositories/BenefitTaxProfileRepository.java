package com.administrative.repositories;

import com.administrative.entitymodels.BenefitTaxProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BenefitTaxProfileRepository extends JpaRepository<BenefitTaxProfile, Long> {
    List<BenefitTaxProfile> findByTaxYearOrderByLabelAsc(Integer taxYear);
    boolean existsByProfileCodeIgnoreCaseAndTaxYearAndIdNot(String code, Integer year, Long id);
}
