package com.humanresource.repositories;

import com.humanresource.entitymodels.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.time.LocalDateTime;
import com.humanresource.integration.primehr.AssessmentSubjectRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Avoid using raw SQL queries to prevent SQL injection attacks.

    Optional<Employee> findByEmployeeNo(String employeeNo);

    @Query(value = "select new com.humanresource.integration.primehr.AssessmentSubjectRow(" +
            "e.employeeId, e.employeeNo, e.firstname, e.lastname, e.suffix, e.updatedAt, " +
            "a.employeeAppointmentId, a.assumptionToDutyDate, a.jobPositionId, a.plantillaId) " +
            "from Employee e join EmployeeAppointment a on a.employeeId = e.employeeId " +
            "where a.activeAppointment = true " +
            "and not exists (select s.separationId from Separation s " +
            "where s.employeeId = e.employeeId and s.separationDate <= :asOf) " +
            "and (:search = '' or lower(e.employeeNo) like concat('%', :search, '%') " +
            "or lower(e.firstname) like concat('%', :search, '%') " +
            "or lower(e.lastname) like concat('%', :search, '%'))",
            countQuery = "select count(e.employeeId) from Employee e " +
                    "join EmployeeAppointment a on a.employeeId = e.employeeId " +
                    "where a.activeAppointment = true " +
                    "and not exists (select s.separationId from Separation s " +
                    "where s.employeeId = e.employeeId and s.separationDate <= :asOf) " +
                    "and (:search = '' or lower(e.employeeNo) like concat('%', :search, '%') " +
                    "or lower(e.firstname) like concat('%', :search, '%') " +
                    "or lower(e.lastname) like concat('%', :search, '%'))")
    Page<AssessmentSubjectRow> findPrimeHrAssessmentSubjects(@Param("search") String search,
            @Param("asOf") LocalDateTime asOf, Pageable pageable);

    @Query("select new com.humanresource.integration.primehr.AssessmentSubjectRow(" +
            "e.employeeId, e.employeeNo, e.firstname, e.lastname, e.suffix, e.updatedAt, " +
            "a.employeeAppointmentId, a.assumptionToDutyDate, a.jobPositionId, a.plantillaId) " +
            "from Employee e join EmployeeAppointment a on a.employeeId = e.employeeId " +
            "where e.employeeId = :employeeId and a.activeAppointment = true " +
            "and not exists (select s.separationId from Separation s " +
            "where s.employeeId = e.employeeId and s.separationDate <= :asOf)")
    Optional<AssessmentSubjectRow> findPrimeHrAssessmentSubject(@Param("employeeId") Long employeeId,
            @Param("asOf") LocalDateTime asOf);
}
