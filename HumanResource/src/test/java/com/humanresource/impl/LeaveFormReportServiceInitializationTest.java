package com.humanresource.impl;

import com.humanresource.reports.JasperReportRegistry;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class LeaveFormReportServiceInitializationTest {

    @Test
    void requiredLeaveReportsAreCompiledAndCachedAtStartup() {
        JasperReport leaveForm = JasperReportRegistry.get("reports/leave_form_2020.jrxml");
        JasperReport leaveCard = JasperReportRegistry.get("reports/leave_card.jrxml");

        assertNotNull(leaveForm);
        assertNotNull(leaveCard);
        org.junit.jupiter.api.Assertions.assertSame(
                leaveForm,
                JasperReportRegistry.get("reports/leave_form_2020.jrxml")
        );
    }
}
