package com.payroll.reports;

import net.sf.jasperreports.engine.JasperCompileManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayrollLegacyReportPortabilityTest {

    private static final List<String> REPORTS = List.of(
            "reports/generic_payroll_earning.jrxml",
            "reports/generic_payroll_deduction.jrxml",
            "reports/gsis_remittance.jrxml",
            "reports/pagibigmembershipremittance.jrxml",
            "reports/philhealth_remittance.jrxml"
    );

    private static final Pattern SQL_QUERY = Pattern.compile(
            "<query\\s+language=\"SQL\"><!\\[CDATA\\[(.*?)]]></query>",
            Pattern.DOTALL
    );

    private static final Pattern VENDOR_SPECIFIC_SQL = Pattern.compile(
            "(?i)\\bTOP\\s+\\d+|\\bISNULL\\s*\\(|\\b(?:OUTER|CROSS)\\s+APPLY\\b"
                    + "|\\bTRY_CAST\\s*\\(|\\bCONVERT\\s*\\(|\\bDATEADD\\s*\\("
                    + "|\\bDATEDIFF\\s*\\(|\\bDATENAME\\s*\\(|\\bFORMAT\\s*\\("
                    + "|sys\\.all_objects|::[a-z_][a-z0-9_]*|\\bILIKE\\b|\\bLIMIT\\s+\\d+",
            Pattern.CASE_INSENSITIVE
    );

    @ParameterizedTest
    @MethodSource("reports")
    void compilesAndContainsNoVendorSpecificSql(String reportPath) throws Exception {
        String jrxml = readReport(reportPath);
        Matcher query = SQL_QUERY.matcher(jrxml);

        assertTrue(query.find(), "SQL query missing from " + reportPath);
        assertFalse(VENDOR_SPECIFIC_SQL.matcher(query.group(1)).find(),
                "Vendor-specific SQL remains in " + reportPath);

        try (InputStream input = resource(reportPath)) {
            assertNotNull(JasperCompileManager.compileReport(input));
        }
    }

    @ParameterizedTest
    @MethodSource("reportAndMode")
    void queryParsesAndExecutesInBothCompatibilityModes(ReportMode reportMode) throws Exception {
        JdbcTemplate jdbcTemplate = jdbcTemplate(reportMode.mode());
        createReportSchema(jdbcTemplate);
        insertRepresentativeData(jdbcTemplate);

        String query = materializeQuery(reportMode.reportPath());

        List<Integer> rows = jdbcTemplate.query(query, (rs, rowNum) -> rowNum);
        assertFalse(rows.isEmpty(), "Representative query returned no rows: " + reportMode);
    }

    @ParameterizedTest
    @MethodSource("reportAndMode")
    void lockedReportExcludesPendingAdjustments(ReportMode reportMode) throws Exception {
        JdbcTemplate jdbcTemplate = jdbcTemplate(reportMode.mode());
        createReportSchema(jdbcTemplate);
        insertRepresentativeData(jdbcTemplate);
        insertPendingAdjustments(jdbcTemplate);
        String query = materializeQuery(reportMode.reportPath());
        int amountColumn = amountColumn(reportMode.reportPath());

        double unlockedAmount = jdbcTemplate.query(query, rs -> {
            assertTrue(rs.next(), "Unlocked report returned no rows: " + reportMode);
            return rs.getDouble(amountColumn);
        });

        jdbcTemplate.update("INSERT INTO payroll_period_lock (salaryPeriodKey) VALUES ('2026-07')");
        double lockedAmount = jdbcTemplate.query(query, rs -> {
            assertTrue(rs.next(), "Locked report returned no rows: " + reportMode);
            return rs.getDouble(amountColumn);
        });

        assertTrue(unlockedAmount > lockedAmount,
                "Pending adjustment was not excluded after lock: " + reportMode);
    }

    private static Stream<String> reports() {
        return REPORTS.stream();
    }

    private static Stream<ReportMode> reportAndMode() {
        return REPORTS.stream()
                .flatMap(report -> Stream.of(
                        new ReportMode(report, "PostgreSQL"),
                        new ReportMode(report, "MSSQLServer")
                ));
    }

    private String readReport(String reportPath) throws Exception {
        try (InputStream input = resource(reportPath)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private InputStream resource(String reportPath) {
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(reportPath);
        assertNotNull(input, "Missing report resource: " + reportPath);
        return input;
    }

    private String extractQuery(String jrxml) {
        Matcher matcher = SQL_QUERY.matcher(jrxml);
        assertTrue(matcher.find(), "SQL query missing");
        return matcher.group(1);
    }

    private String materializeQuery(String reportPath) throws Exception {
        return extractQuery(readReport(reportPath))
                .replace("$P{SALARY_PERIOD_KEY}", "'2026-07'")
                .replace("$P{EARNING_TYPE_CODE}", "'HAZ'")
                .replace("$P{EARNING_TYPE_NAME}", "'Hazard Pay'")
                .replace("$P{DEDUCTION_TYPE_CODE}", "'GSIS'")
                .replace("$P{DEDUCTION_TYPE_NAME}", "'GSIS'")
                .replace("$P{salaryPeriodKey}", "'2026-07'");
    }

    private int amountColumn(String reportPath) {
        if (reportPath.endsWith("generic_payroll_earning.jrxml")) {
            return 8;
        }
        if (reportPath.endsWith("generic_payroll_deduction.jrxml")) {
            return 8;
        }
        if (reportPath.endsWith("gsis_remittance.jrxml")) {
            return 8;
        }
        if (reportPath.endsWith("pagibigmembershipremittance.jrxml")) {
            return 2;
        }
        if (reportPath.endsWith("philhealth_remittance.jrxml")) {
            return 3;
        }
        throw new IllegalArgumentException("Unknown report: " + reportPath);
    }

    private JdbcTemplate jdbcTemplate(String mode) {
        String databaseName = "reports_" + UUID.randomUUID().toString().replace("-", "");
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + databaseName + ";MODE=" + mode + ";DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
        return new JdbcTemplate(dataSource);
    }

    private void createReportSchema(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                CREATE TABLE payroll_period_lock (
                    salaryPeriodKey VARCHAR(64)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE payroll_detail (
                    id BIGINT PRIMARY KEY,
                    employeeNo VARCHAR(64),
                    employeeName VARCHAR(255),
                    department VARCHAR(255),
                    salaryGrade INTEGER,
                    salaryStep INTEGER,
                    basicPerSalary DOUBLE PRECISION,
                    salaryPeriodKey VARCHAR(64)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE payroll_detail_earning (
                    payroll_detail_id BIGINT,
                    earningCode VARCHAR(64),
                    earningName VARCHAR(255),
                    amount DOUBLE PRECISION
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE payroll_detail_deduction (
                    payroll_detail_id BIGINT,
                    deductionCode VARCHAR(64),
                    deductionName VARCHAR(255),
                    amount DOUBLE PRECISION,
                    employerShare DOUBLE PRECISION,
                    reference VARCHAR(255),
                    loanTotalAmount DOUBLE PRECISION,
                    loanPaymentsMade INTEGER
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE payroll_adjustment_header (
                    id BIGINT PRIMARY KEY,
                    employeeNo VARCHAR(64),
                    employeeName VARCHAR(255),
                    salaryPeriodKey VARCHAR(64),
                    status VARCHAR(64),
                    authorityNo VARCHAR(255)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE payroll_adjustment_line (
                    header_id BIGINT,
                    type VARCHAR(64),
                    code VARCHAR(64),
                    name VARCHAR(255),
                    amount DOUBLE PRECISION
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE employee (
                    employeeId BIGINT PRIMARY KEY,
                    employeeNo VARCHAR(64)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE personaldata (
                    employeeId BIGINT,
                    gsisId VARCHAR(64),
                    pagibigId VARCHAR(64),
                    philhealthNo VARCHAR(64),
                    surname VARCHAR(255),
                    firstname VARCHAR(255),
                    middlename VARCHAR(255),
                    extname VARCHAR(64),
                    dob DATE
                )
                """);
    }

    private void insertRepresentativeData(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("""
                INSERT INTO payroll_detail
                    (id, employeeNo, employeeName, department, salaryGrade, salaryStep,
                     basicPerSalary, salaryPeriodKey)
                VALUES (1, '001', 'SANTOS, MARIA', 'HRM', 15, 1, 50000, '2026-07')
                """);
        jdbcTemplate.update("""
                INSERT INTO payroll_detail_earning
                    (payroll_detail_id, earningCode, earningName, amount)
                VALUES (1, 'HAZ', 'Hazard Pay', 1000)
                """);
        jdbcTemplate.update("""
                INSERT INTO payroll_detail_deduction
                    (payroll_detail_id, deductionCode, deductionName, amount, employerShare,
                     reference, loanTotalAmount, loanPaymentsMade)
                VALUES (1, 'GSIS', 'GSIS Contribution', 500, 500, 'REF-1', NULL, NULL)
                """);
        jdbcTemplate.update("""
                INSERT INTO payroll_detail_deduction
                    (payroll_detail_id, deductionCode, deductionName, amount, employerShare,
                     reference, loanTotalAmount, loanPaymentsMade)
                VALUES (1, 'HDMF', 'Pagibig Contribution', 200, 200, 'REF-2', NULL, NULL)
                """);
        jdbcTemplate.update("""
                INSERT INTO payroll_detail_deduction
                    (payroll_detail_id, deductionCode, deductionName, amount, employerShare,
                     reference, loanTotalAmount, loanPaymentsMade)
                VALUES (1, 'PHIC', 'Philhealth Contribution', 300, 300, 'REF-3', NULL, NULL)
                """);
        jdbcTemplate.update("INSERT INTO employee (employeeId, employeeNo) VALUES (1, '001')");
        jdbcTemplate.update("""
                INSERT INTO personaldata
                    (employeeId, gsisId, pagibigId, philhealthNo, surname, firstname,
                     middlename, extname, dob)
                VALUES (1, 'GSIS-001', 'HDMF-001', 'PHIC-001', 'Santos', 'Maria', 'R', '', DATE '1990-01-01')
                """);
    }

    private void insertPendingAdjustments(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("""
                INSERT INTO payroll_adjustment_header
                    (id, employeeNo, employeeName, salaryPeriodKey, status, authorityNo)
                VALUES (100, '001', 'SANTOS, MARIA', '2026-07', 'PENDING', 'AUTH-1')
                """);
        jdbcTemplate.update("""
                INSERT INTO payroll_adjustment_line (header_id, type, code, name, amount)
                VALUES (100, 'EARNING', 'HAZ', 'Hazard Pay', 100)
                """);
        jdbcTemplate.update("""
                INSERT INTO payroll_adjustment_line (header_id, type, code, name, amount)
                VALUES (100, 'DEDUCTION', 'GSIS', 'GSIS Contribution', 50)
                """);
        jdbcTemplate.update("""
                INSERT INTO payroll_adjustment_line (header_id, type, code, name, amount)
                VALUES (100, 'DEDUCTION', 'HDMF', 'Pagibig Contribution', 20)
                """);
        jdbcTemplate.update("""
                INSERT INTO payroll_adjustment_line (header_id, type, code, name, amount)
                VALUES (100, 'DEDUCTION', 'PHIC', 'Philhealth Contribution', 30)
                """);
    }

    private record ReportMode(String reportPath, String mode) {
    }
}
