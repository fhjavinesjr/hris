package com.primehr.rewardsrecognition.report;

import com.primehr.reports.JasperReportRegistry;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RrReportRenderer {
    public byte[] render(
            String title,
            String subtitle,
            String generatedBy,
            List<RrReportData.Row> rows
    ) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("reportTitle", title);
            parameters.put("reportSubtitle", subtitle);
            parameters.put("generatedBy", generatedBy);
            var print = JasperFillManager.fillReport(
                    JasperReportRegistry.get("reports/rr_formal_report.jrxml"),
                    parameters,
                    new JRBeanCollectionDataSource(rows)
            );
            return JasperExportManager.exportReportToPdf(print);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to render the Rewards and Recognition report",
                    exception
            );
        }
    }
}
