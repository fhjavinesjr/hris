package com.payroll.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Resolves report signatory positions outside Jasper SQL so the templates do not
 * need database-specific TOP/OUTER APPLY expressions.
 */
@Component
public class ReportSignatoryPositionResolver {

    private static final String FIND_EMPLOYEE_BY_NUMBER = """
            SELECT e.employeeId, COALESCE(e.position, '') AS employeePosition
            FROM employee e
            WHERE e.employeeNo = ?
            ORDER BY e.employeeId
            """;

    private static final String FIND_EMPLOYEE_BY_NAME = """
            SELECT e.employeeId, COALESCE(e.position, '') AS employeePosition
            FROM employee e
            WHERE UPPER(REPLACE(REPLACE(REPLACE(LTRIM(RTRIM(CONCAT(
                      COALESCE(e.firstname, ''), ' ', COALESCE(e.lastname, '')
                  ))), ' ', ''), '.', ''), '-', '')) = ?
               OR UPPER(REPLACE(REPLACE(REPLACE(LTRIM(RTRIM(CONCAT(
                      COALESCE(e.lastname, ''), ' ', COALESCE(e.firstname, '')
                  ))), ' ', ''), '.', ''), '-', '')) = ?
            ORDER BY e.employeeId
            """;

    private static final String FIND_EMPLOYEE_BY_PAYROLL_NAME = """
            SELECT e.employeeId, COALESCE(e.position, '') AS employeePosition
            FROM payroll_detail pd
            INNER JOIN employee e ON e.employeeNo = pd.employeeNo
            WHERE pd.salaryPeriodKey = ?
              AND UPPER(REPLACE(REPLACE(REPLACE(LTRIM(RTRIM(COALESCE(pd.employeeName, ''))),
                    ' ', ''), '.', ''), '-', '')) = ?
            ORDER BY pd.id DESC, e.employeeId
            """;

    private static final String FIND_APPOINTMENTS = """
            SELECT
                COALESCE(jp.jobPositionName, '') AS appointmentPosition,
                ea.activeAppointment
            FROM employeeappointment ea
            LEFT JOIN job_position jp ON jp.jobPositionId = ea.jobPositionId
            WHERE ea.employeeId = ?
            ORDER BY
                CASE WHEN ea.assumptionToDutyDate IS NULL THEN 1 ELSE 0 END,
                ea.assumptionToDutyDate DESC,
                ea.employeeAppointmentId DESC
            """;

    private static final String FIND_PAYROLL_DEPARTMENT = """
            SELECT COALESCE(pd.department, '') AS department
            FROM payroll_detail pd
            WHERE pd.salaryPeriodKey = ?
              AND pd.employeeNo = ?
            ORDER BY pd.id DESC
            """;

    private static final String FIND_EMPLOYEE_NUMBER = """
            SELECT COALESCE(e.employeeNo, '') AS employeeNo
            FROM employee e
            WHERE e.employeeId = ?
            ORDER BY e.employeeId
            """;

    private final JdbcTemplate jdbcTemplate;

    public ReportSignatoryPositionResolver(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String resolve(String salaryPeriodKey, String employeeNo, String employeeName) {
        EmployeeMatch employee = findEmployee(salaryPeriodKey, employeeNo, employeeName);
        if (employee == null) {
            return "";
        }

        List<AppointmentMatch> appointments = jdbcTemplate.query(
                FIND_APPOINTMENTS,
                (rs, rowNum) -> new AppointmentMatch(
                        clean(rs.getString(1)),
                        booleanValue(rs.getObject(2))
                ),
                employee.employeeId()
        );
        if (!appointments.isEmpty()) {
            AppointmentMatch appointment = appointments.stream()
                    .filter(AppointmentMatch::active)
                    .findFirst()
                    .orElse(appointments.get(0));
            String appointmentPosition = appointment.position();
            if (!appointmentPosition.isEmpty()) {
                return appointmentPosition;
            }
        }

        if (!employee.employeePosition().isEmpty()) {
            return employee.employeePosition();
        }

        String resolvedEmployeeNo = clean(employeeNo);
        if (resolvedEmployeeNo.isEmpty()) {
            List<String> employeeNumbers = jdbcTemplate.query(
                    FIND_EMPLOYEE_NUMBER,
                    (rs, rowNum) -> clean(rs.getString(1)),
                    employee.employeeId()
            );
            if (!employeeNumbers.isEmpty()) {
                resolvedEmployeeNo = employeeNumbers.get(0);
            }
        }

        if (clean(salaryPeriodKey).isEmpty() || resolvedEmployeeNo.isEmpty()) {
            return "";
        }

        List<String> departments = jdbcTemplate.query(
                FIND_PAYROLL_DEPARTMENT,
                (rs, rowNum) -> clean(rs.getString(1)),
                salaryPeriodKey.trim(),
                resolvedEmployeeNo
        );
        return departments.isEmpty() ? "" : departments.get(0);
    }

    private EmployeeMatch findEmployee(String salaryPeriodKey, String employeeNo, String employeeName) {
        String cleanEmployeeNo = clean(employeeNo);
        if (!cleanEmployeeNo.isEmpty()) {
            List<EmployeeMatch> matches = jdbcTemplate.query(
                    FIND_EMPLOYEE_BY_NUMBER,
                    (rs, rowNum) -> new EmployeeMatch(rs.getLong(1), clean(rs.getString(2))),
                    cleanEmployeeNo
            );
            if (!matches.isEmpty()) {
                return matches.get(0);
            }
        }

        String normalizedName = normalize(employeeName);
        if (normalizedName.isEmpty()) {
            return null;
        }

        List<EmployeeMatch> employeeNameMatches = jdbcTemplate.query(
                FIND_EMPLOYEE_BY_NAME,
                (rs, rowNum) -> new EmployeeMatch(rs.getLong(1), clean(rs.getString(2))),
                normalizedName,
                normalizedName
        );
        if (!employeeNameMatches.isEmpty()) {
            return employeeNameMatches.get(0);
        }

        if (clean(salaryPeriodKey).isEmpty()) {
            return null;
        }

        List<EmployeeMatch> payrollNameMatches = jdbcTemplate.query(
                FIND_EMPLOYEE_BY_PAYROLL_NAME,
                (rs, rowNum) -> new EmployeeMatch(rs.getLong(1), clean(rs.getString(2))),
                salaryPeriodKey.trim(),
                normalizedName
        );
        return payrollNameMatches.isEmpty() ? null : payrollNameMatches.get(0);
    }

    private String normalize(String value) {
        return clean(value)
                .replace(" ", "")
                .replace(".", "")
                .replace("-", "")
                .toUpperCase(Locale.ROOT);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean booleanValue(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue() != 0;
        }
        return value != null && Boolean.parseBoolean(value.toString());
    }

    private record EmployeeMatch(long employeeId, String employeePosition) {
    }

    private record AppointmentMatch(String position, boolean active) {
    }
}
