package com.payroll.repositories;

import com.payroll.entitymodels.AnnualPeraTaxCredit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnnualPeraTaxCreditRepository extends JpaRepository<AnnualPeraTaxCredit, Long> {
    Optional<AnnualPeraTaxCredit> findByEmployeeNoIgnoreCaseAndTaxYear(
            String employeeNo, Integer taxYear);
    List<AnnualPeraTaxCredit> findByTaxYear(Integer taxYear);
}
