package com.payroll.controllers;

import com.payroll.services.RemittanceReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/payroll-reports/remittances")
public class RemittanceReportController {

    private final RemittanceReportService remittanceReportService;

    public RemittanceReportController(RemittanceReportService remittanceReportService) {
        this.remittanceReportService = remittanceReportService;
    }

    @GetMapping(value = "/gsis/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public void generateGsisRemittancePdf(
            @RequestParam String salaryPeriodKey,
            @RequestParam(required = false) String salaryDate,
            @RequestParam(required = false) String currentCompany,
            @RequestParam(required = false) String officeCode,
            @RequestParam(required = false) String preparedBy,
            @RequestParam(required = false) String certifiedBy,
            @RequestParam(required = false) String preparedByEmployeeNo,
            @RequestParam(required = false) String certifiedByEmployeeNo,
            HttpServletResponse response
    ) throws Exception {
        prepareDownload(response, "GSIS_Remittance_" + salaryPeriodKey + ".pdf");
        remittanceReportService.generateGsisRemittancePdf(
                salaryPeriodKey, salaryDate, currentCompany, officeCode, preparedBy, certifiedBy,
                preparedByEmployeeNo, certifiedByEmployeeNo, response.getOutputStream());
        response.flushBuffer();
    }

    @GetMapping(value = "/pagibig/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public void generatePagibigRemittancePdf(
            @RequestParam String salaryPeriodKey,
            @RequestParam(required = false) String periodCovered,
            @RequestParam(required = false) String currentCompany,
            @RequestParam(required = false) String employerOfficeId,
            @RequestParam(required = false) String companyAddress,
            @RequestParam(required = false) String employerPagIbigNo,
            @RequestParam(required = false) String certifiedBy,
            @RequestParam(required = false) String certifiedByEmployeeNo,
            HttpServletResponse response
    ) throws Exception {
        prepareDownload(response, "PagIBIG_Remittance_" + salaryPeriodKey + ".pdf");
        remittanceReportService.generatePagibigRemittancePdf(
                salaryPeriodKey, periodCovered, currentCompany, employerOfficeId, companyAddress, employerPagIbigNo,
                certifiedBy, certifiedByEmployeeNo, response.getOutputStream());
        response.flushBuffer();
    }

    @GetMapping(value = "/philhealth/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public void generatePhilhealthRemittancePdf(
            @RequestParam String salaryPeriodKey,
            @RequestParam(required = false) String salaryDate,
            @RequestParam(required = false) String currentCompany,
            @RequestParam(required = false) String companyPhilhealthNo,
            @RequestParam(required = false) String preparedBy,
            @RequestParam(required = false) String preparedByPos,
            @RequestParam(required = false) String certifiedBy,
            @RequestParam(required = false) String certifiedByPos,
            HttpServletResponse response
    ) throws Exception {
        prepareDownload(response, "PhilHealth_Remittance_" + salaryPeriodKey + ".pdf");
        remittanceReportService.generatePhilhealthRemittancePdf(
                salaryPeriodKey, salaryDate, currentCompany, companyPhilhealthNo,
                preparedBy, preparedByPos, certifiedBy, certifiedByPos, response.getOutputStream());
        response.flushBuffer();
    }

    private void prepareDownload(HttpServletResponse response, String fileName) {
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName
        );
    }
}
