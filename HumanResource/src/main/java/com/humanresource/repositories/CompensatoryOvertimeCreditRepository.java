package com.humanresource.repositories;

import com.humanresource.entitymodels.CompensatoryOvertimeCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CompensatoryOvertimeCreditRepository extends JpaRepository<CompensatoryOvertimeCredit, Long> {

    List<CompensatoryOvertimeCredit> findByEmployeeIdOrderByDateFiledDesc(Long employeeId);

    List<CompensatoryOvertimeCredit> findByEmployeeIdAndStatus(Long employeeId, String status);

    List<CompensatoryOvertimeCredit> findByStatusOrderByDateFiledDesc(String status);

    /**
     * Returns true if there is already a Pending or Approved COC for this employee on this date.
     * Disapproved records are excluded so the employee may re-file after rejection.
     */
    boolean existsByEmployeeIdAndDateWorkedAndStatusNot(Long employeeId, LocalDate dateWorked, String status);

    /**
     * Computes the total approved COC hours earned by an employee
     * (from CocBeginningBalance.accumulatedHours + all approved COC transactions).
     * This is used by the CTO module to verify available balance.
     */
    @Query("SELECT COALESCE(SUM(c.hoursWorked), 0) FROM CompensatoryOvertimeCredit c " +
           "WHERE c.employeeId = :employeeId AND c.status = 'Approved'")
    Double sumApprovedHoursByEmployeeId(@Param("employeeId") Long employeeId);
}
