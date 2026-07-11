package com.humanresource.impl;

import com.humanresource.services.PDSReportService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates all four PDS sheets (C1-C4) using pre-compiled .jasper files.
 *
 * Why .jasper instead of compiling .jrxml at runtime?
 * - JasperSoft Studio can already compile the PDS JRXML files successfully.
 * - Spring Boot/JasperReports runtime was failing with "Unable to load report"
 *   while parsing JRXML.
 * - Loading .jasper avoids runtime XML parsing/schema issues.
 *
 * This is still SQL-only: the main reports and subreports execute their own SQL
 * through the same JDBC REPORT_CONNECTION. Java only loads the compiled report
 * templates and passes the subreport templates as parameters.
 */
@Service
public class PDSReportServiceImpl implements PDSReportService {

    @Autowired
    private DataSource dataSource;

    @Override
    public void generatePDS(Long employeeId, OutputStream out) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("EMPLOYEE_ID", employeeId);

        // Load compiled subreports from src/main/resources/reports/*.jasper
        params.put("PDS_C1_CHILDREN_SUBREPORT", loadCompiledReport("reports/pds_c1_children_sub.jasper"));

        params.put("PDS_C2_CIVILSERVICE_SUBREPORT", loadCompiledReport("reports/pds_c2_civilservice_sub.jasper"));
        params.put("PDS_C2_WORKEXPERIENCE_SUBREPORT", loadCompiledReport("reports/pds_c2_workexperience_sub.jasper"));

        params.put("PDS_C3_VOLUNTARYWORK_SUBREPORT", loadCompiledReport("reports/pds_c3_voluntarywork_sub.jasper"));
        params.put("PDS_C3_LND_SUBREPORT", loadCompiledReport("reports/pds_c3_lnd_sub.jasper"));
        params.put("PDS_C3_OTHERINFORMATION_SUBREPORT", loadCompiledReport("reports/pds_c3_otherinformation_sub.jasper"));

        params.put("PDS_C4_REFERENCES_SUBREPORT", loadCompiledReport("reports/pds_c4_references_sub.jasper"));

        // Load compiled main reports from src/main/resources/reports/*.jasper
        JasperReport c1 = loadCompiledReport("reports/pds_c1.jasper");
        JasperReport c2 = loadCompiledReport("reports/pds_c2.jasper");
        JasperReport c3 = loadCompiledReport("reports/pds_c3.jasper");
        JasperReport c4 = loadCompiledReport("reports/pds_c4.jasper");

        try (Connection conn = dataSource.getConnection()) {
            JasperPrint p1 = JasperFillManager.fillReport(c1, params, conn);
            JasperPrint p2 = JasperFillManager.fillReport(c2, params, conn);
            JasperPrint p3 = JasperFillManager.fillReport(c3, params, conn);
            JasperPrint p4 = JasperFillManager.fillReport(c4, params, conn);

            JasperPrint merged = mergeReports(List.of(p1, p2, p3, p4));
            JasperExportManager.exportReportToPdfStream(merged, out);
        }
    }

    private JasperReport loadCompiledReport(String classpathPath) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathPath);
        if (!resource.exists()) {
            throw new IllegalStateException("PDS compiled Jasper file not found in classpath: " + classpathPath);
        }
        try (InputStream is = resource.getInputStream()) {
            Object report = JRLoader.loadObject(is);
            if (!(report instanceof JasperReport)) {
                throw new IllegalStateException("Classpath file is not a JasperReport: " + classpathPath);
            }
            return (JasperReport) report;
        } catch (JRException e) {
            throw new JRException("Unable to load compiled PDS Jasper file: " + classpathPath, e);
        }
    }

    private JasperPrint mergeReports(List<JasperPrint> prints) {
        JasperPrint master = prints.get(0);
        for (int i = 1; i < prints.size(); i++) {
            for (JRPrintPage page : prints.get(i).getPages()) {
                master.addPage(page);
            }
        }
        return master;
    }
}
