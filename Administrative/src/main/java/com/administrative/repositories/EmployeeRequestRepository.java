package com.administrative.repositories;

import com.administrative.entitymodels.EmployeeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeeRequestRepository extends JpaRepository<EmployeeRequest, Long> {
    Optional<EmployeeRequest> findByCodeIgnoreCase(String code);
}
