package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.WorkExperienceDTO;
import com.humanresource.services.WorkExperienceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class WorkExperienceController {

    private final WorkExperienceService workExperienceService;

    public WorkExperienceController(WorkExperienceService workExperienceService) {
        this.workExperienceService = workExperienceService;
    }

    @PostMapping("/create/workExperience")
    public ResponseEntity<MetadataResponse> createWorkExperience(@RequestBody WorkExperienceDTO workExperienceDTO) throws Exception {
        workExperienceDTO = workExperienceService.createWorkExperience(workExperienceDTO);
        if(workExperienceDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create workExperience"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new MetadataResponse(workExperienceDTO.getWorkExperienceId(), "Successful to create work experience"));
    }

    @GetMapping("/fetch/workExperience/{workExperienceId}")
    public ResponseEntity<WorkExperienceDTO> getWorkExperienceByWorkExperienceId(@PathVariable("workExperienceId") Long workExperienceId) throws Exception {
        WorkExperienceDTO dto = workExperienceService.getWorkExperienceByWorkExperienceId(workExperienceId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/fetch/workExperience/by/{personalDataId}")
    public ResponseEntity<List<WorkExperienceDTO>> getWorkExperienceByPersonalDataId(@PathVariable("personalDataId") Long personalDataId) throws Exception {
        List<WorkExperienceDTO> dtoList = workExperienceService.getWorkExperienceByPersonalDataId(personalDataId);
        return ResponseEntity.ok(dtoList);
    }

    @PutMapping("/workExperience/update/{workExperienceId}")
    public Boolean updateWorkExperience(@PathVariable("workExperienceId") Long workExperienceId, @Valid @RequestBody Map<String, Object> updates) throws Exception {
        return workExperienceService.updateWorkExperience(workExperienceId, updates);
    }

    @DeleteMapping("/workExperience/delete/{workExperienceId}")
    public Boolean deleteWorkExperience(@PathVariable("workExperienceId") Long workExperienceId) throws Exception {
        return workExperienceService.deleteWorkExperience(workExperienceId);
    }

}