package com.administrative.controllers;

import com.administrative.dtos.AreasDTO;
import com.administrative.services.AreasService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AreasController {

    private final AreasService areasService;

    public AreasController(AreasService areasService) {
        this.areasService = areasService;
    }

    @PostMapping("/areas/create")
    public ResponseEntity<MetadataResponse> createAreas(@RequestBody AreasDTO areasDTO) throws Exception {
        areasDTO = areasService.createAreas(areasDTO);
        if(areasDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create Areas"));
        }

        return ResponseEntity.ok(new MetadataResponse(areasDTO.getAreasId(), "Successful to create Areas"));
    }

    @GetMapping("/areas/get-all")
    public ResponseEntity<List<AreasDTO>> getAllAreas() throws Exception {
        List<AreasDTO> areasDTOList = areasService.getAllAreas();
        return ResponseEntity.ok(areasDTOList);
    }

    @PutMapping("/areas/update/{areasId}")
    public ResponseEntity<MetadataResponse> updateAreas(@PathVariable("areasId") Long areasId, @RequestBody AreasDTO areasDTO) throws Exception {
        areasDTO = areasService.updateAreas(areasId, areasDTO);
        if(areasDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update areas"));
        }

        return ResponseEntity.ok(new MetadataResponse(areasDTO.getAreasId(), "Successful to update areas"));
    }

    @DeleteMapping("/areas/delete/{areasId}")
    public ResponseEntity<MetadataResponse> deleteAreas(@PathVariable("areasId") Long areasId) throws Exception {
        Boolean boolDel = areasService.deleteAreas(areasId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete areas"));
        }

        return ResponseEntity.ok(new MetadataResponse(areasId, "Successful to areas"));
    }

}