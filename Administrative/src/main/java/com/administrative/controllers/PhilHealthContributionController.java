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
        Double greatestSalaryFrom = all.stream()
                .map(dto -> parseAmount(dto.getMonthlySalaryRangeFrom()))
                .filter(java.util.Objects::nonNull)
                .max(Double::compareTo)
                .orElse(null);
        
        for (PhilHealthContributionDTO dto : all) {
            Map<String, Object> bracket = new LinkedHashMap<>();
            
            // Parse salary range
            Double salaryFrom = parseAmount(dto.getMonthlySalaryRangeFrom());
            Double salaryTo = parseAmount(dto.getMonthlySalaryRangeTo());
            Double psTo = parseAmount(dto.getPersonalShareTo());
            Double esTo = parseAmount(dto.getEmployerShareTo());
            boolean fixedShare = (psTo == null || psTo == 0.0) && (esTo == null || esTo == 0.0);
            boolean isHighestFixedBracket = fixedShare && salaryFrom != null
                    && salaryFrom.equals(greatestSalaryFrom);
            
            bracket.put("salaryFrom", salaryFrom);
            bracket.put("salaryTo", salaryTo);
            // Historical rows use an artificial high salaryTo value. The highest fixed-share
            // bracket is the statutory income ceiling and must also cover every salary above it.
            bracket.put("isAndUp", salaryFrom != null && (salaryTo == null || isHighestFixedBracket));
            
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
            // psFixed = personalShareFrom (the floor or cap fixed amount for this bracket)
            // psTo    = personalShareTo  (>0 means ranged/rate-based bracket; 0 means fixed-amount bracket)
            try {
                if (dto.getPersonalShareFrom() != null && !dto.getPersonalShareFrom().isBlank()) {
                    psFixed = Double.parseDouble(dto.getPersonalShareFrom().replace(",", ""));
                }
                if (dto.getEmployerShareFrom() != null && !dto.getEmployerShareFrom().isBlank()) {
                    esFixed = Double.parseDouble(dto.getEmployerShareFrom().replace(",", ""));
                }
            } catch (Exception ignored) {}
            bracket.put("psFixed", psFixed);
            bracket.put("esFixed", esFixed);
            bracket.put("psTo", psTo);
            bracket.put("esTo", esTo);
            
            brackets.add(bracket);
        }
        
        return ResponseEntity.ok(brackets);
    }

    private static Double parseAmount(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.replace(",", ""));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

}
