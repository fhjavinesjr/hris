package com.humanresource.reports;

import com.humanresource.dtos.PersonnelActionReportData;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class PersonnelActionPortablePdfSmokeTest {

    @Test
    void compilesAndGeneratesPdfWithoutDatabaseSpecificSql() throws Exception {
        PersonnelActionReportData row = new PersonnelActionReportData(
                "ISOFT Test Agency", "Test Address", "Fernando, Michelle S.",
                "Renewal of Appointment", "07/01/2016", "Accountant II",
                "Administrative Officer II", "18,000.00", "27,000.00", "",
                "Patient Support", "", "Hospital Operations", "PERMANENT", null, null);

        ClassPathResource resource = new ClassPathResource("reports/personnel_action.jrxml");
        try (InputStream input = resource.getInputStream()) {
            JasperReport report = JasperCompileManager.compileReport(input);
            assertNull(report.getQuery(), "Bean-backed report must not contain SQL");

            JasperPrint print = JasperFillManager.fillReport(
                    report,
                    new HashMap<>(),
                    new JRBeanCollectionDataSource(List.of(row)));
            byte[] pdf = JasperExportManager.exportReportToPdf(print);
            Path target = Path.of("target");
            Files.createDirectories(target);
            Files.write(target.resolve("personnel-action-smoke.pdf"), pdf);
            Image image = JasperPrintManager.printPageToImage(print, 0, 1.5f);
            BufferedImage rendered = new BufferedImage(
                    image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = rendered.createGraphics();
            try {
                graphics.drawImage(image, 0, 0, null);
            } finally {
                graphics.dispose();
            }
            javax.imageio.ImageIO.write(
                    rendered, "png", target.resolve("personnel-action-smoke.png").toFile());

            assertFalse(print.getPages().isEmpty());
            assertArrayEquals("%PDF".getBytes(), java.util.Arrays.copyOf(pdf, 4));
        }
    }
}
