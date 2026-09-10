package com.humanresource.repositories;

import com.humanresource.entitymodels.EmployeeAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface EmployeeAppointmentRepository extends JpaRepository<EmployeeAppointment, Long> {

    List<EmployeeAppointment> findByJobPositionId(Long jobPositionId);

    EmployeeAppointment findTop1ByEmployeeIdOrderByAssumptionToDutyDateDesc(Long employeeId);

    EmployeeAppointment findTop1ByEmployeeIdAndActiveAppointmentTrueOrderByAssumptionToDutyDateDesc(Long employeeId);

    List<EmployeeAppointment> findByEmployeeId(Long employeeId);

    boolean existsByPlantillaIdAndActiveAppointmentTrue(Long plantillaId);

    Optional<EmployeeAppointment> findTop1ByPlantillaIdAndActiveAppointmentTrueOrderByAssumptionToDutyDateDescEmployeeAppointmentIdDesc(Long plantillaId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from EmployeeAppointment a where a.plantillaId=:plantillaId and a.activeAppointment=true")
    List<EmployeeAppointment> findActiveByPlantillaForUpdate(@Param("plantillaId") Integer plantillaId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from EmployeeAppointment a where a.employeeId=:employeeId and a.activeAppointment=true")
    List<EmployeeAppointment> findActiveByEmployeeForUpdate(@Param("employeeId") Long employeeId);

    Optional<EmployeeAppointment> findTop1ByEmployeeIdAndAssumptionToDutyDateBeforeOrderByAssumptionToDutyDateDescEmployeeAppointmentIdDesc(
            Long employeeId,
            LocalDateTime assumptionToDutyDate
    );

}
