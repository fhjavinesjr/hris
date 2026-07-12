package com.payroll.impl;

import com.payroll.services.DeductionsReportService;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
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
public class DeductionsReportServiceImpl implements DeductionsReportService {

    private final ResourceLoader resourceLoader;
    private final DataSource dataSource;

    public DeductionsReportServiceImpl(ResourceLoader resourceLoader, DataSource dataSource) {
        this.resourceLoader = resourceLoader;
        this.dataSource = dataSource;
    }

    @Override
    public void generateDeductionsReportPdf(String salaryPeriodKey,
                                            String deductionTypeId,
                                            String deductionTypeName,
                                            String deductionTypeCode,
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
        if (deductionTypeName == null || deductionTypeName.isBlank()) {
            throw new IllegalArgumentException("deductionTypeName is required.");
        }

        JasperReport report = compile("classpath:reports/generic_payroll_deduction.jrxml");

        Map<String, Object> params = new HashMap<>();
        params.put("SALARY_PERIOD_KEY", safe(salaryPeriodKey));
        params.put("DEDUCTION_TYPE_ID", safe(deductionTypeId));
        params.put("DEDUCTION_TYPE_NAME", safe(deductionTypeName));
        params.put("DEDUCTION_TYPE_CODE", safe(deductionTypeCode));
        params.put("CURRENT_COMPANY", safe(currentCompany));
        params.put("REPORT_PERIOD_LABEL", safe(reportPeriodLabel != null && !reportPeriodLabel.isBlank() ? reportPeriodLabel : salaryPeriodKey));
        params.put("PREPARED_BY", safe(preparedBy));
        params.put("PREPARED_BY_EMPLOYEE_NO", safe(preparedByEmployeeNo));
        params.put("CERTIFIED_BY", safe(certifiedBy));
        params.put("CERTIFIED_BY_EMPLOYEE_NO", safe(certifiedByEmployeeNo));
        params.put("APPROVED_BY", safe(approvedBy));
        params.put("APPROVED_BY_EMPLOYEE_NO", safe(approvedByEmployeeNo));

        try (Connection conn = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(report, params, conn);
            JasperExportManager.exportReportToPdfStream(print, out);
        }
    }

    private JasperReport compile(String classpathPath) throws Exception {
        Resource resource = resourceLoader.getResource(classpathPath);
        if (!resource.exists()) {
            throw new IllegalStateException("Deductions report JRXML not found: " + classpathPath);
        }
        try (InputStream is = resource.getInputStream()) {
            return JasperCompileManager.compileReport(is);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
