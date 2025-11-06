package com.administrative.repositories;

import com.administrative.entitymodels.SalarySchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface SalaryScheduleRepository extends JpaRepository<SalarySchedule, Long> {

    void deleteByEffectivityDate(LocalDateTime effectivityDate);

}
