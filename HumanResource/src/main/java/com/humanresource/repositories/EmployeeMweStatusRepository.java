package com.humanresource.repositories;

import com.humanresource.entitymodels.EmployeeMweStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeMweStatusRepository extends JpaRepository<EmployeeMweStatus, Long> {
    List<EmployeeMweStatus> findByEmployeeIdOrderByEffectiveFromDesc(Long employeeId);
    List<EmployeeMweStatus> findAllByOrderByEmployeeIdAscEffectiveFromDesc();
}
