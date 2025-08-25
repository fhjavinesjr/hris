package com.timekeeping.repositories;

import com.timekeeping.entitymodels.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {



}
