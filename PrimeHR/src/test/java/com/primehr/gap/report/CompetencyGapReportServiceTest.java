package com.primehr.gap.report;

import com.primehr.gap.api.CompetencyGapDtos.AnalysisResponse;
import com.primehr.gap.api.CompetencyGapDtos.GapItemResponse;
import com.primehr.gap.domain.GapClassification;
import com.primehr.gap.domain.NotAssessedReason;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class CompetencyGapReportServiceTest {
    @Test
    void producesPortablePdfForCalculatedAndNotAssessedRows() {
        GapItemResponse below = item("item-1", "COMP-1", "Communication", "BASIC", "NOVICE",
                2, 1, 1, GapClassification.BELOW, null, "HIGH", "Urgent development priority");
        GapItemResponse missing = item("item-2", "COMP-2", "Technical Writing", "ADV", null,
                3, null, null, GapClassification.NOT_ASSESSED, NotAssessedReason.NO_RESULT,
                "MEDIUM", "Fallback for missing evidence");
        AnalysisResponse analysis = new AnalysisResponse("analysis-1", 1L, "001", "Ferdinand Javines",
                10L, 20L, null, "fingerprint", "Accountant III", null, 19L, 1L,
                "position-profile", 2, 7, "person-profile", 3, LocalDate.of(2026, 8, 1), null,
                "scheme-1", "GENERIC", 1, LocalDate.of(2026, 8, 29), "request-1", "admin",
                Instant.parse("2026-08-29T00:00:00Z"), List.of(below, missing));

        byte[] pdf = new CompetencyGapReportServiceImpl().generate(analysis);

        assertThat(pdf).startsWith("%PDF".getBytes()).hasSizeGreaterThan(2_000);
    }

    @Test
    void longBeanCollectionPaginatesAndRepeatsColumnHeader() throws Exception {
        List<CompetencyGapReportRow> rows = new ArrayList<>();
        for (int index = 1; index <= 70; index++) {
            rows.add(new CompetencyGapReportRow(index, "COMP-" + index + " - Long competency label",
                    "L3 - Advanced (order 3)", "Not assessed", "Not calculated", "NOT_ASSESSED",
                    "MANDATORY / HIGH", "HIGH - High Priority (rank 1)",
                    "Reason: NO_RESULT. Long report-ready explanation that must remain visible."));
        }
        Map<String, Object> parameters = new HashMap<>();
        for (String name : List.of("analysisId", "analysisDate", "employee", "position", "plantilla",
                "salary", "positionProfile", "personProfile", "priorityScheme", "generated")) {
            parameters.put(name, name + " value");
        }
        JasperPrint print;
        try (InputStream input = new ClassPathResource("reports/competency_gap_report.jrxml").getInputStream()) {
            print = JasperFillManager.fillReport(JasperCompileManager.compileReport(input), parameters,
                    new JRBeanCollectionDataSource(rows));
        }
        assertThat(print.getPages()).hasSizeGreaterThan(1);
        for (JRPrintPage page : print.getPages()) {
            assertThat(page.getElements().stream().filter(JRPrintText.class::isInstance)
                    .map(JRPrintText.class::cast).map(JRPrintText::getFullText))
                    .contains("Competency", "Required", "Attained");
        }
    }

    private static GapItemResponse item(String id, String code, String name, String requiredCode,
                                        String attainedCode, int requiredOrder, Integer attainedOrder,
                                        Integer gap, GapClassification classification,
                                        NotAssessedReason reason, String priorityCode, String explanation) {
        return new GapItemResponse(id, "requirement-" + id, "competency-" + id, code, name, 1,
                "scale-1", 1, "required-" + id, requiredCode, requiredCode, requiredOrder,
                attainedCode == null ? null : "attained-" + id, attainedCode, attainedCode, attainedOrder,
                gap, classification, reason, "MANDATORY", "HIGH",
                priorityCode == null ? null : "priority-1", priorityCode, priorityCode, 1,
                priorityCode == null ? null : "rule-1", explanation, 1);
    }
}
