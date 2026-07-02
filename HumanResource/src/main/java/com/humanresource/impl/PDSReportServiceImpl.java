package com.humanresource.impl;

import com.humanresource.services.PDSReportService;
import jakarta.annotation.PostConstruct;
import net.sf.jasperreports.engine.*;
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
 * Generates all four PDS sheets (C1–C4) by compiling JRXML templates that
 * contain embedded SQL queries. JasperReports executes those queries directly
 * against the database using a JDBC connection from the Spring DataSource.
 *
 * XML schema validation is disabled via jasperreports.properties on the
 * classpath, which eliminates the "Unable to load report / jrDesign is null"
 * error caused by the parser trying to fetch the XSD from the internet.
 */
@Service
public class PDSReportServiceImpl implements PDSReportService {

    @Autowired
    private DataSource dataSource;

    /**
     * Disable XML schema validation programmatically at startup.
     * This prevents the SAX parser from trying to fetch the XSD from the
     * internet (jasperreports.sourceforge.net), which causes
     * "Unable to load report" when the server has no outbound internet access.
     */
    @PostConstruct
    private void disableSchemaValidation() {
        System.setProperty("net.sf.jasperreports.compiler.xml.parser.validation", "false");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Public API
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public void generatePDS(Long employeeId, OutputStream out) throws Exception {

        // Compile all four JRXML templates from classpath.
        // jasperreports.properties disables schema validation so the XML
        // loader never needs to reach the internet.
        JasperReport c1 = compile("reports/pds_c1.jrxml");
        JasperReport c2 = compile("reports/pds_c2.jrxml");
        JasperReport c3 = compile("reports/pds_c3.jrxml");
        JasperReport c4 = compile("reports/pds_c4.jrxml");

        // Single parameter passed to every sheet.
        Map<String, Object> params = new HashMap<>();
        params.put("EMPLOYEE_ID", employeeId);

        // One connection is enough — fillReport() fetches all data synchronously
        // and releases the ResultSet before returning.
        try (Connection conn = dataSource.getConnection()) {
            JasperPrint p1 = JasperFillManager.fillReport(c1, params, conn);
            JasperPrint p2 = JasperFillManager.fillReport(c2, params, conn);
            JasperPrint p3 = JasperFillManager.fillReport(c3, params, conn);
            JasperPrint p4 = JasperFillManager.fillReport(c4, params, conn);

            JasperPrint merged = mergeReports(List.of(p1, p2, p3, p4));
            JasperExportManager.exportReportToPdfStream(merged, out);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Compile a JRXML from the classpath. Schema validation is globally
     * disabled by jasperreports.properties, so the loader never touches
     * the internet.
     */
    private JasperReport compile(String classpathPath) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathPath);
        try (InputStream is = resource.getInputStream()) {
            return JasperCompileManager.compileReport(is);
        }
    }

    /**
     * Merge all JasperPrint objects into one so that they export as a
     * single continuous PDF document.
     */
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


