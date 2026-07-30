package com.payroll.impl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportSignatoryPositionResolverTest {

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void resolvesLatestActiveAppointmentOnBothCompatibilityModes(String mode) {
        JdbcTemplate jdbcTemplate = jdbcTemplate(mode);
        createSchema(jdbcTemplate);
        jdbcTemplate.update("""
                INSERT INTO employee (employeeId, employeeNo, firstname, lastname, position)
                VALUES (1, '001', 'Maria', 'Santos', 'Employee Fallback')
                """);
        jdbcTemplate.update("""
                INSERT INTO payroll_detail (id, employeeNo, employeeName, department, salaryPeriodKey)
                VALUES (10, '001', 'MARIA SANTOS', 'Payroll Fallback', '2026-07')
                """);
        jdbcTemplate.update("INSERT INTO job_position (jobPositionId, jobPositionName) VALUES (100, 'Old Position')");
        jdbcTemplate.update("INSERT INTO job_position (jobPositionId, jobPositionName) VALUES (200, 'Current Position')");
        jdbcTemplate.update("""
                INSERT INTO employeeappointment
                    (employeeAppointmentId, employeeId, jobPositionId, assumptionToDutyDate, activeAppointment)
                VALUES (1000, 1, 100, TIMESTAMP '2026-06-01 00:00:00', FALSE)
                """);
        jdbcTemplate.update("""
                INSERT INTO employeeappointment
                    (employeeAppointmentId, employeeId, jobPositionId, assumptionToDutyDate, activeAppointment)
                VALUES (2000, 1, 200, TIMESTAMP '2025-01-01 00:00:00', TRUE)
                """);

        ReportSignatoryPositionResolver resolver = new ReportSignatoryPositionResolver(jdbcTemplate);

        assertEquals("Current Position", resolver.resolve("2026-07", "001", "Maria Santos"));
        assertEquals("Current Position", resolver.resolve("2026-07", "", "Santos Maria"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void fallsBackFromAppointmentToEmployeeThenPayrollDepartment(String mode) {
        JdbcTemplate jdbcTemplate = jdbcTemplate(mode);
        createSchema(jdbcTemplate);
        jdbcTemplate.update("""
                INSERT INTO employee (employeeId, employeeNo, firstname, lastname, position)
                VALUES (1, '001', 'Maria', 'Santos', 'Employee Fallback')
                """);
        jdbcTemplate.update("""
                INSERT INTO employee (employeeId, employeeNo, firstname, lastname, position)
                VALUES (2, '002', 'Juan', 'Dela Cruz', NULL)
                """);
        jdbcTemplate.update("""
                INSERT INTO payroll_detail (id, employeeNo, employeeName, department, salaryPeriodKey)
                VALUES (10, '001', 'MARIA SANTOS', 'First Department', '2026-07')
                """);
        jdbcTemplate.update("""
                INSERT INTO payroll_detail (id, employeeNo, employeeName, department, salaryPeriodKey)
                VALUES (20, '002', 'JUAN DELA CRUZ', 'Human Resource Management', '2026-07')
                """);

        ReportSignatoryPositionResolver resolver = new ReportSignatoryPositionResolver(jdbcTemplate);

        assertEquals("Employee Fallback", resolver.resolve("2026-07", "001", ""));
        assertEquals("Human Resource Management", resolver.resolve("2026-07", "", "Juan Dela Cruz"));
        assertEquals("", resolver.resolve("2026-07", "", ""));
    }

    private JdbcTemplate jdbcTemplate(String mode) {
        String databaseName = "signatory_" + UUID.randomUUID().toString().replace("-", "");
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + databaseName + ";MODE=" + mode + ";DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
        return new JdbcTemplate(dataSource);
    }

    private void createSchema(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                CREATE TABLE employee (
                    employeeId BIGINT PRIMARY KEY,
                    employeeNo VARCHAR(64),
                    firstname VARCHAR(255),
                    lastname VARCHAR(255),
                    position VARCHAR(255)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE payroll_detail (
                    id BIGINT PRIMARY KEY,
                    employeeNo VARCHAR(64),
                    employeeName VARCHAR(255),
                    department VARCHAR(255),
                    salaryPeriodKey VARCHAR(64)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE job_position (
                    jobPositionId BIGINT PRIMARY KEY,
                    jobPositionName VARCHAR(255)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE employeeappointment (
                    employeeAppointmentId BIGINT PRIMARY KEY,
                    employeeId BIGINT,
                    jobPositionId BIGINT,
                    assumptionToDutyDate TIMESTAMP,
                    activeAppointment BOOLEAN
                )
                """);
    }
}
