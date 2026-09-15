package com.humanresource.reports;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LeaveCardPdfSmokeTest {
    @Test
    void rendersOnlyRemarksWithDeductionsAndLongParticularsWithoutBrokenRows() throws Exception {
        JasperReport report;
        try (InputStream input = new ClassPathResource("reports/leave_card.jrxml").getInputStream()) {
            report = JasperCompileManager.compileReport(input);
        }
        Map<String, Object> beginning = row(report, "2026-08-01");
        beginning.put("leaveParticulars", "Beginning Balance as of 2026-08-30");
        beginning.put("isBegBalance", "true");
        beginning.put("vacationLeaveBalance", 10.0);
        beginning.put("sickLeaveBalance", 12.0);
        Map<String, Object> september = row(report, "2026-09-01");
        september.put("leaveParticulars", "30 [A]");
        september.put("earnedVl", 1.25);
        september.put("earnedSl", 1.25);
        september.put("lateUndertimeEquivalent", 1.113);
        september.put("absentCount", 1.0);
        september.put("lateUndertimeMin", 534.0);
        september.put("lateCount", 1);
        september.put("utCount", 1);
        september.put("vacationLeaveBalance", 10.137);
        september.put("sickLeaveBalance", 13.25);
        Map<String, Object> october = row(report, "2026-10-01");
        StringBuilder longText = new StringBuilder();
        for (int day = 1; day <= 31; day++) {
            if (day > 1) longText.append(" | ");
            longText.append(String.format("%d [A]", day));
        }
        october.put("leaveParticulars", longText.toString());
        JasperPrint print = JasperFillManager.fillReport(report, new HashMap<>(),
                new JRMapCollectionDataSource(List.of(beginning, september, october)));
        assertFalse(print.getPages().isEmpty());
        List<JRPrintText> texts = print.getPages().stream()
                .flatMap(page -> page.getElements().stream())
                .filter(JRPrintText.class::isInstance).map(JRPrintText.class::cast).toList();
        assertTrue(texts.stream().anyMatch(text -> "October".equals(text.getFullText())));
        assertTrue(texts.stream().anyMatch(text -> "1.113".equals(text.getFullText())));
        assertTrue(texts.stream().anyMatch(text -> "30 [A]".equals(text.getFullText())));
        assertFalse(texts.stream().anyMatch(text -> text.getFullText().contains("DTR:")));
        for (JRPrintPage page : print.getPages()) {
            for (JRPrintElement element : page.getElements()) {
                if (element instanceof JRPrintText text && text.getFullText().equals(longText.toString())) {
                    JRPrintElement period = page.getElements().stream()
                            .filter(other -> other.getX() == 20 && other.getY() == element.getY())
                            .findFirst().orElseThrow();
                    assertEquals(element.getHeight(), period.getHeight(), "Row borders must stretch with particulars");
                }
            }
        }
        Path directory = Path.of("target", "leave-card-preview");
        Files.createDirectories(directory);
        JasperExportManager.exportReportToPdfFile(print, directory.resolve("leave-card.pdf").toString());
        ImageIO.write((BufferedImage) JasperPrintManager.printPageToImage(print, 0, 1.5f),
                "png", directory.resolve("leave-card.png").toFile());
        JasperPrint empty = JasperFillManager.fillReport(report, new HashMap<>(),
                new JRMapCollectionDataSource(List.of()));
        assertFalse(empty.getPages().isEmpty());
        JasperExportManager.exportReportToPdfFile(empty, directory.resolve("leave-card-empty.pdf").toString());
    }

    private Map<String, Object> row(JasperReport report, String start) {
        Map<String, Object> row = new HashMap<>();
        for (JRField field : report.getFields()) {
            Class<?> type = field.getValueClass();
            row.put(field.getName(), type == Double.class ? 0.0 : type == Integer.class ? 0
                    : type == Boolean.class ? false : type == String.class ? "" : null);
        }
        row.put("cutOffStartDate", Date.valueOf(start));
        row.put("firstname", "Test");
        row.put("lastname", "Employee");
        row.put("position", "Administrative Aide III");
        row.put("currentCompanySetting", "ISOFT Test Agency");
        row.put("forwardstatus", "Previous Year");
        return row;
    }
}
