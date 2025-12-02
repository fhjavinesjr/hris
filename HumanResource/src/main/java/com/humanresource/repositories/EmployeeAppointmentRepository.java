package com.humanresource.repositories;

import com.humanresource.entitymodels.EmployeeAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeAppointmentRepository extends JpaRepository<EmployeeAppointment, Long> {

    List<EmployeeAppointment> findByJobPositionId(Long jobPositionId);

    EmployeeAppointment findTop1ByEmployeeIdOrderByAssumptionToDutyDateDesc(Long employeeId);

    List<EmployeeAppointment> findByEmployeeId(Long employeeId);

}
