package com.timekeeping.controllers;

import com.timekeeping.dtos.DTRDailyDTO;
import com.timekeeping.services.DTRDailyService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
}

