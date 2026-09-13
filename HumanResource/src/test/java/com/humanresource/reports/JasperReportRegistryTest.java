package com.humanresource.reports;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;

class JasperReportRegistryTest {
    @Test
    void allRuntimeTemplatesLoadAtStartupAndRemainCached() {
        JasperReportRegistry registry = new JasperReportRegistry();
        assertDoesNotThrow(registry::preloadRequiredTemplates);
        assertSame(
                JasperReportRegistry.get("reports/leave_form_2020.jrxml"),
                JasperReportRegistry.get("classpath:reports/leave_form_2020.jrxml")
        );
    }
}
