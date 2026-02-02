package com.administrative.controllers;

import com.administrative.dtos.PhilHealthContributionDTO;
import com.administrative.services.PhilHealthContributionService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                    .body(new MetadataResponse("Failed to update PhilHealthContribution"));
        }

        return ResponseEntity.ok(new MetadataResponse(philHealthContributionDTO.getPhilhealthContributionId(), "Successful to update PhilHealthContribution"));
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

}