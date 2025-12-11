package com.administrative.controllers;

import com.administrative.dtos.NatureOfSeparationDTO;
import com.administrative.services.NatureOfSeparationService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NatureOfSeparationController {

    private final NatureOfSeparationService natureOfSeparationService;

    public NatureOfSeparationController(NatureOfSeparationService natureOfSeparationService) {
        this.natureOfSeparationService = natureOfSeparationService;
    }

    @PostMapping("/natureOfSeparation/create")
    public ResponseEntity<MetadataResponse> createNatureOfSeparation(@RequestBody NatureOfSeparationDTO natureOfSeparationDTO) throws Exception {
        natureOfSeparationDTO = natureOfSeparationService.createNatureOfSeparation(natureOfSeparationDTO);
        if(natureOfSeparationDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update NatureOfSeparation"));
        }

        return ResponseEntity.ok(new MetadataResponse(natureOfSeparationDTO.getNatureOfSeparationId(), "Successful to update NatureOfSeparation"));
    }

    @GetMapping("/natureOfSeparation/get-all")
    public ResponseEntity<List<NatureOfSeparationDTO>> getAllNatureOfSeparation() throws Exception {
        List<NatureOfSeparationDTO> natureOfSeparationDTOList = natureOfSeparationService.getAllNatureOfSeparation();
        return ResponseEntity.ok(natureOfSeparationDTOList);
    }

    @PutMapping("/natureOfSeparation/update/{natureOfSeparationId}")
    public ResponseEntity<MetadataResponse> updateNatureOfSeparation(@PathVariable("natureOfSeparationId") Long natureOfSeparationId, @RequestBody NatureOfSeparationDTO natureOfSeparationDTO) throws Exception {
        natureOfSeparationDTO = natureOfSeparationService.updateNatureOfSeparation(natureOfSeparationId, natureOfSeparationDTO);
        if(natureOfSeparationDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update natureOfSeparation"));
        }

        return ResponseEntity.ok(new MetadataResponse(natureOfSeparationDTO.getNatureOfSeparationId(), "Successful to update natureOfSeparation"));
    }

    @DeleteMapping("/natureOfSeparation/delete/{natureOfSeparationId}")
    public ResponseEntity<MetadataResponse> deleteNatureOfSeparation(@PathVariable("natureOfSeparationId") Long natureOfSeparationId) throws Exception {
        Boolean boolDel = natureOfSeparationService.deleteNatureOfSeparation(natureOfSeparationId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete natureOfSeparation"));
        }

        return ResponseEntity.ok(new MetadataResponse(natureOfSeparationId, "Successful to natureOfSeparation"));
    }

}