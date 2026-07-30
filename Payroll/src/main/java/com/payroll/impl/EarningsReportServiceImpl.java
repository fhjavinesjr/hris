package com.payroll.impl;

import com.payroll.services.EarningsReportService;
import net.sf.jasperreports.engine.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Service
public class EarningsReportServiceImpl implements EarningsReportService {

    private final ResourceLoader resourceLoader;
    private final DataSource dataSource;
    private final ReportSignatoryPositionResolver positionResolver;

    public EarningsReportServiceImpl(ResourceLoader resourceLoader,
                                     DataSource dataSource,
                                     ReportSignatoryPositionResolver positionResolver) {
        this.resourceLoader = resourceLoader;
        this.dataSource = dataSource;
        this.positionResolver = positionResolver;
    }

    @Override
    public void generateEarningsReportPdf(String salaryPeriodKey,
                                          String category,
                                          String earningTypeId,
                                          String earningTypeName,
                                          String earningTypeCode,
                                          Boolean hazardPay,
                                          String currentCompany,
                                          String reportPeriodLabel,
                                          String preparedBy,
                                          String preparedByEmployeeNo,
                                          String certifiedBy,
                                          String certifiedByEmployeeNo,
                                          String approvedBy,
                                          String approvedByEmployeeNo,
                                          OutputStream out) throws Exception {

        if (salaryPeriodKey == null || salaryPeriodKey.isBlank()) {
            throw new IllegalArgumentException("salaryPeriodKey is required.");
        }
        if (earningTypeName == null || earningTypeName.isBlank()) {
            throw new IllegalArgumentException("earningTypeName is required.");
        }

        String reportPath = "classpath:reports/generic_payroll_earning.jrxml";
        JasperReport report = compile(reportPath);

        Map<String, Object> params = new HashMap<>();
        params.put("SALARY_PERIOD_KEY", salaryPeriodKey.trim());
        params.put("EARNING_TYPE_ID", safe(earningTypeId));
        params.put("EARNING_TYPE_NAME", safe(earningTypeName));
        params.put("EARNING_TYPE_CODE", safe(earningTypeCode));
        params.put("CATEGORY", safe(category));
        params.put("CURRENT_COMPANY", safe(currentCompany));
        params.put("REPORT_PERIOD_LABEL", safe(reportPeriodLabel != null && !reportPeriodLabel.isBlank() ? reportPeriodLabel : salaryPeriodKey));
        params.put("PREPARED_BY", safe(preparedBy));
        params.put("PREPARED_BY_EMPLOYEE_NO", safe(preparedByEmployeeNo));
        params.put("CERTIFIED_BY", safe(certifiedBy));
        params.put("CERTIFIED_BY_EMPLOYEE_NO", safe(certifiedByEmployeeNo));
        params.put("APPROVED_BY", safe(approvedBy));
        params.put("APPROVED_BY_EMPLOYEE_NO", safe(approvedByEmployeeNo));
        params.put("PREPARED_BY_POSITION", positionResolver.resolve(
                salaryPeriodKey, preparedByEmployeeNo, preparedBy));
        params.put("CERTIFIED_BY_POSITION", positionResolver.resolve(
                salaryPeriodKey, certifiedByEmployeeNo, certifiedBy));
        params.put("APPROVED_BY_POSITION", positionResolver.resolve(
                salaryPeriodKey, approvedByEmployeeNo, approvedBy));

        try (Connection conn = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(report, params, conn);
            JasperExportManager.exportReportToPdfStream(print, out);
        }
    }

    private JasperReport compile(String classpathPath) throws Exception {
        Resource resource = resourceLoader.getResource(classpathPath);
        if (!resource.exists()) {
            throw new IllegalStateException("Earnings report JRXML not found: " + classpathPath);
        }
        try (InputStream is = resource.getInputStream()) {
            return JasperCompileManager.compileReport(is);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
