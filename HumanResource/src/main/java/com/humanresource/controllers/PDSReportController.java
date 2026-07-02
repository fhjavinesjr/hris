package com.humanresource.controllers;

import com.humanresource.services.PDSReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PDSReportController {

    private final PDSReportService pdsReportService;

    public PDSReportController(PDSReportService pdsReportService) {
        this.pdsReportService = pdsReportService;
    }

    /**
     * GET /api/pds/report/{employeeId}
     *
     * Returns a PDF containing all four PDS sheets (C1–C4) for the specified employee.
     */
    @GetMapping("/pds/report/{employeeId}")
    public void downloadPDS(@PathVariable Long employeeId,
                            HttpServletResponse response) throws Exception {

        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"PDS_" + employeeId + ".pdf\""
        );

        pdsReportService.generatePDS(employeeId, response.getOutputStream());
        response.flushBuffer();
    }
}
