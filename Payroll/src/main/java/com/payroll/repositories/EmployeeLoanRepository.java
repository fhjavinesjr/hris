package com.payroll.repositories;

import com.payroll.entitymodels.EmployeeLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeLoanRepository extends JpaRepository<EmployeeLoan, Long> {
}
