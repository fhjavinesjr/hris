package com.administrative.controllers;

import com.administrative.dtos.TimeShiftDTO;
import com.administrative.services.TimeShiftService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.data.repository.query.Param;
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

    @PutMapping("/time-shift/update/{timeShiftId}")
    public ResponseEntity<MetadataResponse> updateTimeShift(@PathVariable("timeShiftId") Long timeShiftId, @RequestBody TimeShiftDTO timeShiftDTO) throws Exception {
        timeShiftDTO = timeShiftService.updateTimeShift(timeShiftId, timeShiftDTO);
        if(timeShiftDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update work schedule"));
        }

        return ResponseEntity.ok(new MetadataResponse(timeShiftDTO.getTimeShiftId(), "Successful to update Time Shift"));
    }

    @DeleteMapping("/time-shift/delete/{timeShiftId}")
    public ResponseEntity<MetadataResponse> deleteTimeShift(@PathVariable("timeShiftId") Long timeShiftId) throws Exception {
        Boolean boolDel = timeShiftService.deleteTimeShift(timeShiftId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete work schedule"));
        }

        return ResponseEntity.ok(new MetadataResponse(timeShiftId, "Successful to delete Time Shift"));
    }
}
