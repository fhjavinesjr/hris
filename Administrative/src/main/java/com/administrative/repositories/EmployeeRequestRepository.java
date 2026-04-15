package com.administrative.repositories;

import com.administrative.entitymodels.EmployeeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRequestRepository extends JpaRepository<EmployeeRequest, Long> {

}
