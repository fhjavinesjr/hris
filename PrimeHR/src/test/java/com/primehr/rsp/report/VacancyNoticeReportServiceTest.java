package com.primehr.rsp.report;

import com.primehr.rsp.api.RspPublicationDtos.ChannelResponse;
import com.primehr.rsp.api.RspPublicationDtos.PublicationResponse;
import com.primehr.rsp.api.RspPublicationDtos.RequirementSnapshotResponse;
import com.primehr.rsp.domain.VacancyPublicationStatus;
import com.primehr.rsp.domain.VacancyVisibility;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VacancyNoticeReportServiceTest {
    private final VacancyNoticeReportService service = new VacancyNoticeReportServiceImpl();

    @Test
    void approvedSnapshotProducesPortablePdf() {
        byte[] pdf = service.generate("ISOFT TEST AGENCY", publication(VacancyPublicationStatus.APPROVED));
        assertThat(pdf).startsWith("%PDF".getBytes()).hasSizeGreaterThan(2_000);
    }

    @Test
    void nonOfficialLifecycleStatesCannotProduceOfficialNotice() {
        assertThatThrownBy(() -> service.generate("AGENCY", publication(VacancyPublicationStatus.SUBMITTED)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("APPROVED or PUBLISHED");
    }

    @Test
    void longBeanCollectionPaginatesAndRepeatsCompetencyHeader() throws Exception {
        List<VacancyNoticeReportRow> rows = new ArrayList<>();
        for (int index = 1; index <= 70; index++) {
            rows.add(new VacancyNoticeReportRow(index, "COMP-" + index + " - Long competency name",
                    "L3 - ADVANCED", "MANDATORY", "HIGH", "Long snapshot remarks"));
        }
        Map<String, Object> parameters = new HashMap<>();
        for (String name : List.of("agency", "noticeId", "status", "position", "plantilla", "salary",
                "assignment", "period", "visibility", "education", "training", "experience",
                "eligibility", "license", "sourceBasis", "instructions", "contact", "noticeText",
                "channels", "sourceEvidence", "approval", "publication")) {
            parameters.put(name, name + " value");
        }
        JasperPrint print;
        try (InputStream input = new ClassPathResource("reports/vacancy_notice.jrxml").getInputStream()) {
            print = JasperFillManager.fillReport(JasperCompileManager.compileReport(input), parameters,
                    new JRBeanCollectionDataSource(rows));
        }
        assertThat(print.getPages()).hasSizeGreaterThan(1);
        for (JRPrintPage page : print.getPages()) {
            assertThat(page.getElements().stream().filter(JRPrintText.class::isInstance)
                    .map(JRPrintText.class::cast).map(JRPrintText::getFullText))
                    .contains("Competency", "Required Level", "Classification");
        }
    }

    private static PublicationResponse publication(VacancyPublicationStatus status) {
        Instant now = Instant.parse("2026-08-29T00:00:00Z");
        ChannelResponse channel = new ChannelResponse("channel-1", "Agency Website",
                LocalDate.of(2026, 9, 1), "REF-001", true, 0);
        RequirementSnapshotResponse requirement = new RequirementSnapshotResponse("requirement-1",
                "competency-1", "COMP-001", "Communication", 1, "level-1", "L1", "Basic",
                "MANDATORY", "HIGH", "Required for the position", 1);
        return new PublicationResponse("publication-1", "vacancy-1", "plan-1", status,
                VacancyVisibility.BOTH, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 15),
                "Submit the listed documents.", "Finance Office", "Contact HRMO.",
                "Applications from qualified candidates are invited.", 3L, "Accountant III Item",
                4L, "Accountant III", 19L, 1L, 2L, "FIN", "Finance Office", 7L, 2,
                "Bachelor's degree", "Eight hours relevant training", "Two years relevant experience",
                "Career Service Professional", null, "CSC Qualification Standards", "profile-1", 1, 7,
                "admin-fingerprint", "hrm-fingerprint", now, "001", now, "2026002", now,
                status == VacancyPublicationStatus.PUBLISHED ? "2026002" : null,
                status == VacancyPublicationStatus.PUBLISHED ? now : null, null, null, null, null, 3,
                List.of(channel), List.of(requirement));
    }
}
