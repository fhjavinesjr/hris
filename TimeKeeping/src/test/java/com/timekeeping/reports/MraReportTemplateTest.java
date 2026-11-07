package com.timekeeping.reports;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JRPrintText;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MraReportTemplateTest {
    @Test
    void jasperQueriesDailyDatasetAndProducesRepresentativePdf() throws Exception {
        DriverManagerDataSource source = new DriverManagerDataSource(
                "jdbc:h2:mem:mra_template;DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE mra_print_day (run_id VARCHAR(36), employee_id BIGINT, employee_no VARCHAR(100), "
                + "employee_name VARCHAR(255), business_unit_id BIGINT, business_unit VARCHAR(255), work_date DATE, "
                + "attendance_status VARCHAR(100), leave_type VARCHAR(100), work_minutes INT, "
                + "late_minutes INT, undertime_minutes INT, overtime_minutes INT, absent_minutes INT, "
                + "overtime_approved INT, remarks VARCHAR(1000))");
        jdbc.update("INSERT INTO mra_print_day VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "sample", 1L, "0001", "Sample Employee", 20L, "Ward A", Date.valueOf("2026-10-01"),
                "PRESENT", "", 480, 12, 4, 60, 0, 1, "Present; approved overtime");
        for (int day = 2; day <= 31; day++) {
            jdbc.update("INSERT INTO mra_print_day VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    "sample", 1L, "0001", "Sample Employee", 20L, "Ward A",
                    Date.valueOf(LocalDate.of(2026, 10, day)),
                    day == 2 ? "NO SCHEDULE / NO DTR - REVIEW"
                            : day == 3 ? "REST DAY WORKED - REVIEW"
                            : day == 4 ? "ABSENT / FOR REVIEW" : "FOR REVIEW", "",
                    0, 0, 0, 0, day == 4 ? 480 : 0, 0, "No schedule or DTR was recorded");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("runId", "sample");
        params.put("monthLabel", "OCTOBER 2026");
        params.put("agency", "Sample Agency");
        params.put("area", "Nursing");
        params.put("businessUnit", "Ward A");
        params.put("reviewNotice", "REVIEW REQUIRED: 30 daily entries need checking before certification.");
        params.put("preparedBy", "Prepared Officer");
        params.put("preparedPosition", "Administrative Officer");
        params.put("certifiedBy", "Certifying Officer");
        params.put("certifiedPosition", "Division Head");
        params.put("verifiedBy", "");
        params.put("verifiedPosition", "");
        JasperPrint print;
        try (var connection = source.getConnection()) {
            print = JasperFillManager.fillReport(
                    JasperReportRegistry.get("reports/monthly_report_attendance.jrxml"),
                    params, connection);
        }
        byte[] pdf = JasperExportManager.exportReportToPdf(print);
        assertThat(print.getPages()).hasSize(1);
        String printedText = print.getPages().get(0).getElements().stream()
                .filter(JRPrintText.class::isInstance)
                .map(JRPrintText.class::cast)
                .map(JRPrintText::getFullText)
                .reduce("", (left, right) -> left + "\n" + right);
        assertThat(printedText).contains("REVIEW REQUIRED: 30")
                .contains("REVIEW REQUIRED: 30 daily entries")
                .contains("Absence for review: 1");
        assertThat(pdf).startsWith("%PDF".getBytes());
        Files.write(Path.of("target", "mra-sample.pdf"), pdf);
    }
}
