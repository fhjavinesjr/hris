package com.administrative.controllers;

import com.administrative.dtos.JobPositionDTO;
import com.administrative.dtos.TimeShiftDTO;
import com.administrative.services.JobPositionService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class JobPositionController {

    private final JobPositionService jobPositionService;

    public JobPositionController(JobPositionService jobPositionService) {
        this.jobPositionService = jobPositionService;
    }

    @PostMapping("/job-position/create")
    public ResponseEntity<MetadataResponse> createTimeShift(@RequestBody JobPositionDTO jobPositionDTO) throws Exception {
        jobPositionDTO = jobPositionService.createJobPosition(jobPositionDTO);
        if(jobPositionDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update Job Position"));
        }

        return ResponseEntity.ok(new MetadataResponse(jobPositionDTO.getJobPositionId(), "Successful to update Job Position"));
    }

}
