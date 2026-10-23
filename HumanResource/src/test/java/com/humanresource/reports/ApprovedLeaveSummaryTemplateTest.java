package com.humanresource.reports;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;

class ApprovedLeaveSummaryTemplateTest {
    @Test
    void printsPopulatedAndEmptyLeaveSummaries() throws Exception {
        Map<String, Object> params = new java.util.HashMap<>(Map.of("agency", "Sample Agency", "area", "Hospital",
                "businessUnit", "Ward A", "rangeLabel", "2026-10-01 to 2026-10-31",
                "leaveTypeLabel", "All leave types", "payLabel", "With and without pay"));
        var row = new ApprovedLeaveSummaryService.Row("0001", "Sample Employee", "Ward A",
                "Vacation Leave", Date.valueOf("2026-10-01"), Date.valueOf("2026-10-02"), 2.0, "With pay");
        JasperPrint populated = JasperFillManager.fillReport(
                JasperReportRegistry.get("reports/approved_leave_summary.jrxml"), params,
                new JRBeanCollectionDataSource(List.of(row)));
        JasperPrint empty = JasperFillManager.fillReport(
                JasperReportRegistry.get("reports/approved_leave_summary.jrxml"), params,
                new JRBeanCollectionDataSource(List.of()));
        assertThat(populated.getPages()).hasSize(1);
        assertThat(empty.getPages()).hasSize(1);
        Files.write(Path.of("target", "approved-leave-summary-sample.pdf"), JasperExportManager.exportReportToPdf(populated));
        ImageIO.write((BufferedImage) JasperPrintManager.printPageToImage(populated, 0, 1.25f), "png",
                Path.of("target", "approved-leave-summary-sample.png").toFile());
        Files.write(Path.of("target", "approved-leave-summary-empty.pdf"), JasperExportManager.exportReportToPdf(empty));
    }
}
