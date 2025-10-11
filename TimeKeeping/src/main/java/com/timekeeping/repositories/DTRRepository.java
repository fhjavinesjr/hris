package com.timekeeping.repositories;

import com.timekeeping.entitymodels.DTR;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DTRRepository  extends JpaRepository<DTR, Long> {
    // Avoid using raw SQL queries to prevent SQL injection attacks.

    Optional<List<DTR>> findByEmployeeIdAndDtrDateBetween(String employeeId, LocalDateTime fromDate, LocalDateTime toDate);

//    @Query("SELECT d FROM DTR d WHERE d.employeeId = :employeeNo AND d.dtrDate BETWEEN :fromDate AND :toDate")
//    List<DTR> fetchByEmployeeIdAndDateRange(
//            @Param("employeeId") String employeeId,
//            @Param("fromDate") LocalDateTime fromDate,
//            @Param("toDate") LocalDateTime toDate
//    );

}
