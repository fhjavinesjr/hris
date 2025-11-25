package com.administrative.repositories;

import com.administrative.entitymodels.SalarySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SalaryScheduleRepository extends JpaRepository<SalarySchedule, Long> {

    void deleteByEffectivityDate(LocalDateTime effectivityDate);

    @Query(value = "SELECT TOP 1 *  FROM salary_schedule s WHERE s.effectivityDate <= :assumptionToDutyDate and s.salaryGrade = :salaryGrade and s.salaryStep = :salaryStep ORDER BY s.effectivityDate DESC", nativeQuery = true)
    SalarySchedule findByEffectivityDateAndSalaryGradeAndSalaryStep(@Param("assumptionToDutyDate") LocalDateTime assumptionToDutyDate, @Param("salaryGrade") Long salaryGrade, @Param("salaryStep") Long salaryStep);

}
