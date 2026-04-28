package com.humanresource.repositories;

import com.humanresource.entitymodels.LeaveApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    List<LeaveApplication> findByEmployeeId(Long employeeId);

    List<LeaveApplication> findByEmployeeIdAndLeaveType(Long employeeId, String leaveType);

    // Range overlap: startDate <= periodEnd AND endDate >= periodStart
    List<LeaveApplication> findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long employeeId, LocalDate periodEnd, LocalDate periodStart);

}
