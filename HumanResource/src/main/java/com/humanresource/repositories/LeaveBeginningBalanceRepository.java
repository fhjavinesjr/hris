package com.humanresource.repositories;

import com.humanresource.entitymodels.LeaveBeginningBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBeginningBalanceRepository extends JpaRepository<LeaveBeginningBalance, Long> {
    List<LeaveBeginningBalance> findByEmployeeId(Long employeeId);
    Optional<LeaveBeginningBalance> findByEmployeeIdAndLeaveType(Long employeeId, String leaveType);
}
