package com.primehr.gap.report;

import com.primehr.gap.api.CompetencyGapDtos.AnalysisResponse;
import com.primehr.gap.api.CompetencyGapDtos.GapItemResponse;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CompetencyGapReportServiceImpl implements CompetencyGapReportService {
    private static final String TEMPLATE = "reports/competency_gap_report.jrxml";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    @Override
    public byte[] generate(AnalysisResponse analysis) {
        Objects.requireNonNull(analysis, "analysis");
        try (InputStream input = new ClassPathResource(TEMPLATE).getInputStream()) {
            JasperReport report = JasperCompileManager.compileReport(input);
            JasperPrint print = JasperFillManager.fillReport(report, parameters(analysis),
                    new JRBeanCollectionDataSource(rows(analysis)));
            return JasperExportManager.exportReportToPdf(print);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate the competency gap report", exception);
        }
    }

    private static Map<String, Object> parameters(AnalysisResponse analysis) {
        Map<String, Object> values = new HashMap<>();
        values.put("analysisId", value(analysis.id()));
        values.put("analysisDate", value(analysis.analysisDate()));
        values.put("employee", value(analysis.employeeNo()) + " - " + value(analysis.employeeName()));
        values.put("position", value(analysis.positionName()));
        values.put("plantilla", value(analysis.plantillaName()));
        values.put("salary", analysis.salaryGrade() == null ? "Not available"
                : "Grade " + analysis.salaryGrade() + " / Step " + value(analysis.salaryStep()));
        values.put("positionProfile", "Definition v" + analysis.positionProfileVersion()
                + " / revision " + analysis.positionProfileRevision());
        values.put("personProfile", "Version " + analysis.personProfileVersion() + " effective "
                + value(analysis.personProfileValidFrom()) + " to "
                + (analysis.personProfileValidTo() == null ? "Open" : analysis.personProfileValidTo()));
        values.put("priorityScheme", value(analysis.prioritySchemeCode()) + " v" + analysis.prioritySchemeVersion());
        values.put("generated", value(analysis.generatedBy()) + " on "
                + (analysis.generatedAt() == null ? "" : DATE_TIME.format(analysis.generatedAt())));
        return values;
    }

    private static List<CompetencyGapReportRow> rows(AnalysisResponse analysis) {
        List<CompetencyGapReportRow> rows = new ArrayList<>();
        int number = 1;
        for (GapItemResponse item : analysis.items()) {
            String formula = item.gap() == null ? "Not calculated"
                    : item.requiredLevelOrder() + " - " + item.attainedLevelOrder() + " = " + item.gap();
            String explanation = item.classification().name().equals("NOT_ASSESSED")
                    ? "Reason: " + value(item.notAssessedReason())
                    : value(item.priorityExplanation());
            rows.add(new CompetencyGapReportRow(number++,
                    item.competencyCode() + " - " + item.competencyName(),
                    item.requiredLevelCode() + " - " + item.requiredLevelLabel()
                            + " (order " + item.requiredLevelOrder() + ")",
                    item.attainedLevelCode() == null ? "Not assessed"
                            : item.attainedLevelCode() + " - " + item.attainedLevelLabel()
                                    + " (order " + item.attainedLevelOrder() + ")",
                    formula, item.classification().name(),
                    item.requirementClassification() + (item.criticalityCode() == null ? ""
                            : " / " + item.criticalityCode()),
                    item.priorityCode() == null ? "—"
                            : item.priorityCode() + " - " + item.priorityLabel() + " (rank "
                                    + item.priorityRank() + ")",
                    explanation.isBlank() ? "—" : explanation));
        }
        return rows;
    }

    private static String value(Object value) {
        return value == null ? "" : value.toString();
    }
}
