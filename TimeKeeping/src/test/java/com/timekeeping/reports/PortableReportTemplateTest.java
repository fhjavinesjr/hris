package com.timekeeping.reports;

import com.timekeeping.dtos.DtrReportRow;
import com.timekeeping.dtos.WorkScheduleReportRow;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PortableReportTemplateTest {
    private static final List<String> VENDOR_SQL_TOKENS = List.of(
            " top ",
            "isnull(",
            "dateadd(",
            "datediff(",
            "datename(",
            " format(",
            "try_cast(",
            "outer apply",
            "sys.all_objects",
            "maxrecursion"
    );

    @Test
    void dtrTemplateCompilesAndFillsFromBeansWithoutVendorSql() throws Exception {
        String resourcePath = "reports/dtrNew.jrxml";
        assertLayoutOnlyAndPortable(resourcePath);
        JasperReport report = compile(resourcePath);

        Map<String, Object> params = commonParameters();
        params.put("EMPLOYEE_ID", "001");
        params.put("fromDate", Date.valueOf("2026-07-01"));
        params.put("toDate", Date.valueOf("2026-07-01"));
        params.put("lastDtrDate", Date.valueOf("2026-07-01"));

        DtrReportRow row = new DtrReportRow(
                Date.valueOf("2026-07-01"),
                "FERDINAND JAVINES",
                Time.valueOf("08:00:00"),
                Time.valueOf("12:00:00"),
                Time.valueOf("13:00:00"),
                Time.valueOf("17:00:00"),
                480,
                0,
                0.0d,
                0.0d,
                0.0d,
                0.0d,
                0.0d,
                0,
                0,
                0,
                "",
                "",
                null,
                null,
                "",
                0,
                "Present",
                "",
                null,
                false,
                "",
                "Wednesday"
        );

        JasperPrint print = JasperFillManager.fillReport(
                report,
                params,
                new JRBeanCollectionDataSource(List.of(row))
        );
        assertRepresentativePdf(print);
    }

    @Test
    void workScheduleTemplateCompilesAndFillsFromBeansWithoutVendorSql() throws Exception {
        String resourcePath = "reports/works_schedule.jrxml";
        assertLayoutOnlyAndPortable(resourcePath);
        JasperReport report = compile(resourcePath);

        Map<String, Object> params = commonParameters();
        params.put("approvedBy", "APPROVER");
        params.put("printedBy", "PREPARER");
        params.put("approvedByPos", "Chief");
        params.put("printedByPos", "Officer");
        params.put("formCode", "");
        params.put("nchLogoPath", "");
        params.put("dohLogoPath", "");
        params.put("supervisingNurse", "");
        params.put("areaId", 10L);
        params.put("businessUnitId", null);
        params.put("fromDate", Date.valueOf("2026-07-01"));
        params.put("toDate", Date.valueOf("2026-07-01"));
        params.put("reportArea", "Nursing");
        params.put("reportBusinessUnit", "All Business Units");
        params.put("dateRangeLabel", "2026-07-01 to 2026-07-01");

        WorkScheduleReportRow row = new WorkScheduleReportRow(
                "Nursing",
                "JAVINES, FERDINAND",
                Date.valueOf("2026-07-01"),
                "1Q - 08:00 AM / 12:00 PM - 01:00 PM / 05:00 PM",
                "",
                "Nurse II",
                11
        );

        JasperPrint print = JasperFillManager.fillReport(
                report,
                params,
                new JRBeanCollectionDataSource(List.of(row))
        );
        assertRepresentativePdf(print);
    }

    private static void assertLayoutOnlyAndPortable(String resourcePath) throws Exception {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        String template;
        try (InputStream input = resource.getInputStream()) {
            template = new String(input.readAllBytes(), StandardCharsets.UTF_8)
                    .toLowerCase(Locale.ROOT);
        }
        assertThat(template).doesNotContain("<query");
        assertThat(template).doesNotContain("defaultdataadapter");
        for (String token : VENDOR_SQL_TOKENS) {
            assertThat(template).doesNotContain(token);
        }
    }

    private static JasperReport compile(String resourcePath) throws Exception {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (InputStream input = resource.getInputStream()) {
            return JasperCompileManager.compileReport(input);
        }
    }

    private static void assertRepresentativePdf(JasperPrint print) throws Exception {
        assertThat(print.getPages()).isNotEmpty();
        byte[] pdf = JasperExportManager.exportReportToPdf(print);
        assertThat(pdf)
                .hasSizeGreaterThan(1_000)
                .startsWith("%PDF".getBytes(StandardCharsets.US_ASCII));
    }

    private static Map<String, Object> commonParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("currentCompany", "ISOFT HRIS");
        params.put("currentCompanyAddress", "Test Address");
        params.put("webAppPath", "");
        params.put("logoleft", null);
        params.put("logoright", null);
        params.put("isDOH", false);
        return params;
    }
}
