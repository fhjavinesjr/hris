package com.timekeeping.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.timekeeping.dtos.WorkScheduleDTO;
import com.timekeeping.services.WorkScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class WorkScheduleController {

    private final WorkScheduleService workScheduleService;

    public WorkScheduleController(WorkScheduleService workScheduleService) {
        this.workScheduleService = workScheduleService;
    }

    @PostMapping("/create/work-schedule")
    public ResponseEntity<MetadataResponse> createWorkSchedule(@RequestBody WorkScheduleDTO workScheduleDTO) throws Exception {
        workScheduleDTO = workScheduleService.createWorkSchedule(workScheduleDTO);
        if(workScheduleDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create work schedule"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new MetadataResponse(workScheduleDTO.getWsId(), "Successful to create work schedule"));
    }

    @GetMapping("/getListByEmployeeAndDateRange/work-schedule")
    public ResponseEntity<List<WorkScheduleDTO>> getAllWorkSchedule(@RequestParam("employeeId") String employeeId, @RequestParam("monthStart") @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime monthStart, @RequestParam("monthEnd") @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime monthEnd) throws Exception {
        List<WorkScheduleDTO> workScheduleDTOList = workScheduleService.getAllWorkSchedule(employeeId, monthStart, monthEnd);

        if (workScheduleDTOList == null || workScheduleDTOList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(workScheduleDTOList);
    }

    @PutMapping("/update/work-schedule/{wsId}")
    public ResponseEntity<MetadataResponse> updateWorkSchedule(@PathVariable("wsId") Long wsId, @RequestBody WorkScheduleDTO workScheduleDTO) throws Exception {
        workScheduleDTO = workScheduleService.updateWorkSchedule(wsId, workScheduleDTO);
        if(workScheduleDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update work schedule"));
        }

        return ResponseEntity.ok(new MetadataResponse(workScheduleDTO.getWsId(), "Successful to update work schedule"));
    }

    @DeleteMapping("/delete/work-schedule/{wsId}")
    public ResponseEntity<MetadataResponse> deleteWorkSchedule(@PathVariable("wsId") Long wsId) throws Exception {
        Boolean boolDel = workScheduleService.deleteWorkSchedule(wsId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete work schedule"));
        }

        return ResponseEntity.ok(new MetadataResponse(wsId, "Successful to delete work schedule"));
    }

}