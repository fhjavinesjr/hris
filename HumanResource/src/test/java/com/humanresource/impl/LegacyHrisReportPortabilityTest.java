package com.humanresource.impl;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyHrisReportPortabilityTest {

    private static final List<String> REPORTS = List.of(
            "reports/CertificateCOC.jrxml",
            "reports/OvertimeAuthorization.jrxml",
            "reports/leave_card.jrxml",
            "reports/leave_form_2020.jrxml"
    );

    private static final Pattern QUERY_PATTERN = Pattern.compile(
            "<query\\b[^>]*><!\\[CDATA\\[(.*?)]]></query>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final List<Pattern> FORBIDDEN_SQL = List.of(
            Pattern.compile("\\bisnull\\s*\\(", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\btop\\s+\\d+\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bouter\\s+apply\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bconvert\\s*\\(", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bdateadd\\s*\\(", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bdatediff\\s*\\(", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\byear\\s*\\(", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bas\\s+bit\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bas\\s+datetime\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\blimit\\s+\\d+\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bilike\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("::[a-z]", Pattern.CASE_INSENSITIVE),
            Pattern.compile("'\\s*\\+|\\+\\s*'")
    );

    @Test
    void legacyHrisReportsCompileWithProviderNeutralQueries() throws Exception {
        for (String reportPath : REPORTS) {
            ClassPathResource resource = new ClassPathResource(reportPath);
            String source;
            try (InputStream input = resource.getInputStream()) {
                source = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }

            Matcher queryMatcher = QUERY_PATTERN.matcher(source);
            assertTrue(queryMatcher.find(), reportPath + " must contain a report query");

            String sql = queryMatcher.group(1);
            for (Pattern forbidden : FORBIDDEN_SQL) {
                assertFalse(
                        forbidden.matcher(sql).find(),
                        () -> reportPath + " contains provider-specific SQL matching " + forbidden
                );
            }

            String normalizedSql = sql.toLowerCase(Locale.ROOT);
            assertTrue(normalizedSql.contains("row_number() over"), reportPath);
            assertTrue(normalizedSql.contains("coalesce("), reportPath);

            try (InputStream input = resource.getInputStream()) {
                JasperReport report = JasperCompileManager.compileReport(input);
                assertNotNull(report.getQuery(), reportPath);
            }
        }
    }

    @Test
    void reportsDeclareServiceComputedPresentationParameters() throws Exception {
        Set<String> certificateParameters = parameterNames("reports/CertificateCOC.jrxml");
        Set<String> leaveCardParameters = parameterNames("reports/leave_card.jrxml");
        Set<String> leaveFormParameters = parameterNames("reports/leave_form_2020.jrxml");

        assertTrue(certificateParameters.contains("VALID_UNTIL"));
        assertTrue(leaveCardParameters.contains("SEPARATION_TEXT"));
        assertTrue(leaveFormParameters.contains("WORKING_DAYS_APPLIED"));
        assertTrue(leaveFormParameters.contains("INCLUSIVE_DATES"));
    }

    private static Set<String> parameterNames(String reportPath) throws Exception {
        ClassPathResource resource = new ClassPathResource(reportPath);
        JasperReport report;
        try (InputStream input = resource.getInputStream()) {
            report = JasperCompileManager.compileReport(input);
        }

        return Arrays.stream(report.getParameters())
                .map(JRParameter::getName)
                .collect(Collectors.toSet());
    }
}
