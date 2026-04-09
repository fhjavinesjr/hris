package com.timekeeping.repositories;

import com.timekeeping.entitymodels.DTRDaily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DTRDailyRepository extends JpaRepository<DTRDaily, Long> {
    Optional<DTRDaily> findByEmployeeIdAndWorkDate(String employeeId, LocalDate workDate);
    List<DTRDaily> findByEmployeeIdAndWorkDateBetween(String employeeId, LocalDate fromDate, LocalDate toDate);
}

