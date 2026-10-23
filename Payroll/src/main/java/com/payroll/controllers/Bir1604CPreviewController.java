package com.payroll.controllers;

import com.payroll.impl.Bir1604CPreviewService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/annualization/1604c-preview")
public class Bir1604CPreviewController {
    private final Bir1604CPreviewService service;

    public Bir1604CPreviewController(Bir1604CPreviewService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_PDF_VALUE)
    public void preview(@RequestParam int year,
                        @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
                        HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader("Content-Disposition",
                "inline; filename=\"BIR_1604C_DRAFT_" + year + ".pdf\"");
        service.generateDraft(year, authorizationHeader, response.getOutputStream());
        response.flushBuffer();
    }
}
