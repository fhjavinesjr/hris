package com.timekeeping.controllers;

import com.timekeeping.dtos.DTRDTO;
import com.timekeeping.dtos.ResponseDTO;
import com.timekeeping.entitymodels.DTR;
import com.timekeeping.services.DTRService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DTRController {

    private final DTRService dtrService;

    public DTRController(DTRService dtrService) {
        this.dtrService = dtrService;
    }

    @PostMapping("/employee/create/dtr")
    public ResponseEntity<Boolean> createDTR(@RequestBody DTRDTO dtrdto) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(dtrService.createEmployeeDTR(dtrdto));
    }

    @GetMapping("/employee/dtr")
    public ResponseEntity<List<DTRDTO>> getEmployeeDTR(@RequestParam("employeeNo") String employeeNo, @RequestParam @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime fromDate, @RequestParam @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime toDate) throws Exception {
        List<DTRDTO> dtrdtoList = dtrService.getEmployeeDTR(employeeNo, fromDate, toDate);
        return ResponseEntity.ok(dtrdtoList);
    }

}
