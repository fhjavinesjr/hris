package com.humanresource.reports;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintFrame;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaveFormPortablePdfSmokeTest {

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void fillsApprovedLeaveWithCertifiedCreditsInBothCompatibilityModes(String mode) throws Exception {
        try (Connection connection = openDatabase(mode);
             Statement statement = connection.createStatement()) {
            createSchema(statement);
            insertRepresentativeData(statement);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("LEAVE_APPLICATION_ID", 100L);
            parameters.put("LEAVE_MONETIZATION_ID", null);
            parameters.put("WORKING_DAYS_APPLIED", 2.0);
            parameters.put("INCLUSIVE_DATES", "09/21/2026 - 09/22/2026");
            parameters.put("CREDIT_DATE_AS_OF", Date.valueOf("2026-08-31"));
            parameters.put("CERTIFIED_VL_CREDIT", 18.0);
            parameters.put("CERTIFIED_SL_CREDIT", 20.0);
            parameters.put("LESS_VL_THIS_APPLICATION", 0.0);
            parameters.put("LESS_SL_THIS_APPLICATION", 0.0);
            parameters.put("LEAVE_TYPE_CODE", 5);
            parameters.put("OTHER_LEAVE_TYPE", "");
            parameters.put("VL_IN_COUNTRY", 0);
            parameters.put("VL_ABROAD", 0);
            parameters.put("VL_LOCATION", "");
            parameters.put("SL_IN_HOSPITAL", 0);
            parameters.put("SL_OUT_PATIENT", 0);
            parameters.put("SL_ILLNESS", "");
            parameters.put("WOMEN_ILLNESS", "");
            parameters.put("STUDY_LEAVE_CASE", 0);
            parameters.put("logoleft", null);
            parameters.put("logoright", null);

            JasperPrint print = fill("reports/leave_form_2020.jrxml", parameters, connection);
            byte[] pdf = JasperExportManager.exportReportToPdf(print);
            List<String> renderedText = renderedText(print);

            assertFalse(print.getPages().isEmpty());
            assertArrayEquals("%PDF".getBytes(), java.util.Arrays.copyOf(pdf, 4));
            assertTrue(renderedText.contains("18.000"));
            assertTrue(renderedText.contains("20.000"));
            assertTrue(renderedText.contains("08/31/2026"));
            assertEquals(1, renderedText.stream()
                    .filter("CARLOS AQUINO"::equals)
                    .count());
        }
    }

    private static void createSchema(Statement statement) throws Exception {
        statement.execute("""
                CREATE TABLE settings (
                    settingsId BIGINT PRIMARY KEY,
                    companyName VARCHAR(200), address VARCHAR(300), hospitalAgency BOOLEAN
                )
                """);
        statement.execute("""
                CREATE TABLE employee (
                    employeeId BIGINT PRIMARY KEY, employeeNo VARCHAR(50),
                    firstname VARCHAR(100), lastname VARCHAR(100), position VARCHAR(150)
                )
                """);
        statement.execute("""
                CREATE TABLE personaldata (
                    employeeId BIGINT PRIMARY KEY, surname VARCHAR(100),
                    firstname VARCHAR(100), middlename VARCHAR(100)
                )
                """);
        statement.execute("""
                CREATE TABLE employeeappointment (
                    employeeAppointmentId BIGINT PRIMARY KEY, employeeId BIGINT,
                    salaryPerMonth DECIMAL(18,2), jobPositionId BIGINT,
                    assumptionToDutyDate DATE
                )
                """);
        statement.execute("""
                CREATE TABLE job_position (
                    jobPositionId BIGINT PRIMARY KEY, jobPositionName VARCHAR(150)
                )
                """);
        statement.execute("""
                CREATE TABLE leave_application (
                    leaveApplicationId BIGINT PRIMARY KEY, employeeId BIGINT,
                    dateFiled DATE, leaveType VARCHAR(100), startDate DATE, endDate DATE,
                    noOfDays FLOAT, details VARCHAR(500), commutation VARCHAR(50),
                    approvedStatus VARCHAR(50), approvalMessage VARCHAR(500),
                    recommendationStatus VARCHAR(50), recommendationMessage VARCHAR(500),
                    recommendingApprovalById BIGINT, authorizedOfficialId BIGINT,
                    approvedById BIGINT, withPay BOOLEAN
                )
                """);
        statement.execute("""
                CREATE TABLE leave_monetization (
                    leaveMonetizationId BIGINT PRIMARY KEY, employeeId BIGINT,
                    dateFiled DATE, totalDays FLOAT, reason VARCHAR(500),
                    approvalStatus VARCHAR(50), approvalRemarks VARCHAR(500),
                    recommendationStatus VARCHAR(50), recommendationRemarks VARCHAR(500),
                    recommendedById BIGINT, approvedById BIGINT,
                    noOfDaysSL FLOAT, noOfDaysVL FLOAT
                )
                """);
    }

    private static void insertRepresentativeData(Statement statement) throws Exception {
        statement.execute("""
                INSERT INTO settings (settingsId, companyName, address, hospitalAgency)
                VALUES (1, 'ISOFT Test Agency', 'Test Address', FALSE)
                """);
        statement.execute("""
                INSERT INTO employee (employeeId, employeeNo, firstname, lastname, position)
                VALUES
                    (1, '202600001', 'Maria', 'Reyes', 'Supervisor'),
                    (6, 'EMP-00006', 'Carlos', 'Aquino', 'Pharmacist II')
                """);
        statement.execute("""
                INSERT INTO personaldata (employeeId, surname, firstname, middlename)
                VALUES (1, 'Reyes', 'Maria', 'Test')
                """);
        statement.execute("""
                INSERT INTO job_position (jobPositionId, jobPositionName)
                VALUES (1, 'Supervisor')
                """);
        statement.execute("""
                INSERT INTO employeeappointment
                    (employeeAppointmentId, employeeId, salaryPerMonth,
                     jobPositionId, assumptionToDutyDate)
                VALUES (1, 1, 30000.00, 1, DATE '2025-01-01')
                """);
        statement.execute("""
                INSERT INTO leave_application
                    (leaveApplicationId, employeeId, dateFiled, leaveType,
                     startDate, endDate, noOfDays, details, commutation,
                     approvedStatus, approvalMessage, recommendationStatus,
                     recommendationMessage, recommendingApprovalById,
                     authorizedOfficialId, approvedById, withPay)
                VALUES
                    (100, 1, DATE '2026-09-13', 'Forced Leave',
                     DATE '2026-09-21', DATE '2026-09-22', 2, '', 'not requested',
                     'Approved', '', NULL, '', NULL, NULL, 6, FALSE)
                """);
    }

    private static Connection openDatabase(String mode) throws Exception {
        String databaseName = "leave_form_" + mode.toLowerCase() + "_"
                + UUID.randomUUID().toString().replace("-", "");
        return DriverManager.getConnection(
                "jdbc:h2:mem:" + databaseName + ";MODE=" + mode + ";DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
    }

    private static JasperPrint fill(String reportPath,
                                    Map<String, Object> parameters,
                                    Connection connection) throws Exception {
        ClassPathResource resource = new ClassPathResource(reportPath);
        try (InputStream input = resource.getInputStream()) {
            JasperReport report = JasperCompileManager.compileReport(input);
            return JasperFillManager.fillReport(report, parameters, connection);
        }
    }

    private static List<String> renderedText(JasperPrint print) {
        List<String> text = new ArrayList<>();
        print.getPages().forEach(page -> collectText(page.getElements(), text));
        return text;
    }

    private static void collectText(List<JRPrintElement> elements, List<String> text) {
        for (JRPrintElement element : elements) {
            if (element instanceof JRPrintText printText) {
                text.add(printText.getFullText());
            } else if (element instanceof JRPrintFrame frame) {
                collectText(frame.getElements(), text);
            }
        }
    }
}
