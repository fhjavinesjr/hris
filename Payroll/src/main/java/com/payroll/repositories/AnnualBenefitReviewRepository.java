package com.payroll.repositories;

import com.payroll.entitymodels.AnnualBenefitReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AnnualBenefitReviewRepository extends JpaRepository<AnnualBenefitReview, Long> {
    List<AnnualBenefitReview> findByTaxYear(Integer taxYear);
    Optional<AnnualBenefitReview> findBySourceTypeAndSourceId(String sourceType, Long sourceId);
}
