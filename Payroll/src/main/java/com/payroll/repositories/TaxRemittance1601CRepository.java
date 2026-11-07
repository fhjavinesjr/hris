package com.payroll.repositories;

import com.payroll.entitymodels.TaxRemittance1601C;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaxRemittance1601CRepository extends JpaRepository<TaxRemittance1601C, Long> {
    List<TaxRemittance1601C> findByTaxYearOrderByTaxMonthAsc(Integer taxYear);
    Optional<TaxRemittance1601C> findByTaxYearAndTaxMonth(Integer taxYear, Integer taxMonth);
}
