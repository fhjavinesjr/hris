package com.administrative.controllers;

import com.administrative.dtos.CivilStatusDTO;
import com.administrative.services.CivilStatusService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CivilStatusController {

    private final CivilStatusService civilStatusService;

    public CivilStatusController(CivilStatusService civilStatusService) {
        this.civilStatusService = civilStatusService;
    }

    @PostMapping("/civilStatus/create")
    public ResponseEntity<MetadataResponse> createCivilStatus(@RequestBody CivilStatusDTO civilStatusDTO) throws Exception {
        civilStatusDTO = civilStatusService.createCivilStatus(civilStatusDTO);
        if(civilStatusDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update CivilStatus"));
        }

        return ResponseEntity.ok(new MetadataResponse(civilStatusDTO.getCivilStatusId(), "Successful to update CivilStatus"));
    }

    @GetMapping("/civilStatus/get-all")
    public ResponseEntity<List<CivilStatusDTO>> getAllCivilStatus() throws Exception {
        List<CivilStatusDTO> civilStatusDTOList = civilStatusService.getAllCivilStatus();
        return ResponseEntity.ok(civilStatusDTOList);
    }

    @PutMapping("/civilStatus/update/{civilStatusId}")
    public ResponseEntity<MetadataResponse> updateCivilStatus(@PathVariable("civilStatusId") Long civilStatusId, @RequestBody CivilStatusDTO civilStatusDTO) throws Exception {
        civilStatusDTO = civilStatusService.updateCivilStatus(civilStatusId, civilStatusDTO);
        if(civilStatusDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update civilStatus"));
        }

        return ResponseEntity.ok(new MetadataResponse(civilStatusDTO.getCivilStatusId(), "Successful to update civilStatus"));
    }

    @DeleteMapping("/civilStatus/delete/{civilStatusId}")
    public ResponseEntity<MetadataResponse> deleteCivilStatus(@PathVariable("civilStatusId") Long civilStatusId) throws Exception {
        Boolean boolDel = civilStatusService.deleteCivilStatus(civilStatusId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete civilStatus"));
        }

        return ResponseEntity.ok(new MetadataResponse(civilStatusId, "Successful to civilStatus"));
    }

}