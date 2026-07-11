package com.payroll.controllers;

import com.payroll.dtos.PayslipDTO;
import com.payroll.services.PayslipService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/payroll-payslip")
public class PayslipController {

    private final PayslipService payslipService;

    public PayslipController(PayslipService payslipService) {
        this.payslipService = payslipService;
    }

    @GetMapping("/{employeeNo}")
    public ResponseEntity<?> getPayslip(
            @PathVariable String employeeNo,
            @RequestParam String salaryPeriodKey,
            @RequestParam(defaultValue = "false") boolean releasedOnly) {
        try {
            PayslipDTO payslip = payslipService.getPayslip(employeeNo, salaryPeriodKey, releasedOnly);
            return ResponseEntity.ok(payslip);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(
                    "PAYSLIP_NOT_FOUND",
                    releasedOnly
                            ? "No released or locked payslip was found for the selected employee and salary period."
                            : "No computed payslip was found for the selected employee and salary period.",
                    ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(error(
                    "INVALID_PAYSLIP_REQUEST",
                    ex.getMessage(),
                    null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(
                    "PAYSLIP_LOAD_ERROR",
                    "The payroll service encountered an error while loading the payslip.",
                    ex.getMessage()));
        }
    }

    @GetMapping(value = "/{employeeNo}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public void getPayslipPdf(
            @PathVariable String employeeNo,
            @RequestParam String salaryPeriodKey,
            @RequestParam(defaultValue = "false") boolean releasedOnly,
            HttpServletResponse response
    ) throws Exception {

        String fileName = "Payslip_" + employeeNo + "_" + salaryPeriodKey + ".pdf";
        String encodedFileName = URLEncoder
                .encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(
                "Content-Disposition",
                "inline; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName
        );

        payslipService.generatePayslipPdf(
                employeeNo,
                salaryPeriodKey,
                releasedOnly,
                response.getOutputStream()
        );

        response.flushBuffer();
    }

    @GetMapping("/periods")
    public ResponseEntity<List<String>> getSalaryPeriods(
            @RequestParam(required = false) String employeeNo) {
        return ResponseEntity.ok(payslipService.getSalaryPeriods(employeeNo));
    }

    private Map<String, String> error(String code, String message, String detail) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        if (detail != null && !detail.isBlank()) {
            body.put("detail", detail);
        }
        return body;
    }
}
