package com.payroll.impl;

import com.payroll.services.RemittanceReportService;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class RemittanceReportServiceImpl implements RemittanceReportService {

    private final DataSource dataSource;
    private final ResourceLoader resourceLoader;

    public RemittanceReportServiceImpl(DataSource dataSource, ResourceLoader resourceLoader) {
        this.dataSource = dataSource;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void generateGsisRemittancePdf(String salaryPeriodKey,
                                          String salaryDate,
                                          String currentCompany,
                                          String officeCode,
                                          String preparedBy,
                                          String certifiedBy,
                                          String preparedByEmployeeNo,
                                          String certifiedByEmployeeNo,
                                          OutputStream out) throws Exception {
        Map<String, Object> params = baseParams(salaryPeriodKey, currentCompany);
        params.put("salaryDate", nvl(salaryDate));
        params.put("officeCode", nvl(officeCode));
        params.put("preparedBy", nvl(preparedBy));
        params.put("certifiedBy", nvl(certifiedBy));
        params.put("preparedByEmployeeNo", nvl(preparedByEmployeeNo));
        params.put("certifiedByEmployeeNo", nvl(certifiedByEmployeeNo));
        export("reports/gsis_remittance.jrxml", params, out);
    }

    @Override
    public void generatePagibigRemittancePdf(String salaryPeriodKey,
                                             String periodCovered,
                                             String currentCompany,
                                             String employerOfficeId,
                                             String companyAddress,
                                             String employerPagIbigNo,
                                             String certifiedBy,
                                             String certifiedByEmployeeNo,
                                             OutputStream out) throws Exception {
        Map<String, Object> params = baseParams(salaryPeriodKey, currentCompany);
        params.put("runDate", new Date());
        params.put("employerOfficeId", nvl(employerOfficeId));
        params.put("companyAddress", nvl(companyAddress));
        params.put("employerPagIbigNo", nvl(employerPagIbigNo));
        params.put("periodCovered", nvl(periodCovered));
        params.put("perCov", nvl(periodCovered));
        params.put("certifiedBy", nvl(certifiedBy));
        params.put("certifiedByEmployeeNo", nvl(certifiedByEmployeeNo));
        export("reports/pagibigmembershipremittance.jrxml", params, out);
    }

    @Override
    public void generatePhilhealthRemittancePdf(String salaryPeriodKey,
                                                String salaryDate,
                                                String currentCompany,
                                                String companyPhilhealthNo,
                                                String preparedBy,
                                                String preparedByPos,
                                                String certifiedBy,
                                                String certifiedByPos,
                                                OutputStream out) throws Exception {
        Map<String, Object> params = baseParams(salaryPeriodKey, currentCompany);
        params.put("salaryDate", nvl(salaryDate));
        params.put("companyPhilhealthNo", nvl(companyPhilhealthNo));
        params.put("preparedBy", nvl(preparedBy));
        params.put("preparedByPos", nvl(preparedByPos));
        params.put("certifiedBy", nvl(certifiedBy));
        params.put("certifiedByPos", nvl(certifiedByPos));
        export("reports/philhealth_remittance.jrxml", params, out);
    }

    private Map<String, Object> baseParams(String salaryPeriodKey, String currentCompany) {
        if (salaryPeriodKey == null || salaryPeriodKey.isBlank()) {
            throw new IllegalArgumentException("Salary period is required.");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("salaryPeriodKey", salaryPeriodKey.trim());
        params.put("currentCompany", nvl(currentCompany, "ISOFT HRIS"));
        return params;
    }

    private void export(String reportPath, Map<String, Object> params, OutputStream out) throws Exception {
        Resource resource = resourceLoader.getResource("classpath:" + reportPath);
        if (!resource.exists()) {
            throw new IllegalStateException("Report template not found: " + reportPath);
        }
        try (InputStream is = resource.getInputStream(); Connection conn = dataSource.getConnection()) {
            JasperReport report = JasperCompileManager.compileReport(is);
            JasperPrint print = JasperFillManager.fillReport(report, params, conn);
            JasperExportManager.exportReportToPdfStream(print, out);
        }
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    private String nvl(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
