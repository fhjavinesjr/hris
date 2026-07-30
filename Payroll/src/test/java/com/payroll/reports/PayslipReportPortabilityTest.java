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

class PayslipReportPortabilityTest {

    @Test
    void templateCompilesAndSelectsOnlyTheValidatedPayrollDetail() throws Exception {
        ClassPathResource resource = new ClassPathResource("reports/payslip.jrxml");

        JasperReport report;
        try (InputStream input = resource.getInputStream()) {
            report = JasperCompileManager.compileReport(input);
        }

        assertNotNull(report.getQuery());
        String query = report.getQuery().getText();
        String normalizedQuery = query.toLowerCase(Locale.ROOT);

        assertTrue(normalizedQuery.contains("where pd.id = $p{payrolldetailid}"));
        assertFalse(normalizedQuery.matches("(?s).*\\btop\\b.*"));
        assertFalse(normalizedQuery.matches("(?s).*\\bislocked\\s*=\\s*[01]\\b.*"));
        assertFalse(normalizedQuery.contains("$p{releasedonly}"));
        assertFalse(normalizedQuery.contains("$p{employeeno}"));
        assertFalse(normalizedQuery.contains("$p{salaryperiodkey}"));
    }
}
