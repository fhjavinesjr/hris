package com.payroll.repositories;

import com.payroll.entitymodels.PayrollAdjustmentHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollAdjustmentHeaderRepository extends JpaRepository<PayrollAdjustmentHeader, Long> {

    /**
     * Returns the current ACTIVE header (PENDING or POSTED, not SUPERSEDED) for an employee + period.
     * Used in saveAdjustment to locate the record that will be versioned out.
     */
    Optional<PayrollAdjustmentHeader> findByEmployeeNoAndSalaryPeriodKeyAndStatusNot(
            String employeeNo, String salaryPeriodKey, String excludedStatus);

    /**
     * Fetch the ACTIVE (non-SUPERSEDED) header with lines eagerly.
     * Used by findAdjustment (GET endpoint) and the adjustment modal pre-population.
     */
    @Query("SELECT h FROM PayrollAdjustmentHeader h LEFT JOIN FETCH h.lines " +
           "WHERE h.employeeNo = :empNo AND h.salaryPeriodKey = :key AND h.status <> 'SUPERSEDED'")
    Optional<PayrollAdjustmentHeader> findActiveWithLinesByEmployeeNoAndSalaryPeriodKey(
            @Param("empNo") String employeeNo, @Param("key") String key);

    /**
     * Fetch ALL versions (including SUPERSEDED) for one employee + period, newest first.
     * Used by the history endpoint to build the audit trail.
     */
    @Query("SELECT h FROM PayrollAdjustmentHeader h LEFT JOIN FETCH h.lines " +
           "WHERE h.employeeNo = :empNo AND h.salaryPeriodKey = :key ORDER BY h.version DESC")
    List<PayrollAdjustmentHeader> findAllVersionsByEmployeeNoAndSalaryPeriodKey(
            @Param("empNo") String empNo, @Param("key") String key);

    /**
     * Fetch ACTIVE (non-SUPERSEDED) headers with lines for a period.
     * Used by getSummaryForPeriod — only the latest active version per employee counts.
     */
    @Query("SELECT h FROM PayrollAdjustmentHeader h LEFT JOIN FETCH h.lines " +
           "WHERE h.salaryPeriodKey = :key AND h.status <> 'SUPERSEDED'")
    List<PayrollAdjustmentHeader> findActiveWithLinesBySalaryPeriodKey(@Param("key") String key);

            long countBySalaryPeriodKeyAndStatus(String salaryPeriodKey, String status);

            long deleteBySalaryPeriodKeyAndStatus(String salaryPeriodKey, String status);

            @Query("SELECT h.id FROM PayrollAdjustmentHeader h " +
                    "WHERE h.salaryPeriodKey = :key AND h.status = 'PENDING' AND h.employeeNo IN :employeeNos")
            List<Long> findPendingIdsForPeriodAndEmployees(
                     @Param("key") String salaryPeriodKey,
                     @Param("employeeNos") Collection<String> employeeNos);

            @Modifying
            @Transactional
            @Query("DELETE FROM PayrollAdjustmentHeader h WHERE h.id IN :headerIds")
            int deleteByIdIn(@Param("headerIds") Collection<Long> headerIds);
}
