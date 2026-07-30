package com.payroll.impl;

import com.payroll.dtos.GeneralPayrollReportRow;
import com.payroll.services.PayrollPeriodLockService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeneralPayrollReportDataLoaderTest {

    @ParameterizedTest
    @CsvSource({
            "PostgreSQL,false,115.0,23.0,92.0,165.0,28.0,137.0",
            "PostgreSQL,true,110.0,22.0,88.0,160.0,27.0,133.0",
            "MSSQLServer,false,115.0,23.0,92.0,165.0,28.0,137.0",
            "MSSQLServer,true,110.0,22.0,88.0,160.0,27.0,133.0"
    })
    void preservesDynamicLinesAndLockSemanticsInBothCompatibilityModes(
            String mode,
            boolean locked,
            double expectedEmployeeGross,
            double expectedEmployeeDeduction,
            double expectedEmployeeNet,
            double expectedGrandGross,
            double expectedGrandDeduction,
            double expectedGrandNet) throws Exception {
        DriverManagerDataSource dataSource = database(mode);
        createSchemaAndData(dataSource);

        PayrollPeriodLockService lockService =
                mock(PayrollPeriodLockService.class);
        when(lockService.isPeriodLocked("2026-07")).thenReturn(locked);
        GeneralPayrollReportDataLoader loader =
                new GeneralPayrollReportDataLoader(
                        new NamedParameterJdbcTemplate(dataSource),
                        lockService
                );

        List<GeneralPayrollReportRow> rows =
                loader.load("2026-07", "REGULAR");

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).getEmployeeNo()).isEqualTo("002");
        assertThat(rows.get(0).getRowNo()).isEqualTo(1);

        GeneralPayrollReportRow employee = rows.get(1);
        assertThat(employee.getEmployeeNo()).isEqualTo("001");
        assertThat(employee.getRowNo()).isEqualTo(2);
        assertThat(employee.getGrossAmount()).isEqualTo(expectedEmployeeGross);
        assertThat(employee.getTotalDeduction())
                .isEqualTo(expectedEmployeeDeduction);
        assertThat(employee.getNetAmount()).isEqualTo(expectedEmployeeNet);
        assertThat(employee.getGrandActualBasic()).isEqualTo(3000.0);
        assertThat(employee.getGrandGrossAmount()).isEqualTo(expectedGrandGross);
        assertThat(employee.getGrandTotalDeduction())
                .isEqualTo(expectedGrandDeduction);
        assertThat(employee.getGrandNetAmount()).isEqualTo(expectedGrandNet);
        assertThat(employee.getEarningBreakdown())
                .contains("Basic Salary [BASIC]: 100.00")
                .contains("Posted Earning [ADJ-POSTED / AUTH-1] [POST]: 10.00");
        assertThat(employee.getDeductionBreakdown())
                .contains("GSIS [GSIS]: 20.00")
                .contains("Posted Deduction [ADJ-POSTED / AUTH-1] [POSTD]: 2.00");

        if (locked) {
            assertThat(employee.getEarningBreakdown())
                    .doesNotContain("Pending Earning");
            assertThat(employee.getReportMode()).startsWith("FINAL / LOCKED");
        } else {
            assertThat(employee.getEarningBreakdown())
                    .contains("Pending Earning [ADJ-PENDING] [PEND]: 5.00");
            assertThat(employee.getReportMode())
                    .startsWith("DRAFT / PREVIEW");
        }
    }

    private DriverManagerDataSource database(String mode) {
        String name = "general_payroll_" + mode.toLowerCase() + "_"
                + UUID.randomUUID().toString().replace("-", "");
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(
                "jdbc:h2:mem:" + name + ";MODE=" + mode + ";DB_CLOSE_DELAY=-1"
        );
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    private void createSchemaAndData(DriverManagerDataSource dataSource)
            throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE payroll_detail (
                        id BIGINT PRIMARY KEY,
                        employeeNo VARCHAR(50),
                        employeeName VARCHAR(200),
                        department VARCHAR(200),
                        salaryGrade INTEGER,
                        salaryStep INTEGER,
                        salaryPeriodKey VARCHAR(30),
                        cutoffStartDate DATE,
                        cutoffEndDate DATE,
                        salaryDate DATE,
                        actualBasic DOUBLE,
                        displayToLastPage BOOLEAN,
                        payrollGroup VARCHAR(30),
                        status VARCHAR(30)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE payroll_detail_earning (
                        id BIGINT PRIMARY KEY,
                        payroll_detail_id BIGINT,
                        earningCode VARCHAR(50),
                        earningName VARCHAR(150),
                        indexNo INTEGER,
                        amount DOUBLE
                    )
                    """);
            statement.execute("""
                    CREATE TABLE payroll_detail_deduction (
                        id BIGINT PRIMARY KEY,
                        payroll_detail_id BIGINT,
                        deductionCode VARCHAR(50),
                        deductionName VARCHAR(150),
                        indexNo INTEGER,
                        amount DOUBLE
                    )
                    """);
            statement.execute("""
                    CREATE TABLE payroll_adjustment_header (
                        id BIGINT PRIMARY KEY,
                        employeeNo VARCHAR(50),
                        salaryPeriodKey VARCHAR(30),
                        status VARCHAR(30),
                        authorityNo VARCHAR(200)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE payroll_adjustment_line (
                        id BIGINT PRIMARY KEY,
                        header_id BIGINT,
                        type VARCHAR(30),
                        code VARCHAR(50),
                        name VARCHAR(150),
                        indexNo INTEGER,
                        amount DOUBLE
                    )
                    """);

            statement.execute("""
                    INSERT INTO payroll_detail (
                        id, employeeNo, employeeName, department, salaryGrade,
                        salaryStep, salaryPeriodKey, cutoffStartDate,
                        cutoffEndDate, salaryDate, actualBasic,
                        displayToLastPage, payrollGroup, status
                    ) VALUES
                        (1, '001', 'Last Page Employee', 'HR', 12, 1,
                         '2026-07', DATE '2026-07-01', DATE '2026-07-15',
                         DATE '2026-07-23', 1000, TRUE, 'REGULAR', 'COMPUTED'),
                        (2, '002', 'Regular Employee', 'Accounting', 10, 2,
                         '2026-07', DATE '2026-07-01', DATE '2026-07-15',
                         DATE '2026-07-23', 2000, FALSE, 'REGULAR', 'COMPUTED')
                    """);
            statement.execute("""
                    INSERT INTO payroll_detail_earning (
                        id, payroll_detail_id, earningCode, earningName,
                        indexNo, amount
                    ) VALUES
                        (1, 1, 'BASIC', 'Basic Salary', 1, 100),
                        (2, 2, 'BASIC', 'Basic Salary', 1, 50)
                    """);
            statement.execute("""
                    INSERT INTO payroll_detail_deduction (
                        id, payroll_detail_id, deductionCode, deductionName,
                        indexNo, amount
                    ) VALUES
                        (1, 1, 'GSIS', 'GSIS', 1, 20),
                        (2, 2, 'GSIS', 'GSIS', 1, 5)
                    """);
            statement.execute("""
                    INSERT INTO payroll_adjustment_header (
                        id, employeeNo, salaryPeriodKey, status, authorityNo
                    ) VALUES
                        (1, '001', '2026-07', 'POSTED', 'AUTH-1'),
                        (2, '001', '2026-07', 'PENDING', '')
                    """);
            statement.execute("""
                    INSERT INTO payroll_adjustment_line (
                        id, header_id, type, code, name, indexNo, amount
                    ) VALUES
                        (1, 1, 'EARNING', 'POST', 'Posted Earning', 1, 10),
                        (2, 1, 'DEDUCTION', 'POSTD', 'Posted Deduction', 1, 2),
                        (3, 2, 'EARNING', 'PEND', 'Pending Earning', 1, 5),
                        (4, 2, 'DEDUCTION', 'PENDD', 'Pending Deduction', 1, 1)
                    """);
        }
    }
}
