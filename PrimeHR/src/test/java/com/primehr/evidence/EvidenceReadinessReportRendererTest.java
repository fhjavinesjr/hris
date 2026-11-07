package com.primehr.evidence;

import com.primehr.evidence.report.EvidenceReadinessReportData;
import com.primehr.evidence.report.EvidenceReadinessReportRenderer;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EvidenceReadinessReportRendererTest {
    @Test
    void compilesBeanTemplateAndGeneratesRepresentativePaginatedPdf() {
        List<EvidenceReadinessReportData.Row> rows = java.util.stream.IntStream.range(0, 80)
                .mapToObj(index -> new EvidenceReadinessReportData.Row("Requirement", "REQ-" + index,
                        "Exact provenance and transparent fact " + index, index % 2 == 0 ? "COVERED" : "NOT COVERED"))
                .toList();
        byte[] pdf = new EvidenceReadinessReportRenderer().render("Internal Evidence Readiness",
                "Internal self-assessment only; not an official CSC determination.", "VALIDATOR", rows);
        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
        assertThat(pdf.length).isGreaterThan(2000);
    }
}
