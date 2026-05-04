package com.humanresource.repositories;

import com.humanresource.entitymodels.LeaveMonetization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveMonetizationRepository extends JpaRepository<LeaveMonetization, Long> {

    // All records for an employee, newest first
    List<LeaveMonetization> findByEmployeeIdOrderByDateFiledDesc(Long employeeId);

    // All records by approval status (e.g. "Pending") — for HR review screens
    List<LeaveMonetization> findByApprovalStatusOrderByDateFiledDesc(String approvalStatus);

    // Approved monetizations for a specific employee within a leave cutoff period.
    // Used by LeaveProcessServiceImpl to subtract monetized SL/VL from computed balances.
    List<LeaveMonetization> findByEmployeeIdAndApprovalStatusAndApprovedAtBetween(
            Long employeeId, String approvalStatus, LocalDate from, LocalDate to);
}
