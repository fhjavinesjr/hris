package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.VoluntaryWorkDTO;
import com.humanresource.services.VoluntaryWorkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class VoluntaryWorkController {

    private final VoluntaryWorkService voluntaryWorkService;

    public VoluntaryWorkController(VoluntaryWorkService voluntaryWorkService) {
        this.voluntaryWorkService = voluntaryWorkService;
    }

    @PostMapping("/create/voluntaryWork")
    public ResponseEntity<MetadataResponse> createVoluntaryWork(@RequestBody VoluntaryWorkDTO voluntaryWorkDTO) throws Exception {
        voluntaryWorkDTO = voluntaryWorkService.createVoluntaryWork(voluntaryWorkDTO);
        if(voluntaryWorkDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create voluntaryWork"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new MetadataResponse(voluntaryWorkDTO.getVoluntaryWorkId(), "Successful to create voluntary work"));
    }

    @GetMapping("/fetch/voluntaryWork/{voluntaryWorkId}")
    public ResponseEntity<VoluntaryWorkDTO> getVoluntaryWorkByVoluntaryWorkId(@PathVariable("voluntaryWorkId") Long voluntaryWorkId) throws Exception {
        VoluntaryWorkDTO dto = voluntaryWorkService.getVoluntaryWorkByVoluntaryWorkId(voluntaryWorkId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/fetch/voluntaryWork/by/{personalDataId}")
    public ResponseEntity<List<VoluntaryWorkDTO>> getVoluntaryWorkByPersonalDataId(@PathVariable("personalDataId") Long personalDataId) throws Exception {
        List<VoluntaryWorkDTO> dtoList = voluntaryWorkService.getVoluntaryWorkByPersonalDataId(personalDataId);
        return ResponseEntity.ok(dtoList);
    }

    @PutMapping("/voluntaryWork/update/{voluntaryWorkId}")
    public Boolean updateVoluntaryWork(@PathVariable("voluntaryWorkId") Long voluntaryWorkId, @Valid @RequestBody Map<String, Object> updates) throws Exception {
        return voluntaryWorkService.updateVoluntaryWork(voluntaryWorkId, updates);
    }

    @DeleteMapping("/voluntaryWork/delete/{voluntaryWorkId}")
    public Boolean deleteVoluntaryWork(@PathVariable("voluntaryWorkId") Long voluntaryWorkId) throws Exception {
        return voluntaryWorkService.deleteVoluntaryWork(voluntaryWorkId);
    }

}