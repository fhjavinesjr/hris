package com.administrative.controllers;

import com.administrative.dtos.SalaryScheduleDTO;
import com.administrative.dtos.TimeShiftDTO;
import com.administrative.services.SalaryScheduleService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SalaryScheduleController {

    private final SalaryScheduleService salaryScheduleService;

    public SalaryScheduleController(SalaryScheduleService salaryScheduleService) {
        this.salaryScheduleService = salaryScheduleService;
    }

    @PostMapping("/salary-schedule/create")
    public ResponseEntity<MetadataResponse> createSalarySchedule(@RequestBody List<SalaryScheduleDTO> salaryScheduleDTOList) throws Exception {
        salaryScheduleDTOList = salaryScheduleService.createSalarySchedule(salaryScheduleDTOList);
        if(salaryScheduleDTOList == null || salaryScheduleDTOList.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create Salary Schedule"));
        }

        return ResponseEntity.ok(new MetadataResponse(200L, "Successful to create Salary Schedule"));
    }

    @GetMapping("/salary-schedule/get-all")
    public ResponseEntity<List<SalaryScheduleDTO>> getAllSalarySchedule() throws Exception {
        List<SalaryScheduleDTO> salaryScheduleDTOList = salaryScheduleService.getAllSalarySchedule();
        return ResponseEntity.ok(salaryScheduleDTOList);
    }

    @PutMapping("/salary-schedule/update")
    public ResponseEntity<MetadataResponse> updateSalarySchedule(@RequestBody List<SalaryScheduleDTO> salaryScheduleDTOList) throws Exception {
        salaryScheduleDTOList = salaryScheduleService.updateSalarySchedule(salaryScheduleDTOList);
        if(salaryScheduleDTOList == null || salaryScheduleDTOList.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update salary schedule"));
        }

        return ResponseEntity.ok(new MetadataResponse(0L, "Successful to update salary schedule"));
    }

    @DeleteMapping("/salary-schedule/delete/{effectivityDate}")
    public ResponseEntity<MetadataResponse> deleteSalarySchedule(@PathVariable("effectivityDate") @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime effectivityDate) throws Exception {
        Boolean boolDel = salaryScheduleService.deleteSalarySchedule(effectivityDate);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete salary schedule"));
        }

        return ResponseEntity.ok(new MetadataResponse(0L, "Successful to delete salary schedule"));
    }
}