package com.payroll.repositories;

import com.payroll.entitymodels.EmployeeLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeLoanRepository extends JpaRepository<EmployeeLoan, Long> {

    /** Returns all loans that are active (not stopped) and not yet fully paid. */
    @Query("SELECT l FROM EmployeeLoan l WHERE l.isStopDeduction = false AND l.paid < l.toPay")
    List<EmployeeLoan> findAllActiveLoans();
}
