package com.administrative.controllers;

import com.administrative.dtos.JobPositionDTO;
import com.administrative.services.JobPositionService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class JobPositionController {

    private final JobPositionService jobPositionService;

    public JobPositionController(JobPositionService jobPositionService) {
        this.jobPositionService = jobPositionService;
    }

    @PostMapping("/job-position/create")
    public ResponseEntity<MetadataResponse> createJobPosition(@RequestBody JobPositionDTO jobPositionDTO) throws Exception {
        jobPositionDTO = jobPositionService.createJobPosition(jobPositionDTO);
        if(jobPositionDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update Job Position"));
        }

        return ResponseEntity.ok(new MetadataResponse(jobPositionDTO.getJobPositionId(), "Successful to update Job Position"));
    }

    @GetMapping("/job-position/get-all")
    public ResponseEntity<List<JobPositionDTO>> getAllJobPosition() throws Exception {
        List<JobPositionDTO> jobPositionDTOList = jobPositionService.getAllJobPosition();
        return ResponseEntity.ok(jobPositionDTOList);
    }

    @PutMapping("/job-position/update/{jobPositionId}")
    public ResponseEntity<MetadataResponse> updateJobPosition(@PathVariable("jobPositionId") Long jobPositionId, @RequestBody JobPositionDTO jobPositionDTO) throws Exception {
        jobPositionDTO = jobPositionService.updateJobPosition(jobPositionId, jobPositionDTO);
        if(jobPositionDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update job position"));
        }

        return ResponseEntity.ok(new MetadataResponse(jobPositionDTO.getJobPositionId(), "Successful to update job position"));
    }

    @DeleteMapping("/job-position/delete/{jobPositionId}")
    public ResponseEntity<MetadataResponse> deleteJobPosition(@PathVariable("jobPositionId") Long jobPositionId) throws Exception {
        Boolean boolDel = jobPositionService.deleteJobPosition(jobPositionId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete job position"));
        }

        return ResponseEntity.ok(new MetadataResponse(jobPositionId, "Successful to job position"));
    }

}
