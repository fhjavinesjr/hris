package com.payroll.repositories;

import com.payroll.entitymodels.PayrollDetailDeduction;
import com.payroll.entitymodels.PayrollStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PayrollDetailDeductionRepository extends JpaRepository<PayrollDetailDeduction, Long> {
    @Query("""
            SELECT d FROM PayrollDetailDeduction d
            JOIN FETCH d.payrollDetail pd
            WHERE pd.salaryDate BETWEEN :fromDate AND :toDate
              AND pd.status <> :cancelledStatus
              AND EXISTS (
                  SELECT pl.salaryPeriodKey FROM PayrollPeriodLock pl
                  WHERE pl.salaryPeriodKey = pd.salaryPeriodKey
              )
            """)
    List<PayrollDetailDeduction> findFinalizedForAnnualization(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("cancelledStatus") PayrollStatus cancelledStatus);
}
