package com.humanresource.onboarding;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AppointmentDocumentReportRenderer {
    public byte[] oath(AppointmentReportData.LegalDocument data) {
        return render("reports/csc_oath_of_office_2025.jrxml", legal(data), new JREmptyDataSource(1));
    }

    public byte[] assumption(AppointmentReportData.LegalDocument data) {
        return render("reports/csc_assumption_to_duty_2025.jrxml", legal(data), new JREmptyDataSource(1));
    }

    public byte[] onboarding(AppointmentReportData.OnboardingCompletion data) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("agencyName", data.agencyName());
        values.put("agencyAddress", data.agencyAddress());
        values.put("intakeId", data.intakeId());
        values.put("handoffId", data.handoffId());
        values.put("selectionId", data.selectionId());
        values.put("applicationId", data.applicationId());
        values.put("employeeReference", data.employeeReference());
        values.put("appointmentReference", data.appointmentReference());
        values.put("templateReference", data.templateReference());
        values.put("completedAt", data.completedAt());
        values.put("sourceFingerprint", data.sourceFingerprint());
        values.put("generated", data.generated());
        return render("reports/onboarding_completion_record.jrxml", values,
                new JRBeanCollectionDataSource(data.rows()));
    }

    private Map<String, Object> legal(AppointmentReportData.LegalDocument data) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("agencyName", data.agencyName());
        values.put("agencyAddress", data.agencyAddress());
        values.put("appointeeName", data.appointeeName());
        values.put("appointeeAddress", data.appointeeAddress());
        values.put("position", data.position());
        values.put("office", data.office());
        values.put("governmentIdType", data.governmentIdType());
        values.put("governmentIdNumber", data.governmentIdNumber());
        values.put("governmentIdDate", data.governmentIdDate());
        values.put("issueDate", data.issueDate());
        values.put("oathDate", data.oathDate());
        values.put("assumptionDate", data.assumptionDate());
        values.put("venue", data.venue());
        values.put("administeringName", data.administeringName());
        values.put("administeringPosition", data.administeringPosition());
        values.put("certifyingName", data.certifyingName());
        values.put("certifyingPosition", data.certifyingPosition());
        values.put("attestingName", data.attestingName());
        values.put("attestingPosition", data.attestingPosition());
        values.put("templateCode", data.templateCode());
        values.put("templateVersion", data.templateVersion());
        values.put("sourceFingerprint", data.sourceFingerprint());
        values.put("generated", data.generated());
        return values;
    }

    private byte[] render(String path, Map<String, Object> parameters, JRDataSource source) {
        try (InputStream input = new ClassPathResource(path).getInputStream();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            JasperPrint print = JasperFillManager.fillReport(
                    JasperCompileManager.compileReport(input), parameters, source);
            JasperExportManager.exportReportToPdfStream(print, output);
            return output.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate appointment document PDF", exception);
        }
    }
}
