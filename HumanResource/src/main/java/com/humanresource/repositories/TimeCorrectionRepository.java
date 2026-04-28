package com.humanresource.repositories;

import com.humanresource.entitymodels.TimeCorrection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeCorrectionRepository extends JpaRepository<TimeCorrection, Long> {

    List<TimeCorrection> findByEmployeeId(Long employeeId);

    List<TimeCorrection> findByEmployeeIdAndWorkDateBetween(Long employeeId, LocalDate from, LocalDate to);

    List<TimeCorrection> findByStatusOrderByDateFiledDesc(String status);
}
