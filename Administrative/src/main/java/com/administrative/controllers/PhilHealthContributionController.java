package com.administrative.controllers;

import com.administrative.dtos.PhilHealthContributionDTO;
import com.administrative.services.PhilHealthContributionService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PhilHealthContributionController {

    private final PhilHealthContributionService philHealthContributionService;

    public PhilHealthContributionController(PhilHealthContributionService philHealthContributionService) {
        this.philHealthContributionService = philHealthContributionService;
    }

    @PostMapping("/philHealthContribution/create")
    public ResponseEntity<MetadataResponse> createPhilHealthContribution(@RequestBody PhilHealthContributionDTO philHealthContributionDTO) throws Exception {
        philHealthContributionDTO = philHealthContributionService.createPhilHealthContribution(philHealthContributionDTO);
        if(philHealthContributionDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create PhilHealthContribution"));
        }

        return ResponseEntity.ok(new MetadataResponse(philHealthContributionDTO.getPhilhealthContributionId(), "Successful to create PhilHealthContribution"));
    }

    @GetMapping("/philHealthContribution/get-all")
    public ResponseEntity<List<PhilHealthContributionDTO>> getAllPhilHealthContribution() throws Exception {
        List<PhilHealthContributionDTO> philHealthContributionDTOList = philHealthContributionService.getAllPhilHealthContribution();
        return ResponseEntity.ok(philHealthContributionDTOList);
    }

    @PutMapping("/philHealthContribution/update/{philHealthContributionId}")
    public ResponseEntity<MetadataResponse> updatePhilHealthContribution(@PathVariable("philHealthContributionId") Long philHealthContributionId, @RequestBody PhilHealthContributionDTO philHealthContributionDTO) throws Exception {
        philHealthContributionDTO = philHealthContributionService.updatePhilHealthContribution(philHealthContributionId, philHealthContributionDTO);
        if(philHealthContributionDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update philHealthContribution"));
        }

        return ResponseEntity.ok(new MetadataResponse(philHealthContributionDTO.getPhilhealthContributionId(), "Successful to update philHealthContribution"));
    }

    @DeleteMapping("/philHealthContribution/delete/{philHealthContributionId}")
    public ResponseEntity<MetadataResponse> deletePhilHealthContribution(@PathVariable("philHealthContributionId") Long philHealthContributionId) throws Exception {
        Boolean boolDel = philHealthContributionService.deletePhilHealthContribution(philHealthContributionId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete philHealthContribution"));
        }

        return ResponseEntity.ok(new MetadataResponse(philHealthContributionId, "Successful to philHealthContribution"));
    }

    /**
     * Returns PhilHealth contribution brackets for payroll computation.
     * Converts database String fields to the format expected by PayrollBatchService.
     * 
     * @return List of brackets with salaryFrom/To, rate, and fixed PS/ES amounts
     */
    @GetMapping("/philhealth/brackets")
    public ResponseEntity<List<Map<String, Object>>> getPhilHealthBrackets() throws Exception {
        List<PhilHealthContributionDTO> all = philHealthContributionService.getAllPhilHealthContribution();
        List<Map<String, Object>> brackets = new java.util.ArrayList<>();
        
        for (PhilHealthContributionDTO dto : all) {
            Map<String, Object> bracket = new LinkedHashMap<>();
            
            // Parse salary range
            Double salaryFrom = null;
            Double salaryTo = null;
            try {
                if (dto.getMonthlySalaryRangeFrom() != null && !dto.getMonthlySalaryRangeFrom().isBlank()) {
                    salaryFrom = Double.parseDouble(dto.getMonthlySalaryRangeFrom().replace(",", ""));
                }
                if (dto.getMonthlySalaryRangeTo() != null && !dto.getMonthlySalaryRangeTo().isBlank()) {
                    salaryTo = Double.parseDouble(dto.getMonthlySalaryRangeTo().replace(",", ""));
                }
            } catch (Exception ignored) {}
            
            bracket.put("salaryFrom", salaryFrom);
            bracket.put("salaryTo", salaryTo);
            bracket.put("isAndUp", salaryTo == null && salaryFrom != null);
            
            // Parse rate percentage
            Double rate = 0.0;
            try {
                if (dto.getRatePercentage() != null && !dto.getRatePercentage().isBlank()) {
                    rate = Double.parseDouble(dto.getRatePercentage().replace("%", "").trim());
                    // Convert percentage to decimal (e.g., 5 -> 0.05)
                    if (rate > 1.0) rate = rate / 100.0;
                }
            } catch (Exception ignored) {}
            bracket.put("rate", rate);
            
            // Parse fixed personal share and employer share (for cap brackets)
            Double psFixed = null;
            Double esFixed = null;
            try {
                // Use personalShareFrom or personalShareTo as fixed amount for cap brackets
                String psStr = dto.getPersonalShareTo() != null && !dto.getPersonalShareTo().isBlank() 
                    ? dto.getPersonalShareTo() : dto.getPersonalShareFrom();
                if (psStr != null && !psStr.isBlank()) {
                    psFixed = Double.parseDouble(psStr.replace(",", ""));
                }
                
                String esStr = dto.getEmployerShareTo() != null && !dto.getEmployerShareTo().isBlank() 
                    ? dto.getEmployerShareTo() : dto.getEmployerShareFrom();
                if (esStr != null && !esStr.isBlank()) {
                    esFixed = Double.parseDouble(esStr.replace(",", ""));
                }
            } catch (Exception ignored) {}
            bracket.put("psFixed", psFixed);
            bracket.put("esFixed", esFixed);
            
            brackets.add(bracket);
        }
        
        return ResponseEntity.ok(brackets);
    }

}