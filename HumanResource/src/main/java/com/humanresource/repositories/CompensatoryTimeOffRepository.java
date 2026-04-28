package com.humanresource.repositories;

import com.humanresource.entitymodels.CompensatoryTimeOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CompensatoryTimeOffRepository extends JpaRepository<CompensatoryTimeOff, Long> {

    List<CompensatoryTimeOff> findByEmployeeIdOrderByDateFiledDesc(Long employeeId);

    List<CompensatoryTimeOff> findByEmployeeIdAndStatus(Long employeeId, String status);

    List<CompensatoryTimeOff> findByStatusOrderByDateFiledDesc(String status);

    boolean existsByEmployeeIdAndDateOfOffset(Long employeeId, LocalDate dateOfOffset);

    // Find CTOs where dateOfOffset falls within [from, to]
    List<CompensatoryTimeOff> findByEmployeeIdAndDateOfOffsetBetween(Long employeeId, LocalDate from, LocalDate to);

    /**
     * Computes total approved CTO hours used by an employee.
     * Subtracted from COC balance earned to get remaining balance.
     */
    @Query("SELECT COALESCE(SUM(c.hoursUsed), 0) FROM CompensatoryTimeOff c " +
           "WHERE c.employeeId = :employeeId AND c.status = 'Approved'")
    Double sumApprovedHoursUsedByEmployeeId(@Param("employeeId") Long employeeId);
}
