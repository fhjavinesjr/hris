package com.payroll.controllers;

import com.payroll.services.EarningsReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/payroll-reports/earnings")
public class EarningsReportController {

    private final EarningsReportService earningsReportService;

    public EarningsReportController(EarningsReportService earningsReportService) {
        this.earningsReportService = earningsReportService;
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public void generateEarningsReportPdf(
            @RequestParam String salaryPeriodKey,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String earningTypeId,
            @RequestParam String earningTypeName,
            @RequestParam(required = false) String earningTypeCode,
            @RequestParam(defaultValue = "false") Boolean hazardPay,
            @RequestParam(required = false) String currentCompany,
            @RequestParam(required = false) String reportPeriodLabel,
            HttpServletResponse response
    ) throws Exception {

        String cleanType = earningTypeName == null
                ? "Earning"
                : earningTypeName.replaceAll("[^a-zA-Z0-9_-]", "_");

        String fileName = cleanType + "_Report_" + salaryPeriodKey + ".pdf";
        String encodedFileName = URLEncoder
                .encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName
        );

        earningsReportService.generateEarningsReportPdf(
                salaryPeriodKey,
                category,
                earningTypeId,
                earningTypeName,
                earningTypeCode,
                hazardPay,
                currentCompany,
                reportPeriodLabel,
                response.getOutputStream()
        );

        response.flushBuffer();
    }
}
