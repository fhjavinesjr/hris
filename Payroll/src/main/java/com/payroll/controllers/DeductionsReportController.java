package com.payroll.controllers;

import com.payroll.services.DeductionsReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/payroll-reports/deductions")
public class DeductionsReportController {

    private final DeductionsReportService deductionsReportService;

    public DeductionsReportController(DeductionsReportService deductionsReportService) {
        this.deductionsReportService = deductionsReportService;
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public void generateDeductionsReportPdf(
            @RequestParam String salaryPeriodKey,
            @RequestParam(required = false) String deductionTypeId,
            @RequestParam String deductionTypeName,
            @RequestParam(required = false) String deductionTypeCode,
            @RequestParam(required = false) String currentCompany,
            @RequestParam(required = false) String reportPeriodLabel,
            @RequestParam(required = false) String preparedBy,
            @RequestParam(required = false) String preparedByEmployeeNo,
            @RequestParam(required = false) String certifiedBy,
            @RequestParam(required = false) String certifiedByEmployeeNo,
            @RequestParam(required = false) String approvedBy,
            @RequestParam(required = false) String approvedByEmployeeNo,
            HttpServletResponse response
    ) throws Exception {

        String cleanType = deductionTypeName == null ? "Deduction" : deductionTypeName.replaceAll("[^a-zA-Z0-9_-]", "_");
        String fileName = cleanType + "_Deduction_Report_" + salaryPeriodKey + ".pdf";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName
        );

        deductionsReportService.generateDeductionsReportPdf(
                salaryPeriodKey,
                deductionTypeId,
                deductionTypeName,
                deductionTypeCode,
                currentCompany,
                reportPeriodLabel,
                preparedBy,
                preparedByEmployeeNo,
                certifiedBy,
                certifiedByEmployeeNo,
                approvedBy,
                approvedByEmployeeNo,
                response.getOutputStream()
        );
        response.flushBuffer();
    }
}
