package com.humanresource.impl;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermitSlipReportTemplateTest {

    private static final Pattern QUERY_PATTERN = Pattern.compile(
            "<query\\b[^>]*><!\\[CDATA\\[(.*?)]]></query>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    @Test
    void permitSlipCompilesWithProviderNeutralSql() throws Exception {
        ClassPathResource resource =
                new ClassPathResource("reports/permitSlip.jrxml");

        String source;
        try (InputStream input = resource.getInputStream()) {
            source = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        Matcher queryMatcher = QUERY_PATTERN.matcher(source);
        assertTrue(queryMatcher.find(), "Permit Slip must contain its report query");

        String sql = queryMatcher.group(1).toLowerCase(Locale.ROOT);
        assertFalse(sql.matches("(?s).*\\bisnull\\s*\\(.*"));
        assertFalse(sql.matches("(?s).*\\btop\\s+\\d+.*"));
        assertFalse(sql.matches("(?s).*\\bouter\\s+apply\\b.*"));
        assertFalse(sql.matches("(?s).*\\bas\\s+bit\\b.*"));
        assertFalse(sql.contains("+"), "Use CONCAT instead of vendor-specific '+' text concatenation");

        assertTrue(sql.contains("row_number() over"));
        assertTrue(sql.contains("left join ranked_settings"));
        assertTrue(sql.contains("coalesce("));
        assertTrue(sql.contains("concat("));

        try (InputStream input = resource.getInputStream()) {
            JasperReport report = JasperCompileManager.compileReport(input);
            assertNotNull(report.getQuery());
        }
    }
}
