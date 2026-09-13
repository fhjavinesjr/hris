package com.primehr.reports;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;

class JasperReportRegistryTest {
    @Test
    void allRuntimeTemplatesLoadAtStartupAndRemainCached() {
        JasperReportRegistry registry = new JasperReportRegistry();
        assertDoesNotThrow(registry::preloadRequiredTemplates);
        assertSame(
                JasperReportRegistry.get("reports/vacancy_notice.jrxml"),
                JasperReportRegistry.get("classpath:reports/vacancy_notice.jrxml")
        );
    }
}
