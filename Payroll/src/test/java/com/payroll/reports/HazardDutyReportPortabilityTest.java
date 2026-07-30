package com.payroll.reports;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HazardDutyReportPortabilityTest {

    @Test
    void templateCompilesWithProviderNeutralSql() throws Exception {
        ClassPathResource resource = new ClassPathResource("reports/hazarddutyreport.jrxml");

        JasperReport report;
        try (InputStream input = resource.getInputStream()) {
            report = JasperCompileManager.compileReport(input);
        }

        assertNotNull(report.getQuery());
        String query = report.getQuery().getText().toLowerCase(Locale.ROOT);

        assertTrue(query.contains("coalesce("));
        assertFalse(query.matches("(?s).*\\bisnull\\s*\\(.*"));
        assertFalse(query.matches("(?s).*\\btop\\s+\\d+.*"));
        assertFalse(query.matches("(?s).*\\bouter\\s+apply\\b.*"));
        assertFalse(query.matches("(?s).*\\btry_cast\\s*\\(.*"));
    }
}
