package com.payroll.reports;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PortablePayrollPdfSmokeTest {

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void fillsPayslipAndHazardDutyPdfsInBothCompatibilityModes(String mode) throws Exception {
        try (Connection connection = openDatabase(mode);
             Statement statement = connection.createStatement()) {
            createSchema(statement);
            insertRepresentativePayroll(statement);

            JasperPrint payslip = fill(
                    "reports/payslip.jrxml",
                    Map.of("payrollDetailId", 10L),
                    connection
            );

            Map<String, Object> hazardParameters = new HashMap<>();
            hazardParameters.put("SALARY_PERIOD_KEY", "2026-07");
            hazardParameters.put("EARNING_TYPE_ID", "1");
            hazardParameters.put("EARNING_TYPE_NAME", "Hazard Pay");
            hazardParameters.put("EARNING_TYPE_CODE", "HAZ");
            hazardParameters.put("CATEGORY", "Hazard Pay");
            hazardParameters.put("CURRENT_COMPANY", "ISOFT Test Agency");
            hazardParameters.put("REPORT_PERIOD_LABEL", "July 2026");
            JasperPrint hazard = fill(
                    "reports/hazarddutyreport.jrxml",
                    hazardParameters,
                    connection
            );

            assertPdf(payslip);
            assertPdf(hazard);
        }
    }

    private void createSchema(Statement statement) throws Exception {
        statement.execute("""
                CREATE TABLE payroll_detail (
                    id BIGINT PRIMARY KEY,
                    employeeNo VARCHAR(100),
                    employeeName VARCHAR(200),
                    department VARCHAR(200),
                    salaryGrade INTEGER,
                    salaryStep INTEGER,
                    salaryPeriodKey VARCHAR(100),
                    cutoffStartDate DATE,
                    cutoffEndDate DATE,
                    salaryDate DATE,
                    basicPerSalary DOUBLE,
                    salaryPerDay DOUBLE,
                    salaryPerMinute DOUBLE,
                    actualBasic DOUBLE,
                    grossAmount DOUBLE,
                    totalDeduction DOUBLE,
                    netAmount DOUBLE,
                    taxableIncome DOUBLE,
                    taxAmount DOUBLE,
                    lateMinutes INTEGER,
                    lateValue DOUBLE,
                    undertimeMinutes INTEGER,
                    undertimeValue DOUBLE,
                    absentDays DOUBLE,
                    absentParticulars VARCHAR(300),
                    vacationLeaveUsed DOUBLE,
                    sickLeaveUsed DOUBLE,
                    forceLeaveUsed DOUBLE,
                    vlBalance DOUBLE,
                    slBalance DOUBLE,
                    status VARCHAR(100),
                    isLocked BOOLEAN
                )
                """);
        statement.execute("""
                CREATE TABLE payroll_detail_earning (
                    id BIGINT PRIMARY KEY,
                    payroll_detail_id BIGINT,
                    earningCode VARCHAR(100),
                    earningName VARCHAR(200),
                    amount DOUBLE,
                    indexNo INTEGER
                )
                """);
        statement.execute("""
                CREATE TABLE payroll_detail_deduction (
                    id BIGINT PRIMARY KEY,
                    payroll_detail_id BIGINT,
                    deductionCode VARCHAR(100),
                    deductionName VARCHAR(200),
                    amount DOUBLE,
                    employerShare DOUBLE,
                    reference VARCHAR(200),
                    loanPaymentsMade INTEGER,
                    loanTotalAmount DOUBLE,
                    indexNo INTEGER
                )
                """);
        statement.execute("""
                CREATE TABLE payroll_adjustment_header (
                    id BIGINT PRIMARY KEY,
                    employeeNo VARCHAR(100),
                    salaryPeriodKey VARCHAR(100),
                    status VARCHAR(100),
                    authorityNo VARCHAR(200)
                )
                """);
        statement.execute("""
                CREATE TABLE payroll_adjustment_line (
                    id BIGINT PRIMARY KEY,
                    header_id BIGINT,
                    type VARCHAR(100),
                    amount DOUBLE,
                    code VARCHAR(100),
                    name VARCHAR(200),
                    indexNo INTEGER
                )
                """);
    }

    private void insertRepresentativePayroll(Statement statement) throws Exception {
        statement.execute("""
                INSERT INTO payroll_detail (
                    id, employeeNo, employeeName, department, salaryGrade, salaryStep,
                    salaryPeriodKey, cutoffStartDate, cutoffEndDate, salaryDate,
                    basicPerSalary, salaryPerDay, salaryPerMinute, actualBasic,
                    grossAmount, totalDeduction, netAmount, taxableIncome, taxAmount,
                    lateMinutes, lateValue, undertimeMinutes, undertimeValue,
                    absentDays, absentParticulars, vacationLeaveUsed, sickLeaveUsed,
                    forceLeaveUsed, vlBalance, slBalance, status, isLocked
                ) VALUES (
                    10, '001', 'FERDINAND JAVINES', 'Human Resource Management', 12, 1,
                    '2026-07', DATE '2026-07-01', DATE '2026-07-15', DATE '2026-07-23',
                    30000, 1000, 2.08, 15000,
                    17000, 2000, 15000, 14000, 1000,
                    0, 0, 0, 0,
                    0, '', 0, 0,
                    0, 5, 5, 'FINAL / RELEASED', TRUE
                )
                """);
        statement.execute("""
                INSERT INTO payroll_detail_earning
                    (id, payroll_detail_id, earningCode, earningName, amount, indexNo)
                VALUES
                    (1, 10, 'HAZ', 'Hazard Pay', 2000, 1)
                """);
        statement.execute("""
                INSERT INTO payroll_detail_deduction
                    (id, payroll_detail_id, deductionCode, deductionName, amount,
                     employerShare, reference, loanPaymentsMade, loanTotalAmount, indexNo)
                VALUES
                    (1, 10, 'GSIS', 'GSIS Contribution', 1000,
                     1000, '', NULL, NULL, 1)
                """);
    }

    private Connection openDatabase(String mode) throws Exception {
        String databaseName = "payroll_" + mode.toLowerCase() + "_"
                + UUID.randomUUID().toString().replace("-", "");
        return DriverManager.getConnection(
                "jdbc:h2:mem:" + databaseName + ";MODE=" + mode + ";DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
    }

    private JasperPrint fill(String reportPath,
                             Map<String, Object> parameters,
                             Connection connection) throws Exception {
        ClassPathResource resource = new ClassPathResource(reportPath);
        try (InputStream input = resource.getInputStream()) {
            JasperReport report = JasperCompileManager.compileReport(input);
            return JasperFillManager.fillReport(
                    report,
                    new HashMap<>(parameters),
                    connection
            );
        }
    }

    private void assertPdf(JasperPrint print) throws Exception {
        byte[] pdf = JasperExportManager.exportReportToPdf(print);
        assertFalse(print.getPages().isEmpty());
        assertArrayEquals("%PDF".getBytes(), java.util.Arrays.copyOf(pdf, 4));
    }
}
