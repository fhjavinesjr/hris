package com.administrative.controllers;

import com.administrative.dtos.DeductionTypeDTO;
import com.administrative.services.DeductionTypeService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DeductionTypeController {

    private final DeductionTypeService deductionTypeService;

    public DeductionTypeController(DeductionTypeService deductionTypeService) {
        this.deductionTypeService = deductionTypeService;
    }

    @PostMapping("/deductionType/create")
    public ResponseEntity<MetadataResponse> createDeductionType(@RequestBody DeductionTypeDTO deductionTypeDTO) throws Exception {
        deductionTypeDTO = deductionTypeService.createDeductionType(deductionTypeDTO);
        if (deductionTypeDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create DeductionType"));
        }
        return ResponseEntity.ok(new MetadataResponse(deductionTypeDTO.getDeductionTypeId(), "Successfully created DeductionType"));
    }

    @GetMapping("/deductionType/get-all")
    public ResponseEntity<List<DeductionTypeDTO>> getAllDeductionTypes() throws Exception {
        return ResponseEntity.ok(deductionTypeService.getAllDeductionTypes());
    }

    @PutMapping("/deductionType/update/{deductionTypeId}")
    public ResponseEntity<MetadataResponse> updateDeductionType(
            @PathVariable("deductionTypeId") Long deductionTypeId,
            @RequestBody DeductionTypeDTO deductionTypeDTO) throws Exception {
        deductionTypeDTO = deductionTypeService.updateDeductionType(deductionTypeId, deductionTypeDTO);
        if (deductionTypeDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update DeductionType"));
        }
        return ResponseEntity.ok(new MetadataResponse(deductionTypeId, "Successfully updated DeductionType"));
    }

    @DeleteMapping("/deductionType/delete/{deductionTypeId}")
    public ResponseEntity<MetadataResponse> deleteDeductionType(@PathVariable("deductionTypeId") Long deductionTypeId) throws Exception {
        Boolean deleted = deductionTypeService.deleteDeductionType(deductionTypeId);
        if (!deleted) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete DeductionType"));
        }
        return ResponseEntity.ok(new MetadataResponse(deductionTypeId, "Successfully deleted DeductionType"));
    }
}
