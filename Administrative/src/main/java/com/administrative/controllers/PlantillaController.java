package com.administrative.controllers;

import com.administrative.dtos.PlantillaDTO;
import com.administrative.services.PlantillaService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PlantillaController {

    private final PlantillaService plantillaService;

    public PlantillaController(PlantillaService plantillaService) {
        this.plantillaService = plantillaService;
    }

    @PostMapping("/plantilla/create")
    public ResponseEntity<MetadataResponse> createPlantilla(@RequestBody PlantillaDTO plantillaDTO) throws Exception {
        plantillaDTO = plantillaService.createPlantilla(plantillaDTO);
        if(plantillaDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update Plantilla"));
        }

        return ResponseEntity.ok(new MetadataResponse(plantillaDTO.getPlantillaId(), "Successful to update Plantilla"));
    }

    @GetMapping("/plantilla/get-all")
    public ResponseEntity<List<PlantillaDTO>> getAllPlantilla() throws Exception {
        List<PlantillaDTO> plantillaDTOList = plantillaService.getAllPlantilla();
        return ResponseEntity.ok(plantillaDTOList);
    }

    @PutMapping("/plantilla/update/{plantillaId}")
    public ResponseEntity<MetadataResponse> updatePlantilla(@PathVariable("plantillaId") Long plantillaId, @RequestBody PlantillaDTO plantillaDTO) throws Exception {
        plantillaDTO = plantillaService.updatePlantilla(plantillaId, plantillaDTO);
        if(plantillaDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update plantilla"));
        }

        return ResponseEntity.ok(new MetadataResponse(plantillaDTO.getPlantillaId(), "Successful to update plantilla"));
    }

    @DeleteMapping("/plantilla/delete/{plantillaId}")
    public ResponseEntity<MetadataResponse> deletePlantilla(@PathVariable("plantillaId") Long plantillaId) throws Exception {
        Boolean boolDel = plantillaService.deletePlantilla(plantillaId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete plantilla"));
        }

        return ResponseEntity.ok(new MetadataResponse(plantillaId, "Successful to plantilla"));
    }

    @GetMapping("/plantilla/by-job-position/{jobPositionId}")
    public ResponseEntity<List<PlantillaDTO>> getByJobPosition(@PathVariable Long jobPositionId) throws Exception {

        List<PlantillaDTO> list = plantillaService.getByJobPositionId(jobPositionId);
        return ResponseEntity.ok(list);
    }

}