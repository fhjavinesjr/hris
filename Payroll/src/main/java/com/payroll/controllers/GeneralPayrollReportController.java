package com.payroll.controllers;

import com.payroll.services.GeneralPayrollReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/payroll-general-report")
public class GeneralPayrollReportController {

    private final GeneralPayrollReportService generalPayrollReportService;

    public GeneralPayrollReportController(GeneralPayrollReportService generalPayrollReportService) {
        this.generalPayrollReportService = generalPayrollReportService;
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public void generateGeneralPayrollPdf(
            @RequestParam String salaryPeriodKey,
            @RequestParam(required = false) String payrollGroup,
            @RequestParam(required = false) String currentCompany,
            @RequestParam(required = false) String preparedBy,
            @RequestParam(required = false) String approvedBy,
            @RequestParam(required = false) String cashierBy,
            @RequestParam(required = false) String preparedByEmployeeNo,
            @RequestParam(required = false) String approvedByEmployeeNo,
            @RequestParam(required = false) String cashierByEmployeeNo,
            HttpServletResponse response
    ) throws Exception {

        String fileName = "General_Payroll_" + salaryPeriodKey + ".pdf";
        String encodedFileName = URLEncoder
                .encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(
                "Content-Disposition",
                "inline; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName
        );

        generalPayrollReportService.generateGeneralPayrollPdf(
                salaryPeriodKey,
                payrollGroup,
                currentCompany,
                preparedBy,
                approvedBy,
                cashierBy,
                preparedByEmployeeNo,
                approvedByEmployeeNo,
                cashierByEmployeeNo,
                response.getOutputStream()
        );
        response.flushBuffer();
    }
}
