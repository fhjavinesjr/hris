package com.primehr.learningdevelopment.report.application;

import com.primehr.learningdevelopment.report.application.LdFormalReportData.Document;
import com.primehr.reports.JasperReportRegistry;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LdFormalReportRenderer {
    public byte[] render(Document document) {
        try {
            var h = document.header();
            Map<String,Object> parameters = new LinkedHashMap<>();
            parameters.put("title",h.title()); parameters.put("subject",h.subject());
            parameters.put("lifecycle",h.lifecycle()); parameters.put("employee",h.employee());
            parameters.put("organization",h.organization()); parameters.put("period",h.period());
            parameters.put("sourceReference",h.sourceReference()); parameters.put("generated",h.generated());
            parameters.put("templateVersion","PHASE-7E-v1");
            List<Map<String,?>> rows=document.rows().stream().map(row->{Map<String,?> values=Map.of(
                    "rowNumber",row.rowNumber(),"section",row.section(),"label",row.label(),
                    "detail",row.detail(),"status",row.status());return values;}).collect(Collectors.toList());
            var print = JasperFillManager.fillReport(JasperReportRegistry.get("reports/ld_formal_report.jrxml"),
                    parameters,new JRMapCollectionDataSource(rows));
            return JasperExportManager.exportReportToPdf(print);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate L&D formal report",exception);
        }
    }
}
