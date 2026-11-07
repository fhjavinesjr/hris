package com.payroll.repositories;

import com.payroll.entitymodels.Bir1604COfficerReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface Bir1604COfficerReviewRepository extends JpaRepository<Bir1604COfficerReview, Long> {
    Optional<Bir1604COfficerReview> findByTaxYear(Integer taxYear);
}
