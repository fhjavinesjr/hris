package com.payroll.repositories;

import com.payroll.entitymodels.PayrollPeriodLock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollPeriodLockRepository extends JpaRepository<PayrollPeriodLock, String> {
    /** Returns true if the given period key has already been locked. */
    boolean existsBySalaryPeriodKey(String salaryPeriodKey);
}
