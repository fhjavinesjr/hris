package com.timekeeping.controllers;

import com.timekeeping.reports.MraReportService;
import com.timekeeping.reports.AttendanceSummaryType;
import com.timekeeping.reports.TimekeepingReportAccess;
import com.timekeeping.reports.IncompleteDtrReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DateTimeException;
import java.time.YearMonth;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/timekeeping/reports")
public class MraReportController {
    private final MraReportService reports;
    private final TimekeepingReportAccess access;
    private final IncompleteDtrReportService incompleteDtr;

    public MraReportController(MraReportService reports, TimekeepingReportAccess access,
                               IncompleteDtrReportService incompleteDtr) {
        this.reports = reports;
        this.access = access;
        this.incompleteDtr = incompleteDtr;
    }

    @GetMapping(value = "/incomplete-dtr", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> incompleteDtr(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam Long areaId,
            @RequestParam(required = false) Long businessUnitId,
            @RequestParam Long preparedById,
            @RequestParam Long certifiedById,
            Authentication authentication) throws Exception {
        access.requireAccess(authentication, "tk.reports.incompleteDtr");
        byte[] pdf = incompleteDtr.generate(from, to, areaId, businessUnitId, preparedById, certifiedById);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "inline; filename=\"Incomplete_DTR_Review.pdf\"")
                .body(pdf);
    }

    @GetMapping(value = "/monthly-attendance", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> print(@RequestParam int year, @RequestParam int month,
                                        @RequestParam Long areaId,
                                        @RequestParam(required = false) Long businessUnitId,
                                        @RequestParam Long preparedById,
                                        @RequestParam Long certifiedById,
                                        @RequestParam(required = false) Long verifiedById,
                                        Authentication authentication) throws Exception {
        access.requireAccess(authentication, "tk.reports.mra");
        try {
            YearMonth.of(year, month);
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException("Select a valid calendar month and year.");
        }
        byte[] pdf = reports.generate(year, month, areaId, businessUnitId,
                preparedById, certifiedById, verifiedById);
        String filename = "MRA_" + year + "_" + String.format("%02d", month) + ".pdf";
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                .body(pdf);
    }

    @GetMapping(value = "/summaries/{kind}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> printSummary(@PathVariable String kind,
                                               @RequestParam int year, @RequestParam int month,
                                               @RequestParam Long areaId,
                                               @RequestParam(required = false) Long businessUnitId,
                                               @RequestParam Long preparedById,
                                               @RequestParam Long certifiedById,
                                               @RequestParam(required = false) Long verifiedById,
                                               Authentication authentication) throws Exception {
        AttendanceSummaryType type = AttendanceSummaryType.fromSlug(kind);
        access.requireAccess(authentication, type.permissionKey());
        try {
            YearMonth.of(year, month);
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException("Select a valid calendar month and year.");
        }
        byte[] pdf = reports.generateSummary(type, year, month, areaId, businessUnitId,
                preparedById, certifiedById, verifiedById);
        String filename = type.name() + "_" + year + "_" + String.format("%02d", month) + ".pdf";
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
