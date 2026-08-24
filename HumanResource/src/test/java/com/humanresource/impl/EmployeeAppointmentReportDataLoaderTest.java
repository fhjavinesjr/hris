package com.humanresource.impl;

import com.humanresource.dtos.PersonnelActionReportData;
import com.humanresource.entitymodels.Employee;
import com.humanresource.entitymodels.EmployeeAppointment;
import com.humanresource.repositories.EmployeeAppointmentRepository;
import com.humanresource.repositories.EmployeeRepository;
import com.humanresource.repositories.PersonalDataRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmployeeAppointmentReportDataLoaderTest {

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void mapsSelectedAppointmentAsToAndPriorAppointmentAsFrom(String compatibilityMode) {
        JdbcTemplate jdbc = jdbc(compatibilityMode);
        createReferenceSchema(jdbc);
        jdbc.update("INSERT INTO settings (settingsId, companyName, address) VALUES (1, ?, ?)",
                "ISOFT Test Agency", "Test Address");
        jdbc.update("INSERT INTO areas (areasId, areasName) VALUES (10, ?)", "Hospital Operations");
        jdbc.update("INSERT INTO businessunits (businessUnitsId, businessUnitsName) VALUES (20, ?)",
                "Patient Support");
        jdbc.update("INSERT INTO manage_personnel (id, employeeId, areaId, businessUnitId) VALUES (1, 7, 10, 20)");
        jdbc.update("INSERT INTO job_position (jobPositionId, jobPositionName) VALUES (100, ?), (101, ?)",
                "Accountant II", "Administrative Officer II");
        jdbc.update("INSERT INTO natureofappointment (natureofappointmentId, nature) VALUES (5, ?)",
                "PERMANENT");

        EmployeeAppointmentRepository appointments = mock(EmployeeAppointmentRepository.class);
        EmployeeRepository employees = mock(EmployeeRepository.class);
        PersonalDataRepository personalData = mock(PersonalDataRepository.class);

        EmployeeAppointment previous = appointment(1L, 7L, LocalDateTime.of(2025, 4, 14, 0, 0),
                100, new BigDecimal("18000"), "Initial Appointment");
        EmployeeAppointment selected = appointment(2L, 7L, LocalDateTime.of(2026, 4, 14, 0, 0),
                101, new BigDecimal("27000"), "Renewal of Appointment");
        Employee employee = new Employee();
        employee.setEmployeeId(7L);
        employee.setFirstname("Michelle S.");
        employee.setLastname("Fernando");

        when(appointments.findById(2L)).thenReturn(Optional.of(selected));
        when(appointments.findTop1ByEmployeeIdAndAssumptionToDutyDateBeforeOrderByAssumptionToDutyDateDescEmployeeAppointmentIdDesc(
                7L, selected.getAssumptionToDutyDate())).thenReturn(Optional.of(previous));
        when(employees.findById(7L)).thenReturn(Optional.of(employee));
        when(personalData.findByEmployeeId(7L)).thenReturn(null);

        PersonnelActionReportData result = new EmployeeAppointmentReportDataLoader(
                appointments, employees, personalData, jdbc).load(2L);

        assertEquals("Fernando, Michelle S.", result.getEmployeeName());
        assertEquals("Renewal of Appointment", result.getNatureOfAction());
        assertEquals("04/14/2026", result.getEffectiveDate());
        assertEquals("Accountant II", result.getFromPosition());
        assertEquals("Administrative Officer II", result.getToPosition());
        assertEquals("18,000.00", result.getFromSalary());
        assertEquals("27,000.00", result.getToSalary());
        assertEquals("Patient Support", result.getToSection());
        assertEquals("Hospital Operations", result.getToDivision());
        assertEquals("PERMANENT", result.getRemarks());
    }

    private static EmployeeAppointment appointment(
            Long id, Long employeeId, LocalDateTime assumptionDate, int positionId,
            BigDecimal salary, String details) {
        EmployeeAppointment appointment = new EmployeeAppointment();
        appointment.setEmployeeAppointmentId(id);
        appointment.setEmployeeId(employeeId);
        appointment.setAssumptionToDutyDate(assumptionDate);
        appointment.setAppointmentIssuedDate(assumptionDate.minusMonths(1));
        appointment.setJobPositionId(positionId);
        appointment.setNatureOfAppointmentId(5);
        appointment.setSalaryPerMonth(salary);
        appointment.setDetails(details);
        return appointment;
    }

    private static JdbcTemplate jdbc(String compatibilityMode) {
        String databaseName = "appointment_report_" + compatibilityMode.toLowerCase()
                + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:h2:mem:" + databaseName + ";MODE=" + compatibilityMode + ";DB_CLOSE_DELAY=-1";
        return new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
    }

    private static void createReferenceSchema(JdbcTemplate jdbc) {
        jdbc.execute("CREATE TABLE settings (settingsId BIGINT PRIMARY KEY, companyName VARCHAR(200), address VARCHAR(300), leftHeaderLogo VARBINARY(1000), rightHeaderLogo VARBINARY(1000))");
        jdbc.execute("CREATE TABLE areas (areasId BIGINT PRIMARY KEY, areasName VARCHAR(100))");
        jdbc.execute("CREATE TABLE businessunits (businessUnitsId BIGINT PRIMARY KEY, businessUnitsName VARCHAR(100))");
        jdbc.execute("CREATE TABLE manage_personnel (id BIGINT PRIMARY KEY, employeeId BIGINT, areaId BIGINT, businessUnitId BIGINT)");
        jdbc.execute("CREATE TABLE job_position (jobPositionId BIGINT PRIMARY KEY, jobPositionName VARCHAR(100))");
        jdbc.execute("CREATE TABLE natureofappointment (natureofappointmentId BIGINT PRIMARY KEY, nature VARCHAR(100))");
    }
}
