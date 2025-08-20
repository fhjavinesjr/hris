package com.administrative.controllers;

import com.administrative.dtos.TimeShiftDTO;
import com.administrative.impl.TimeShiftImpl;
import com.administrative.services.TimeShiftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TimeShiftController {

    private final TimeShiftService timeShiftService;

    public TimeShiftController(TimeShiftService timeShiftService) {
        this.timeShiftService = timeShiftService;
    }

    @PostMapping("/time-shift/create")
    public ResponseEntity<TimeShiftDTO> createEmployee(@RequestBody TimeShiftDTO timeShiftDTO) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(timeShiftService.createTimeShift(timeShiftDTO));
    }

}
