package com.payroll.repositories;

import com.payroll.entitymodels.EmployeeDeduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeDeductionRepository extends JpaRepository<EmployeeDeduction, Long> {

    /** Returns all entry deductions for the given salary period string (e.g. "2024-01"). */
    List<EmployeeDeduction> findBySalaryPeriod(String salaryPeriod);
}
