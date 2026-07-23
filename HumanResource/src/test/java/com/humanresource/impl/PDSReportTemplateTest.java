package com.humanresource.impl;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class PDSReportTemplateTest {

    private static final List<String> TEMPLATES = List.of(
            "pds_c1_children_sub",
            "pds_c2_civilservice_sub",
            "pds_c2_workexperience_sub",
            "pds_c3_voluntarywork_sub",
            "pds_c3_lnd_sub",
            "pds_c3_otherinformation_sub",
            "pds_c4_references_sub",
            "pds_c1",
            "pds_c2",
            "pds_c3",
            "pds_c4"
    );

    @Test
    void everyPdsSourceTemplateCompilesAndContainsNoDatabaseQuery() throws Exception {
        for (String template : TEMPLATES) {
            ClassPathResource resource =
                    new ClassPathResource("reports/" + template + ".jrxml");
            byte[] sourceBytes;
            try (InputStream input = resource.getInputStream()) {
                sourceBytes = input.readAllBytes();
            }

            String source = new String(sourceBytes, StandardCharsets.UTF_8)
                    .toLowerCase(Locale.ROOT);
            assertFalse(source.contains("<query"));
            assertFalse(source.contains("report_connection"));
            assertFalse(source.contains("select top"));
            assertFalse(source.contains("try_convert"));
            assertFalse(source.contains("getdate("));

            try (InputStream input = resource.getInputStream()) {
                JasperReport report = JasperCompileManager.compileReport(input);
                assertNull(report.getQuery());
            }
        }
    }

    @Test
    void everyPackagedPdsBinaryIsLoadableAndContainsNoDatabaseQuery() throws Exception {
        for (String template : TEMPLATES) {
            ClassPathResource resource =
                    new ClassPathResource("reports/" + template + ".jasper");
            try (InputStream input = resource.getInputStream()) {
                Object loaded = JRLoader.loadObject(input);
                JasperReport report = assertInstanceOf(JasperReport.class, loaded);
                assertNull(report.getQuery());
            }
        }
    }
}
