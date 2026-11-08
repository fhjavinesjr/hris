package com.primehr.succession.report;

import com.primehr.reports.JasperReportRegistry;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class SuccessionReportRenderer {
    public byte[] render(String title, String subtitle, String generatedBy, List<SuccessionReportData.Row> rows) {
        try {
            var parameters = new HashMap<String, Object>();
            parameters.put("reportTitle", title);
            parameters.put("reportSubtitle", subtitle);
            parameters.put("generatedBy", generatedBy);
            var print = JasperFillManager.fillReport(
                    JasperReportRegistry.get("reports/succession_formal_report.jrxml"),
                    parameters, new JRBeanCollectionDataSource(rows));
            return JasperExportManager.exportReportToPdf(print);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to render the succession planning report", exception);
        }
    }
}
