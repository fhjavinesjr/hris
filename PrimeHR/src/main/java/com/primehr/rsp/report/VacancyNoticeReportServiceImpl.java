package com.primehr.rsp.report;

import com.primehr.rsp.api.RspPublicationDtos.PublicationResponse;
import com.primehr.rsp.api.RspPublicationDtos.RequirementSnapshotResponse;
import com.primehr.rsp.domain.VacancyPublicationStatus;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class VacancyNoticeReportServiceImpl implements VacancyNoticeReportService {
    private static final String TEMPLATE = "reports/vacancy_notice.jrxml";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    @Override
    public byte[] generate(String agencyId, PublicationResponse publication) {
        Objects.requireNonNull(publication, "publication");
        if (publication.status() != VacancyPublicationStatus.APPROVED
                && publication.status() != VacancyPublicationStatus.PUBLISHED) {
            throw new IllegalArgumentException(
                    "An official vacancy notice is available only for APPROVED or PUBLISHED records");
        }
        try (InputStream input = new ClassPathResource(TEMPLATE).getInputStream()) {
            JasperReport report = JasperCompileManager.compileReport(input);
            JasperPrint print = JasperFillManager.fillReport(report, parameters(agencyId, publication),
                    new JRBeanCollectionDataSource(rows(publication)));
            return JasperExportManager.exportReportToPdf(print);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate the vacancy notice", exception);
        }
    }

    private static Map<String, Object> parameters(String agencyId, PublicationResponse item) {
        Map<String, Object> values = new HashMap<>();
        values.put("agency", value(agencyId));
        values.put("noticeId", value(item.id()));
        values.put("status", value(item.status()));
        values.put("position", value(item.jobPositionName()));
        values.put("plantilla", value(item.plantillaName()) + " (#" + value(item.plantillaId()) + ")");
        values.put("salary", "Salary Grade " + value(item.salaryGrade()) + " / Step " + value(item.salaryStep()));
        values.put("assignment", value(item.placeOfAssignment()));
        values.put("period", value(item.openingDate()) + " to " + value(item.closingDate()));
        values.put("visibility", value(item.visibility()));
        values.put("education", value(item.educationRequirement()));
        values.put("training", value(item.trainingRequirement()));
        values.put("experience", value(item.experienceRequirement()));
        values.put("eligibility", value(item.eligibilityRequirement()));
        values.put("license", emptyAsNone(item.licenseRequirement()));
        values.put("sourceBasis", emptyAsNone(item.qualificationSourceBasis()));
        values.put("instructions", value(item.instructions()));
        values.put("contact", value(item.contactGuidance()));
        values.put("noticeText", value(item.noticeText()));
        values.put("channels", item.channels().stream().filter(channel -> channel.active())
                .map(channel -> channel.channelName() + " (" + channel.publicationDate() + ")"
                        + (channel.reference() == null || channel.reference().isBlank()
                        ? "" : " - " + channel.reference())).reduce((left, right) -> left + "; " + right)
                .orElse("No channel recorded"));
        values.put("sourceEvidence", "QS v" + item.qualificationStandardVersion()
                + "; Position Profile v" + item.positionProfileDefinitionVersion()
                + "/rev " + item.positionProfileRecordRevision()
                + "; snapshot " + value(item.sourceSnapshotAt()));
        values.put("approval", item.approvedBy() == null ? "Not recorded"
                : item.approvedBy() + " on " + DATE_TIME.format(item.approvedAt()));
        values.put("publication", item.publishedBy() == null ? "Not yet published"
                : item.publishedBy() + " on " + DATE_TIME.format(item.publishedAt()));
        return values;
    }

    private static List<VacancyNoticeReportRow> rows(PublicationResponse publication) {
        List<VacancyNoticeReportRow> result = new ArrayList<>();
        int number = 1;
        for (RequirementSnapshotResponse item : publication.requirements()) {
            result.add(new VacancyNoticeReportRow(number++,
                    item.competencyCode() + " - " + item.competencyName(),
                    item.requiredLevelCode() + " - " + item.requiredLevelLabel(),
                    value(item.classification()), emptyAsNone(item.criticalityCode()),
                    emptyAsNone(item.remarks())));
        }
        if (result.isEmpty()) {
            result.add(new VacancyNoticeReportRow(1, "No competency requirement recorded",
                    "—", "—", "—", "—"));
        }
        return result;
    }

    private static String emptyAsNone(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private static String value(Object value) {
        return value == null ? "" : value.toString();
    }
}
