package com.timekeeping.reports;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;

class IncompleteDtrReportTest {
    @Test
    void classifiesReviewDaysWithoutFlaggingOrdinaryAttendanceOrRest() {
        LocalDate first = LocalDate.of(2026, 10, 1);
        var days = List.of(
                day(first, "PRESENT", "Present", 480, 0),
                day(first.plusDays(1), "REST DAY", "REST DAY", 0, 0),
                day(first.plusDays(2), "ABSENT / FOR REVIEW", "ABSENT", 0, 480),
                day(first.plusDays(3), "FOR REVIEW", "NO SCHEDULE / NO DTR - REVIEW", 0, 0));
        var findings = IncompleteDtrReportService.classifyDays(days);
        assertThat(findings).doesNotContainKeys(first, first.plusDays(1));
        assertThat(findings.get(first.plusDays(2))).anyMatch(value -> value.startsWith("ABSENT"));
        assertThat(findings).doesNotContainKey(first.plusDays(3));
    }

    @Test
    void printsPopulatedAndEmptyPages() throws Exception {
        Map<String, Object> params = new java.util.HashMap<>(Map.of("agency", "Sample Agency", "area", "Hospital",
                "businessUnit", "Ward A", "rangeLabel", "2026-10-01 to 2026-10-31",
                "preparedBy", "Officer A", "preparedPosition", "Administrative Officer",
                "certifiedBy", "Officer B", "certifiedPosition", "Division Chief"));
        var row = new IncompleteDtrReportService.Finding("0001", "Sample Employee", "Ward A",
                Date.valueOf("2026-10-01"), "INCOMPLETE PUNCH - missing time in or out");
        JasperPrint populated = JasperFillManager.fillReport(
                JasperReportRegistry.get("reports/incomplete_dtr_review.jrxml"), params,
                new JRBeanCollectionDataSource(List.of(row)));
        JasperPrint empty = JasperFillManager.fillReport(
                JasperReportRegistry.get("reports/incomplete_dtr_review.jrxml"), params,
                new JRBeanCollectionDataSource(List.of()));
        assertThat(populated.getPages()).hasSize(1);
        assertThat(empty.getPages()).hasSize(1);
        Files.write(Path.of("target", "incomplete-dtr-sample.pdf"), JasperExportManager.exportReportToPdf(populated));
        ImageIO.write((BufferedImage) JasperPrintManager.printPageToImage(populated, 0, 1.25f), "png",
                Path.of("target", "incomplete-dtr-sample.png").toFile());
        Files.write(Path.of("target", "incomplete-dtr-empty.pdf"), JasperExportManager.exportReportToPdf(empty));
    }

    private static MraDailyProjection.Day day(LocalDate date, String status, String remarks,
                                               int workMinutes, int absentMinutes) {
        return new MraDailyProjection.Day(date, status, "", workMinutes, 0, 0, 0,
                absentMinutes, false, remarks);
    }
}
