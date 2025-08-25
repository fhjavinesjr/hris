package com.timekeeping.controllers;

import com.timekeeping.dtos.WorkScheduleDTO;
import com.timekeeping.entitymodels.WorkSchedule;
import com.timekeeping.services.WorkScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class WorkScheduleController {

    private final WorkScheduleService workScheduleService;

    public WorkScheduleController(WorkScheduleService workScheduleService) {
        this.workScheduleService = workScheduleService;
    }

    @PostMapping("/create/work-schedule")
    public ResponseEntity<WorkScheduleDTO> login(@RequestBody WorkScheduleDTO workScheduleDTO) throws Exception {
        workScheduleDTO = workScheduleService.createWorkSchedule(workScheduleDTO);
        return ResponseEntity.ok(workScheduleDTO);
    }

}
