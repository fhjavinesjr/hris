package com.administrative.controllers;

import com.administrative.dtos.BusinessUnitsDTO;
import com.administrative.services.BusinessUnitsService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BusinessUnitsController {

    private final BusinessUnitsService businessUnitsService;

    public BusinessUnitsController(BusinessUnitsService businessUnitsService) {
        this.businessUnitsService = businessUnitsService;
    }

    @PostMapping("/businessUnits/create")
    public ResponseEntity<MetadataResponse> createBusinessUnits(@RequestBody BusinessUnitsDTO businessUnitsDTO) throws Exception {
        businessUnitsDTO = businessUnitsService.createBusinessUnits(businessUnitsDTO);
        if(businessUnitsDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create BusinessUnits"));
        }

        return ResponseEntity.ok(new MetadataResponse(businessUnitsDTO.getBusinessUnitsId(), "Successful to create BusinessUnits"));
    }

    @GetMapping("/businessUnits/get-all")
    public ResponseEntity<List<BusinessUnitsDTO>> getAllBusinessUnits() throws Exception {
        List<BusinessUnitsDTO> businessUnitsDTOList = businessUnitsService.getAllBusinessUnits();
        return ResponseEntity.ok(businessUnitsDTOList);
    }

    @PutMapping("/businessUnits/update/{businessUnitsId}")
    public ResponseEntity<MetadataResponse> updateBusinessUnits(@PathVariable("businessUnitsId") Long businessUnitsId, @RequestBody BusinessUnitsDTO businessUnitsDTO) throws Exception {
        businessUnitsDTO = businessUnitsService.updateBusinessUnits(businessUnitsId, businessUnitsDTO);
        if(businessUnitsDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update businessUnits"));
        }

        return ResponseEntity.ok(new MetadataResponse(businessUnitsDTO.getBusinessUnitsId(), "Successful to update businessUnits"));
    }

    @DeleteMapping("/businessUnits/delete/{businessUnitsId}")
    public ResponseEntity<MetadataResponse> deleteBusinessUnits(@PathVariable("businessUnitsId") Long businessUnitsId) throws Exception {
        Boolean boolDel = businessUnitsService.deleteBusinessUnits(businessUnitsId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete businessUnits"));
        }

        return ResponseEntity.ok(new MetadataResponse(businessUnitsId, "Successful to businessUnits"));
    }

}