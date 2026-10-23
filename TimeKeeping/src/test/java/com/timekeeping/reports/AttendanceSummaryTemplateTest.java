package com.timekeeping.reports;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AttendanceSummaryTemplateTest {
    @Test
    void rendersPopulatedAndEmptySummaryPdfs() throws Exception {
        DriverManagerDataSource source = new DriverManagerDataSource(
                "jdbc:h2:mem:attendance_summary_template;DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE mra_print_day (run_id VARCHAR(36), employee_id BIGINT, "
                + "employee_no VARCHAR(100), employee_name VARCHAR(255), business_unit_id BIGINT, "
                + "business_unit VARCHAR(255), work_date DATE, attendance_status VARCHAR(100), "
                + "metric_minutes INT, overtime_approved INT, remarks VARCHAR(1000))");
        jdbc.update("INSERT INTO mra_print_day VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "sample", 1L, "0001", "Sample Employee", 20L, "Ward A", Date.valueOf("2026-10-01"),
                "PRESENT", 45, 0, "Present");
        Map<String, Object> params = new HashMap<>();
        params.put("runId", "sample");
        params.put("monthLabel", "OCTOBER 2026");
        params.put("agency", "Sample Agency");
        params.put("area", "Nursing");
        params.put("businessUnit", "Ward A");
        params.put("summaryTitle", "TARDINESS SUMMARY");
        params.put("summaryNote", "Derived from finalized DTR daily values.");
        params.put("noDataMessage", "No recorded tardiness minutes.");
        params.put("preparedBy", "Prepared Officer");
        params.put("preparedPosition", "Administrative Officer");
        params.put("certifiedBy", "Certifying Officer");
        params.put("certifiedPosition", "Division Head");
        params.put("verifiedBy", "");
        params.put("verifiedPosition", "");
        JasperPrint populated = fill(source, params);
        assertThat(populated.getPages()).hasSize(1);
        Files.write(Path.of("target", "attendance-summary-sample.pdf"),
                JasperExportManager.exportReportToPdf(populated));

        params.put("runId", "missing");
        JasperPrint empty = fill(source, params);
        assertThat(empty.getPages()).hasSize(1);
        Files.write(Path.of("target", "attendance-summary-empty.pdf"),
                JasperExportManager.exportReportToPdf(empty));
    }

    private JasperPrint fill(DriverManagerDataSource source, Map<String, Object> params) throws Exception {
        try (var connection = source.getConnection()) {
            return JasperFillManager.fillReport(
                    JasperReportRegistry.get("reports/attendance_summary.jrxml"), params, connection);
        }
    }
}
