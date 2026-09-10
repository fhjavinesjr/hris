package com.primehr.rsp.report;

import org.junit.jupiter.api.Test;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RspFormalReportRendererTest {
    private final RspFormalReportRenderer renderer = new RspFormalReportRenderer();

    @Test
    void allThreeBeanBackedTemplatesProducePdf() {
        List<RspFormalReportData.ComparativeRow> comparativeRows = List.of(
                new RspFormalReportData.ComparativeRow(1, "Applicant One", "application-1",
                        new BigDecimal("91.2500"), 1, 1,
                        "EXAM: score 45, contribution 40 | INTERVIEW: score 51.25, contribution 51.25",
                        "ELIGIBLE", "ENDORSED_FOR_SELECTION_DECISION - Recommended"));
        byte[] comparative = renderer.comparative(new RspFormalReportData.Comparative(
                "ISOFT TEST AGENCY", "proceeding-1", "publication-1", "Accountant III / Item 1",
                "Finance Office", "MSP-1 v1; weighted sum; ties COMPETITION",
                "HRMPSB v1; CSC basis", "Meeting v1; quorum 3", "a".repeat(64),
                "2026-09-07 10:00 PST", "tester at 2026-09-07 10:01 PST", comparativeRows));

        byte[] selection = renderer.selection(new RspFormalReportData.Selection(
                "ISOFT TEST AGENCY", "selection-1", "proceeding-1", "publication-1",
                "Accountant III / Item 1", "v1", "SELECTED", "Appointing authority; approved",
                "ACCEPTED", "ACKNOWLEDGED; receipt receipt-1", "b".repeat(64),
                "FINALIZED by authority", "tester at 2026-09-07 10:01 PST",
                List.of(new RspFormalReportData.SelectionRow(1, "Applicant One", "application-1",
                        new BigDecimal("91.2500"), 1, "ENDORSED_FOR_SELECTION_DECISION", "SELECTED"))));

        byte[] evidence = renderer.evidenceIndex(new RspFormalReportData.EvidenceIndex(
                "ISOFT TEST AGENCY", "selection-1", "proceeding-1", "publication-1",
                "Accountant III / Item 1", "c".repeat(64), "FINALIZED",
                "tester at 2026-09-07 10:01 PST",
                List.of(new RspFormalReportData.EvidenceRow(1, "APPLICATION_DOCUMENT", "document-1",
                        "snapshot", "CONFIDENTIAL", "APPLICANT_DOCUMENT", "d".repeat(64),
                        "PrimeHR", "SNAPSHOTTED", "2026-09-07 09:00 PST"))));

        assertThat(comparative).startsWith("%PDF".getBytes()).hasSizeGreaterThan(2_000);
        assertThat(selection).startsWith("%PDF".getBytes()).hasSizeGreaterThan(2_000);
        assertThat(evidence).startsWith("%PDF".getBytes()).hasSizeGreaterThan(2_000);
    }

    @Test
    void longEvidenceIndexPaginatesAndRepeatsMetadataHeaders() throws Exception {
        List<RspFormalReportData.EvidenceRow> rows = new ArrayList<>();
        for (int index = 1; index <= 80; index++) {
            rows.add(new RspFormalReportData.EvidenceRow(index, "EVALUATION_EVIDENCE", "record-" + index,
                    "record v1", "HRMPSB_RESTRICTED", "RSP_RECORD", "e".repeat(64),
                    "PrimeHR", "INDEXED", "2026-09-07 09:00 PST"));
        }
        Map<String,Object> parameters = new HashMap<>();
        parameters.put("agency","ISOFT TEST AGENCY");parameters.put("classification","CONFIDENTIAL METADATA INDEX - NO EVIDENCE CONTENT");parameters.put("templateVersion","PHASE-5F.1-v1");parameters.put("generated","tester");parameters.put("selectionId","selection-1");parameters.put("proceedingId","proceeding-1");parameters.put("publicationId","publication-1");parameters.put("vacancy","Vacancy");parameters.put("sourceFingerprint","f".repeat(64));parameters.put("terminalStatus","FINALIZED");
        JasperPrint print;
        try(InputStream input=new ClassPathResource("reports/rsp_evidence_index.jrxml").getInputStream()){
            print=JasperFillManager.fillReport(JasperCompileManager.compileReport(input),parameters,
                    new JRBeanCollectionDataSource(rows));
        }
        assertThat(print.getPages()).hasSizeGreaterThan(1);
        for(JRPrintPage page:print.getPages()){
            assertThat(page.getElements().stream().filter(JRPrintText.class::isInstance)
                    .map(JRPrintText.class::cast).map(JRPrintText::getFullText))
                    .contains("Record type / revision","Checksum / fingerprint","Event time");
        }
    }

    @Test
    void edgeCaseReportsHandleNoSelectionTiesCorrectedHistoryUnicodeAndNoLogo() {
        byte[] comparative = renderer.comparative(new RspFormalReportData.Comparative(
                "ISOFT Zamboanga - José Rizal", "proceeding-unicode", "publication-1",
                "Medical Officer III / Plantilla 123", "Clínica Niño",
                "MSP-1 v2; weighted sum; ties COMPETITION", "HRMPSB v3; CSC basis",
                "Meeting v2; quorum 3", "a".repeat(64), "2026-09-07 10:00 PST",
                "reviewer at 2026-09-07 10:01 PST",
                List.of(
                        new RspFormalReportData.ComparativeRow(1, "Ana dela Cruz", "application-1",
                                new BigDecimal("88.5000"), 1, 7, "EXAM: score 88.5", "ELIGIBLE",
                                "ENDORSED_FOR_SELECTION_DECISION - tied"),
                        new RspFormalReportData.ComparativeRow(2, "Bea Santos", "application-2",
                                new BigDecimal("88.5000"), 1, 7, "EXAM: score 88.5", "ELIGIBLE",
                                "ENDORSED_FOR_SELECTION_DECISION - tied"))));

        byte[] selection = renderer.selection(new RspFormalReportData.Selection(
                "ISOFT Zamboanga - José Rizal", "selection-corrected", "proceeding-unicode",
                "publication-1", "Medical Officer III / Plantilla 123", "v2; supersedes selection-v1",
                "NO_SELECTION", "appointing-authority; no candidate selected", "NOT APPLICABLE",
                "NOT CREATED", "b".repeat(64), "SUPERSEDED by authority at 2026-09-07 10:00 PST",
                "reviewer at 2026-09-07 10:01 PST",
                List.of(new RspFormalReportData.SelectionRow(1, "Ana dela Cruz", "application-1",
                        new BigDecimal("88.5000"), 1, "ENDORSED_FOR_SELECTION_DECISION", "NO SELECTION"))));

        assertThat(comparative).startsWith("%PDF".getBytes()).hasSizeGreaterThan(2_000);
        assertThat(selection).startsWith("%PDF".getBytes()).hasSizeGreaterThan(2_000);
    }
}
