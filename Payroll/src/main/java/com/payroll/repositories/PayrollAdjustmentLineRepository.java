package com.payroll.repositories;

import com.payroll.entitymodels.PayrollAdjustmentLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.time.LocalDate;
import java.util.List;
import com.payroll.entitymodels.PayrollStatus;

@Repository
public interface PayrollAdjustmentLineRepository extends JpaRepository<PayrollAdjustmentLine, Long> {

    @Query("""
            SELECT l FROM PayrollAdjustmentLine l
            JOIN FETCH l.header h
            JOIN PayrollDetail pd
              ON pd.employeeNo = h.employeeNo
             AND pd.salaryPeriodKey = h.salaryPeriodKey
            WHERE UPPER(h.status) = 'POSTED'
              AND pd.salaryDate BETWEEN :fromDate AND :toDate
              AND pd.status <> :cancelledStatus
              AND EXISTS (
                  SELECT pl.salaryPeriodKey FROM PayrollPeriodLock pl
                  WHERE pl.salaryPeriodKey = pd.salaryPeriodKey
              )
            """)
    List<PayrollAdjustmentLine> findPostedForAnnualization(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("cancelledStatus") PayrollStatus cancelledStatus);

    @Query("""
            SELECT l FROM PayrollAdjustmentLine l JOIN FETCH l.header h
            WHERE UPPER(h.status) = 'POSTED'
              AND UPPER(h.employeeNo) = UPPER(:employeeNo)
              AND h.salaryPeriodKey = :salaryPeriodKey
            """)
    List<PayrollAdjustmentLine> findPostedForEmployeePeriod(
            @Param("employeeNo") String employeeNo,
            @Param("salaryPeriodKey") String salaryPeriodKey);

    @Modifying
    @Transactional
    @Query("DELETE FROM PayrollAdjustmentLine l WHERE l.header.id IN :headerIds")
    int deleteByHeaderIdIn(@Param("headerIds") Collection<Long> headerIds);
}
