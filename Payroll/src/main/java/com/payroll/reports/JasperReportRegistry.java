package com.payroll.reports;

import jakarta.annotation.PostConstruct;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("payrollJasperReportRegistry")
public class JasperReportRegistry {
    private static final List<String> REQUIRED_TEMPLATES = List.of(
            "reports/general_payroll.jrxml",
            "reports/generic_payroll_deduction.jrxml",
            "reports/generic_payroll_earning.jrxml",
            "reports/gsis_remittance.jrxml",
            "reports/hazarddutyreport.jrxml",
            "reports/pagibigmembershipremittance.jrxml",
            "reports/payslip.jrxml",
            "reports/philhealth_remittance.jrxml"
    );
    private static final Map<String, byte[]> RESOURCE_BYTES = new ConcurrentHashMap<>();
    private static final Map<String, JasperReport> COMPILED_REPORTS = new ConcurrentHashMap<>();

    @PostConstruct
    void preloadRequiredTemplates() {
        System.setProperty("net.sf.jasperreports.compiler.xml.parser.validation", "false");
        REQUIRED_TEMPLATES.forEach(JasperReportRegistry::get);
    }

    public static JasperReport get(String resourcePath) {
        String path = normalize(resourcePath);
        return COMPILED_REPORTS.computeIfAbsent(path, JasperReportRegistry::loadReport);
    }

    private static JasperReport loadReport(String path) {
        try (ByteArrayInputStream input = new ByteArrayInputStream(
                RESOURCE_BYTES.computeIfAbsent(path, JasperReportRegistry::readResource))) {
            return JasperCompileManager.compileReport(input);
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid Payroll report template: " + path, exception);
        }
    }

    private static byte[] readResource(String path) {
        try (InputStream input = new ClassPathResource(path).getInputStream()) {
            return input.readAllBytes();
        } catch (Exception exception) {
            throw new IllegalStateException("Missing Payroll report resource: " + path, exception);
        }
    }

    private static String normalize(String path) {
        return path.startsWith("classpath:") ? path.substring("classpath:".length()) : path;
    }
}
