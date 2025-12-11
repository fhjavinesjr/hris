package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.SeparationDTO;
import com.humanresource.services.SeparationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SeparationController {

    private final SeparationService separationService;

    public SeparationController(SeparationService separationService) {
        this.separationService = separationService;
    }

    @PostMapping("/separation/create")
    public ResponseEntity<MetadataResponse> createSeparation(@RequestBody SeparationDTO separationDTO) throws Exception {
        separationDTO = separationService.createSeparation(separationDTO);
        if(separationDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update Separation"));
        }

        return ResponseEntity.ok(new MetadataResponse(separationDTO.getSeparationId(), "Successful to create Separation"));
    }

    @GetMapping("/separation/get-all")
    public ResponseEntity<List<SeparationDTO>> getAllSeparation() throws Exception {
        List<SeparationDTO> separationDTOList = separationService.getAllSeparation();
        return ResponseEntity.ok(separationDTOList);
    }

    @GetMapping("/separation/get-all/{employeeId}")
    public ResponseEntity<List<SeparationDTO>> getAllSeparationByEmployeeId(@PathVariable Long employeeId) throws Exception {
        List<SeparationDTO> separationDTOList = separationService.getAllSeparationByEmployeeId(employeeId);
        return ResponseEntity.ok(separationDTOList);
    }

    @PutMapping("/separation/update/{separationId}")
    public ResponseEntity<MetadataResponse> updateSeparation(@PathVariable("separationId") Long separationId, @RequestBody SeparationDTO separationDTO) throws Exception {
        separationDTO = separationService.updateSeparation(separationId, separationDTO);
        if(separationDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update separation"));
        }

        return ResponseEntity.ok(new MetadataResponse(separationDTO.getSeparationId(), "Successful to update separation"));
    }

    @DeleteMapping("/separation/delete/{separationId}")
    public ResponseEntity<MetadataResponse> deleteSeparation(@PathVariable("separationId") Long separationId) throws Exception {
        Boolean boolDel = separationService.deleteSeparation(separationId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete separation"));
        }

        return ResponseEntity.ok(new MetadataResponse(separationId, "Successful to separation"));
    }

    @GetMapping("/separation/getLatestSeparationByEmployeeId/{employeeId}")
    public ResponseEntity<SeparationDTO> getLatestSeparationByEmployeeId(@PathVariable Long employeeId) throws Exception {
        SeparationDTO separationDTO = separationService.getLatestSeparationByEmployeeId(employeeId);
        return ResponseEntity.ok(separationDTO);
    }
    
}
