package com.humanresource.reports;

import jakarta.annotation.PostConstruct;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("humanResourceJasperReportRegistry")
public class JasperReportRegistry {

    private static final List<String> REQUIRED_TEMPLATES = List.of(
            "reports/CertificateCOC.jrxml",
            "reports/csc_assumption_to_duty_2025.jrxml",
            "reports/csc_oath_of_office_2025.jrxml",
            "reports/leave_card.jrxml",
            "reports/leave_form_2020.jrxml",
            "reports/onboarding_completion_record.jrxml",
            "reports/OvertimeAuthorization.jrxml",
            "reports/pds_c1.jasper",
            "reports/pds_c1_children_sub.jasper",
            "reports/pds_c2.jasper",
            "reports/pds_c2_civilservice_sub.jasper",
            "reports/pds_c2_workexperience_sub.jasper",
            "reports/pds_c3.jasper",
            "reports/pds_c3_lnd_sub.jasper",
            "reports/pds_c3_otherinformation_sub.jasper",
            "reports/pds_c3_voluntarywork_sub.jasper",
            "reports/pds_c4.jasper",
            "reports/pds_c4_references_sub.jasper",
            "reports/permitSlip.jrxml",
            "reports/personnel_action.jrxml"
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

    public static byte[] bytes(String resourcePath) {
        String path = normalize(resourcePath);
        return RESOURCE_BYTES.computeIfAbsent(path, JasperReportRegistry::readResource).clone();
    }

    private static JasperReport loadReport(String path) {
        try (ByteArrayInputStream input = new ByteArrayInputStream(resourceBytes(path))) {
            return path.endsWith(".jasper")
                    ? (JasperReport) JRLoader.loadObject(input)
                    : JasperCompileManager.compileReport(input);
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid Human Resource report template: " + path, exception);
        }
    }

    private static byte[] resourceBytes(String path) {
        return RESOURCE_BYTES.computeIfAbsent(path, JasperReportRegistry::readResource);
    }

    private static byte[] readResource(String path) {
        try (InputStream input = new ClassPathResource(path).getInputStream()) {
            return input.readAllBytes();
        } catch (Exception exception) {
            throw new IllegalStateException("Missing Human Resource report resource: " + path, exception);
        }
    }

    private static String normalize(String resourcePath) {
        return resourcePath.startsWith("classpath:")
                ? resourcePath.substring("classpath:".length())
                : resourcePath;
    }
}
