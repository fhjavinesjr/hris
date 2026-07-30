package com.payroll.impl;

import com.payroll.dtos.GeneralPayrollReportRow;
import com.payroll.services.GeneralPayrollReportService;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeneralPayrollReportServiceImpl implements GeneralPayrollReportService {

    private final GeneralPayrollReportDataLoader dataLoader;
    private final ReportSignatoryPositionResolver positionResolver;

    public GeneralPayrollReportServiceImpl(
            GeneralPayrollReportDataLoader dataLoader,
            ReportSignatoryPositionResolver positionResolver) {
        this.dataLoader = dataLoader;
        this.positionResolver = positionResolver;
    }

    @Override
    public void generateGeneralPayrollPdf(String salaryPeriodKey,
                                          String payrollGroup,
                                          String currentCompany,
                                          String preparedBy,
                                          String approvedBy,
                                          String cashierBy,
                                          String preparedByEmployeeNo,
                                          String approvedByEmployeeNo,
                                          String cashierByEmployeeNo,
                                          OutputStream out) throws Exception {
        if (salaryPeriodKey == null || salaryPeriodKey.isBlank()) {
            throw new IllegalArgumentException("Salary period key is required.");
        }

        JasperReport report = compile("reports/general_payroll.jrxml");

        Map<String, Object> params = new HashMap<>();
        String cleanSalaryPeriodKey = salaryPeriodKey.trim();
        String cleanPayrollGroup =
                clean(payrollGroup, "REGULAR").toUpperCase();
        String cleanPreparedBy = clean(preparedBy, "");
        String cleanApprovedBy = clean(approvedBy, "");
        String cleanCashierBy = clean(cashierBy, "");
        String cleanPreparedByEmployeeNo = clean(preparedByEmployeeNo, "");
        String cleanApprovedByEmployeeNo = clean(approvedByEmployeeNo, "");
        String cleanCashierByEmployeeNo = clean(cashierByEmployeeNo, "");

        params.put("salaryPeriodKey", cleanSalaryPeriodKey);
        params.put("payrollGroup", cleanPayrollGroup);
        params.put("currentCompany", clean(currentCompany, "ISOFT HRIS"));
        params.put("preparedBy", cleanPreparedBy);
        params.put("approvedBy", cleanApprovedBy);
        params.put("cashierBy", cleanCashierBy);
        params.put("preparedByEmployeeNo", cleanPreparedByEmployeeNo);
        params.put("approvedByEmployeeNo", cleanApprovedByEmployeeNo);
        params.put("cashierByEmployeeNo", cleanCashierByEmployeeNo);
        params.put("preparedByPosition", positionResolver.resolve(
                cleanSalaryPeriodKey,
                cleanPreparedByEmployeeNo,
                cleanPreparedBy
        ));
        params.put("approvedByPosition", positionResolver.resolve(
                cleanSalaryPeriodKey,
                cleanApprovedByEmployeeNo,
                cleanApprovedBy
        ));
        params.put("cashierByPosition", positionResolver.resolve(
                cleanSalaryPeriodKey,
                cleanCashierByEmployeeNo,
                cleanCashierBy
        ));

        List<GeneralPayrollReportRow> rows =
                dataLoader.load(cleanSalaryPeriodKey, cleanPayrollGroup);
        JasperPrint print = JasperFillManager.fillReport(
                report,
                params,
                new JRBeanCollectionDataSource(rows)
        );
        JasperExportManager.exportReportToPdfStream(print, out);
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
