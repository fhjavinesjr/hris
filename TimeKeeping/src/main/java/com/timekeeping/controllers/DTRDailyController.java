package com.timekeeping.controllers;

import com.timekeeping.dtos.DTRDailyDTO;
import com.timekeeping.services.DTRDailyService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dtr-daily")
public class DTRDailyController {
    private final DTRDailyService dtrDailyService;

    public DTRDailyController(DTRDailyService dtrDailyService) {
        this.dtrDailyService = dtrDailyService;
    }

    @PostMapping
    public ResponseEntity<DTRDailyDTO> createOrUpdateDTRDaily(@RequestBody DTRDailyDTO dtrDailyDTO) {
        DTRDailyDTO saved = dtrDailyService.createOrUpdateDTRDaily(dtrDailyDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<DTRDailyDTO>> getEmployeeDTRDaily(
            @RequestParam String employeeId,
            @RequestParam @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime toDate) {
        List<DTRDailyDTO> list = dtrDailyService.getEmployeeDTRDaily(employeeId, fromDate, toDate);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/report")
    public void downloadDtrReport(@RequestParam String employeeId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                  HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=\"DTR_" + employeeId + "_" + fromDate + "_" + toDate + ".pdf\"");
        dtrDailyService.generateDtrReport(employeeId, fromDate, toDate, response.getOutputStream());
    }
}
