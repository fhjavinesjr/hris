package com.payroll.reports;

import com.payroll.dtos.GeneralPayrollReportRow;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GeneralPayrollReportTemplateTest {

    @Test
    void compilesAndExportsRepresentativeBeanBackedPdf() throws Exception {
        ClassPathResource resource =
                new ClassPathResource("reports/general_payroll.jrxml");
        String template = Files.readString(
                resource.getFile().toPath(),
                StandardCharsets.UTF_8
        );
        String normalized = template.toLowerCase(Locale.ROOT);

        assertFalse(normalized.contains("<query"));
        for (String vendorToken : List.of(
                " top ", "isnull(", "outer apply", "string_agg(",
                " limit ", " ilike ", "::"
        )) {
            assertFalse(
                    normalized.contains(vendorToken),
                    () -> "Unexpected provider-specific token: " + vendorToken
            );
        }

        JasperReport report;
        try (InputStream input = resource.getInputStream()) {
            report = JasperCompileManager.compileReport(input);
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("salaryPeriodKey", "2026-07");
        parameters.put("payrollGroup", "REGULAR");
        parameters.put("currentCompany", "ISOFT Test Agency");
        parameters.put("preparedBy", "PREPARED EMPLOYEE");
        parameters.put("approvedBy", "APPROVING EMPLOYEE");
        parameters.put("cashierBy", "CASHIER EMPLOYEE");
        parameters.put("preparedByEmployeeNo", "001");
        parameters.put("approvedByEmployeeNo", "002");
        parameters.put("cashierByEmployeeNo", "003");
        parameters.put("preparedByPosition", "Payroll Officer");
        parameters.put("approvedByPosition", "Agency Head");
        parameters.put("cashierByPosition", "Cashier");

        JasperPrint print = JasperFillManager.fillReport(
                report,
                parameters,
                new JRBeanCollectionDataSource(List.of(representativeRow()))
        );
        byte[] pdf = JasperExportManager.exportReportToPdf(print);

        assertFalse(print.getPages().isEmpty());
        assertArrayEquals(
                "%PDF".getBytes(StandardCharsets.US_ASCII),
                Arrays.copyOf(pdf, 4)
        );
    }

    private GeneralPayrollReportRow representativeRow() {
        GeneralPayrollReportRow row = new GeneralPayrollReportRow();
        row.setRowNo(1);
        row.setEmployeeNo("001");
        row.setEmployeeName("FERDINAND JAVINES");
        row.setDepartment("Human Resource Management");
        row.setSalaryGrade(12);
        row.setSalaryStep(1);
        row.setSalaryPeriodKey("2026-07");
        row.setCutoffStartDate(Date.valueOf(LocalDate.of(2026, 7, 1)));
        row.setCutoffEndDate(Date.valueOf(LocalDate.of(2026, 7, 15)));
        row.setSalaryDate(Date.valueOf(LocalDate.of(2026, 7, 23)));
        row.setActualBasic(15_000.0);
        row.setEarningBreakdown("Basic Salary [BASIC]: 15,000.00");
        row.setGrossAmount(17_000.0);
        row.setDeductionBreakdown("GSIS [GSIS]: 2,000.00");
        row.setTotalDeduction(2_000.0);
        row.setNetAmount(15_000.0);
        row.setGrandActualBasic(15_000.0);
        row.setGrandGrossAmount(17_000.0);
        row.setGrandTotalDeduction(2_000.0);
        row.setGrandNetAmount(15_000.0);
        row.setGrandEarningsBreakdown("Basic Salary [BASIC]: 15,000.00");
        row.setGrandDeductionsBreakdown("GSIS [GSIS]: 2,000.00");
        row.setReportMode("FINAL / LOCKED");
        return row;
    }
}
