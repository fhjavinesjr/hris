package com.humanresource.reports;

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

class PermitSlipPortablePdfSmokeTest {

    @ParameterizedTest
    @ValueSource(strings = {"PostgreSQL", "MSSQLServer"})
    void fillsRepresentativePdfInBothCompatibilityModes(String mode) throws Exception {
        try (Connection connection = openDatabase(mode);
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE settings (
                        settingsId BIGINT PRIMARY KEY,
                        companyName VARCHAR(200),
                        address VARCHAR(300),
                        hospitalAgency BOOLEAN,
                        leftHeaderLogo VARBINARY(1000),
                        rightHeaderLogo VARBINARY(1000)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE employee (
                        employeeId BIGINT PRIMARY KEY,
                        firstname VARCHAR(100),
                        lastname VARCHAR(100),
                        suffix VARCHAR(30)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE pass_slip (
                        passSlipId BIGINT PRIMARY KEY,
                        employeeId BIGINT,
                        approvedById BIGINT,
                        passSlipDate DATE,
                        purpose VARCHAR(300),
                        details VARCHAR(300),
                        departureTime TIME,
                        arrivalTime TIME
                    )
                    """);

            statement.execute("""
                    INSERT INTO settings
                        (settingsId, companyName, address, hospitalAgency)
                    VALUES
                        (1, 'ISOFT Test Agency', 'Test Address', TRUE)
                    """);
            statement.execute("""
                    INSERT INTO employee (employeeId, firstname, lastname, suffix)
                    VALUES
                        (1, 'FERDINAND', 'JAVINES', NULL),
                        (2, 'APPROVER', 'OFFICER', NULL)
                    """);
            statement.execute("""
                    INSERT INTO pass_slip
                        (passSlipId, employeeId, approvedById, passSlipDate, purpose,
                         details, departureTime, arrivalTime)
                    VALUES
                        (100, 1, 2, DATE '2026-07-23', 'Official business',
                         'Portable report smoke test', TIME '09:00:00', TIME '11:00:00')
                    """);

            JasperPrint print = fill(
                    "reports/permitSlip.jrxml",
                    Map.of("passSlipId", 100L),
                    connection
            );
            byte[] pdf = JasperExportManager.exportReportToPdf(print);

            assertFalse(print.getPages().isEmpty());
            assertArrayEquals("%PDF".getBytes(), java.util.Arrays.copyOf(pdf, 4));
        }
    }

    private Connection openDatabase(String mode) throws Exception {
        String databaseName = "permit_" + mode.toLowerCase() + "_"
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
}
