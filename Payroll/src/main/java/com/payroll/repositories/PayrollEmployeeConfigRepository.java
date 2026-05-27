package com.payroll.repositories;

import com.payroll.entitymodels.PayrollEmployeeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollEmployeeConfigRepository extends JpaRepository<PayrollEmployeeConfig, Long> {

    List<PayrollEmployeeConfig> findBySalaryPeriodKey(String salaryPeriodKey);

    Optional<PayrollEmployeeConfig> findByEmployeeNoAndSalaryPeriodKey(String employeeNo, String salaryPeriodKey);

    /**
     * Returns the most recent config for each employee based on salaryPeriodKey lexicographic order.
     * Used to copy defaults when initializing a new period.
     */
    @Query("""
        SELECT c FROM PayrollEmployeeConfig c
        WHERE c.salaryPeriodKey = (
            SELECT MAX(c2.salaryPeriodKey)
            FROM PayrollEmployeeConfig c2
            WHERE c2.employeeNo = c.employeeNo
              AND c2.salaryPeriodKey < :salaryPeriodKey
        )
    """)
    List<PayrollEmployeeConfig> findLatestBeforePeriod(@Param("salaryPeriodKey") String salaryPeriodKey);
}
