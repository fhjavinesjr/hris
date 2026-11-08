package com.primehr.succession.report;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SuccessionReportRendererTest {
    @Test
    void compilesAndFillsBeanDrivenSuccessionRecord() {
        byte[] pdf = new SuccessionReportRenderer().render(
                "Position-based Succession Coverage Register",
                "Restricted operational record; bench entries are not ranked.",
                "EMP-00001",
                List.of(new SuccessionReportData.Row("Position coverage", "Chief Administrative Officer", "ACTIVE_BENCH"),
                        new SuccessionReportData.Row("Boundary", "RSP authority", "No application, selection, or appointment created")));
        assertThat(pdf).startsWith("%PDF".getBytes()).hasSizeGreaterThan(1_000);
    }
}
