package com.payroll.repositories;

import com.payroll.entitymodels.EmployeeDeduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeDeductionRepository extends JpaRepository<EmployeeDeduction, Long> {
}
