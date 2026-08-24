package com.humanresource.repositories;

import com.humanresource.entitymodels.EmployeeAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmployeeAppointmentRepository extends JpaRepository<EmployeeAppointment, Long> {

    List<EmployeeAppointment> findByJobPositionId(Long jobPositionId);

    EmployeeAppointment findTop1ByEmployeeIdOrderByAssumptionToDutyDateDesc(Long employeeId);

    EmployeeAppointment findTop1ByEmployeeIdAndActiveAppointmentTrueOrderByAssumptionToDutyDateDesc(Long employeeId);

    List<EmployeeAppointment> findByEmployeeId(Long employeeId);

    boolean existsByPlantillaIdAndActiveAppointmentTrue(Long plantillaId);

    Optional<EmployeeAppointment> findTop1ByEmployeeIdAndAssumptionToDutyDateBeforeOrderByAssumptionToDutyDateDescEmployeeAppointmentIdDesc(
            Long employeeId,
            LocalDateTime assumptionToDutyDate
    );

}
