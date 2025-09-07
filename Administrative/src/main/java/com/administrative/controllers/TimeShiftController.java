package com.administrative.controllers;

import com.administrative.dtos.TimeShiftDTO;
import com.administrative.services.TimeShiftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TimeShiftController {

    private final TimeShiftService timeShiftService;

    public TimeShiftController(TimeShiftService timeShiftService) {
        this.timeShiftService = timeShiftService;
    }

    @PostMapping("/time-shift/create")
    public ResponseEntity<TimeShiftDTO> createTimeShift(@RequestBody TimeShiftDTO timeShiftDTO) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(timeShiftService.createTimeShift(timeShiftDTO));
    }

    @GetMapping("/getAll/time-shift")
    public ResponseEntity<List<TimeShiftDTO>> getAllTimeShift() throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(timeShiftService.getAllTimeShift());
    }
}
