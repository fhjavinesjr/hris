package com.timekeeping.repositories;

import com.timekeeping.entitymodels.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Avoid using raw SQL queries to prevent SQL injection attacks.

    Optional<Employee> findByEmployeeNo(String employeeNo);
}
