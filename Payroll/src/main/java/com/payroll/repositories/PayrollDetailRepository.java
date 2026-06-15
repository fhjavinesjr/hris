package com.payroll.repositories;

import com.payroll.entitymodels.PayrollDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollDetailRepository extends JpaRepository<PayrollDetail, Long> {

    Optional<PayrollDetail> findByEmployeeNoAndSalaryPeriodKey(String employeeNo, String salaryPeriodKey);

    List<PayrollDetail> findBySalaryPeriodKey(String salaryPeriodKey);

    /** Delete child earnings before deleting parent (FK constraint). */
    @Modifying
    @Transactional
    @Query("DELETE FROM PayrollDetailEarning e WHERE e.payrollDetail.id IN (" +
            "SELECT pd.id FROM PayrollDetail pd WHERE pd.employeeNo = :empNo AND pd.salaryPeriodKey = :periodKey)")
    void deleteEarningsByEmployeeNoAndSalaryPeriodKey(@Param("empNo") String employeeNo,
                                                      @Param("periodKey") String salaryPeriodKey);

    /** Delete child deductions before deleting parent (FK constraint). */
    @Modifying
    @Transactional
    @Query("DELETE FROM PayrollDetailDeduction d WHERE d.payrollDetail.id IN (" +
            "SELECT pd.id FROM PayrollDetail pd WHERE pd.employeeNo = :empNo AND pd.salaryPeriodKey = :periodKey)")
    void deleteDeductionsByEmployeeNoAndSalaryPeriodKey(@Param("empNo") String employeeNo,
                                                        @Param("periodKey") String salaryPeriodKey);

    /** Used to allow recomputation: remove old record before saving fresh computation. */
    @Modifying
    @Transactional
    @Query("DELETE FROM PayrollDetail pd WHERE pd.employeeNo = :empNo AND pd.salaryPeriodKey = :periodKey")
    void deleteByEmployeeNoAndSalaryPeriodKey(@Param("empNo") String employeeNo,
                                              @Param("periodKey") String salaryPeriodKey);

    /**
     * Fetches the most recent payroll detail per employee before the given cutoff start.
     * Used to seed VL/SL balances from the previous period.
     * Returns at most one record per employeeNo (the latest).
     */
    @Query("""
            SELECT pd FROM PayrollDetail pd
            WHERE pd.employeeNo IN :employeeNos
              AND pd.cutoffEndDate < :cutoffStart
              AND pd.cutoffEndDate = (
                  SELECT MAX(pd2.cutoffEndDate)
                  FROM PayrollDetail pd2
                  WHERE pd2.employeeNo = pd.employeeNo
                    AND pd2.cutoffEndDate < :cutoffStart
              )
            """)
    List<PayrollDetail> findLatestBeforeCutoff(@Param("employeeNos") List<String> employeeNos,
                                               @Param("cutoffStart") LocalDate cutoffStart);
}
