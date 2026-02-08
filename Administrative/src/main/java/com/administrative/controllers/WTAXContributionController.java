package com.administrative.controllers;

import com.administrative.dtos.WTAXContributionDTO;
import com.administrative.services.WTAXContributionService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class WTAXContributionController {

    private final WTAXContributionService wTAXContributionService;

    public WTAXContributionController(WTAXContributionService wTAXContributionService) {
        this.wTAXContributionService = wTAXContributionService;
    }

    @PostMapping("/wTAXContribution/create")
    public ResponseEntity<MetadataResponse> createWTAXContribution(@RequestBody WTAXContributionDTO wTAXContributionDTO) throws Exception {
        wTAXContributionDTO = wTAXContributionService.createWTAXContribution(wTAXContributionDTO);
        if(wTAXContributionDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update WTAXContribution"));
        }

        return ResponseEntity.ok(new MetadataResponse(wTAXContributionDTO.getwTaxContributionId(), "Successful to update WTAXContribution"));
    }

    @GetMapping("/wTAXContribution/get-all")
    public ResponseEntity<List<WTAXContributionDTO>> getAllWTAXContribution() throws Exception {
        List<WTAXContributionDTO> wTAXContributionDTOList = wTAXContributionService.getAllWTAXContribution();
        return ResponseEntity.ok(wTAXContributionDTOList);
    }

    @PutMapping("/wTAXContribution/update/{wTAXContributionId}")
    public ResponseEntity<MetadataResponse> updateWTAXContribution(@PathVariable("wTAXContributionId") Long wTAXContributionId, @RequestBody WTAXContributionDTO wTAXContributionDTO) throws Exception {
        wTAXContributionDTO = wTAXContributionService.updateWTAXContribution(wTAXContributionId, wTAXContributionDTO);
        if(wTAXContributionDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update wTAXContribution"));
        }

        return ResponseEntity.ok(new MetadataResponse(wTAXContributionDTO.getwTaxContributionId(), "Successful to update wTAXContribution"));
    }

    @DeleteMapping("/wTAXContribution/delete/{wTAXContributionId}")
    public ResponseEntity<MetadataResponse> deleteWTAXContribution(@PathVariable("wTAXContributionId") Long wTAXContributionId) throws Exception {
        Boolean boolDel = wTAXContributionService.deleteWTAXContribution(wTAXContributionId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete wTAXContribution"));
        }

        return ResponseEntity.ok(new MetadataResponse(wTAXContributionId, "Successful to wTAXContribution"));
    }

}