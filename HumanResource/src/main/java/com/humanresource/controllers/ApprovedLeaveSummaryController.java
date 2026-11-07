package com.humanresource.controllers;

import com.humanresource.onboarding.HrmPermissionGuard;
import com.humanresource.reports.ApprovedLeaveSummaryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/hrm/reports")
public class ApprovedLeaveSummaryController {
    private final ApprovedLeaveSummaryService reports;
    private final HrmPermissionGuard permission;

    public ApprovedLeaveSummaryController(ApprovedLeaveSummaryService reports, HrmPermissionGuard permission) {
        this.reports = reports;
        this.permission = permission;
    }

    @GetMapping(value = "/approved-leave-summary", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> print(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam long areaId,
            @RequestParam(required = false) Long businessUnitId,
            @RequestParam(required = false) String leaveType,
            @RequestParam(defaultValue = "ALL") String payFilter) throws Exception {
        permission.require(token, "hrm.reports.leaveSummary", HrmPermissionGuard.Action.ACCESS);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Approved_Leave_Summary.pdf\"")
                .body(reports.generate(from, to, areaId, businessUnitId, leaveType, payFilter));
    }
}
