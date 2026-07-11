package com.payroll.impl;

import com.payroll.services.GeneralPayrollReportService;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Service
public class GeneralPayrollReportServiceImpl implements GeneralPayrollReportService {

    private final DataSource dataSource;

    public GeneralPayrollReportServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void generateGeneralPayrollPdf(String salaryPeriodKey,
                                          String currentCompany,
                                          String preparedBy,
                                          String approvedBy,
                                          String cashierBy,
                                          OutputStream out) throws Exception {
        if (salaryPeriodKey == null || salaryPeriodKey.isBlank()) {
            throw new IllegalArgumentException("Salary period key is required.");
        }

        JasperReport report = compile("reports/general_payroll.jrxml");

        Map<String, Object> params = new HashMap<>();
        params.put("salaryPeriodKey", salaryPeriodKey.trim());
        params.put("currentCompany", clean(currentCompany, "ISOFT HRIS"));
        params.put("preparedBy", clean(preparedBy, ""));
        params.put("approvedBy", clean(approvedBy, ""));
        params.put("cashierBy", clean(cashierBy, ""));

        try (Connection conn = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(report, params, conn);
            JasperExportManager.exportReportToPdfStream(print, out);
        }
    }

    private JasperReport compile(String classpathPath) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathPath);
        if (!resource.exists()) {
            throw new IllegalStateException("General Payroll JRXML not found in classpath: " + classpathPath);
        }
        try (InputStream is = resource.getInputStream()) {
            try {
                return JasperCompileManager.compileReport(is);
            } catch (JRException ex) {
                throw new JRException("Failed to compile General Payroll JRXML: " + classpathPath + " — " + ex.getMessage(), ex);
            }
        }
    }

    private String clean(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
