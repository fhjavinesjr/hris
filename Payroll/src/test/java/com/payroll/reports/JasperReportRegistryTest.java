package com.payroll.reports;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;

class JasperReportRegistryTest {
    @Test
    void allRuntimeTemplatesLoadAtStartupAndRemainCached() {
        JasperReportRegistry registry = new JasperReportRegistry();
        assertDoesNotThrow(registry::preloadRequiredTemplates);
        assertSame(
                JasperReportRegistry.get("reports/payslip.jrxml"),
                JasperReportRegistry.get("classpath:reports/payslip.jrxml")
        );
    }
}
