package com.timekeeping.repositories;

import com.timekeeping.entitymodels.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {

    Optional<List<WorkSchedule>> findByEmployeeNoAndWsDateTimeBetween(String employeeNo, LocalDateTime fromDate, LocalDateTime toDate);

}
