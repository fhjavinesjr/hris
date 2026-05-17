package com.administrative.controllers;

import com.administrative.dtos.EarningTypeDTO;
import com.administrative.services.EarningTypeService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EarningTypeController {

    private final EarningTypeService earningTypeService;

    public EarningTypeController(EarningTypeService earningTypeService) {
        this.earningTypeService = earningTypeService;
    }

    @PostMapping("/earningType/create")
    public ResponseEntity<MetadataResponse> createEarningType(@RequestBody EarningTypeDTO earningTypeDTO) throws Exception {
        earningTypeDTO = earningTypeService.createEarningType(earningTypeDTO);
        if (earningTypeDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create EarningType"));
        }
        return ResponseEntity.ok(new MetadataResponse(earningTypeDTO.getEarningTypeId(), "Successfully created EarningType"));
    }

    @GetMapping("/earningType/get-all")
    public ResponseEntity<List<EarningTypeDTO>> getAllEarningTypes() throws Exception {
        return ResponseEntity.ok(earningTypeService.getAllEarningTypes());
    }

    @PutMapping("/earningType/update/{earningTypeId}")
    public ResponseEntity<MetadataResponse> updateEarningType(
            @PathVariable("earningTypeId") Long earningTypeId,
            @RequestBody EarningTypeDTO earningTypeDTO) throws Exception {
        earningTypeDTO = earningTypeService.updateEarningType(earningTypeId, earningTypeDTO);
        if (earningTypeDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update EarningType"));
        }
        return ResponseEntity.ok(new MetadataResponse(earningTypeId, "Successfully updated EarningType"));
    }

    @DeleteMapping("/earningType/delete/{earningTypeId}")
    public ResponseEntity<MetadataResponse> deleteEarningType(@PathVariable("earningTypeId") Long earningTypeId) throws Exception {
        Boolean deleted = earningTypeService.deleteEarningType(earningTypeId);
        if (!deleted) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete EarningType"));
        }
        return ResponseEntity.ok(new MetadataResponse(earningTypeId, "Successfully deleted EarningType"));
    }
}
