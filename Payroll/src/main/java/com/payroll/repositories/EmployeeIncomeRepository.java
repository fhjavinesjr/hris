package com.payroll.repositories;

import com.payroll.entitymodels.EmployeeIncome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeIncomeRepository extends JpaRepository<EmployeeIncome, Long> {

    @Query("SELECT i FROM EmployeeIncome i WHERE i.month = :month AND i.year = :year")
    List<EmployeeIncome> findByMonthAndYear(@Param("month") int month, @Param("year") int year);

    List<EmployeeIncome> findByEmployeeNoAndMonthAndYear(String employeeNo, int month, int year);
}
