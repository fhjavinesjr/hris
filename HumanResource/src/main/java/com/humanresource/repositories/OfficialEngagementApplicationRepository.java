package com.humanresource.repositories;

import com.humanresource.entitymodels.OfficialEngagementApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OfficialEngagementApplicationRepository
        extends JpaRepository<OfficialEngagementApplication, Long> {

    List<OfficialEngagementApplication> findByEmployeeId(Long employeeId);

    List<OfficialEngagementApplication> findByEmployeeIdAndStatus(Long employeeId, String status);

    // Find OEs whose date range overlaps [periodStart, periodEnd]:
    // startDate <= periodEnd AND endDate >= periodStart
    List<OfficialEngagementApplication> findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long employeeId, LocalDate periodEnd, LocalDate periodStart);

    List<OfficialEngagementApplication> findByStatusOrderByStartDateDesc(String status);
}
