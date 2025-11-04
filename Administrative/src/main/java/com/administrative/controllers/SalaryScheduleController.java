package com.administrative.controllers;

import com.administrative.dtos.SalaryScheduleDTO;
import com.administrative.services.SalaryScheduleService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}