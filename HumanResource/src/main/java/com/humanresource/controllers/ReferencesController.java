package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.ReferencesDTO;
import com.humanresource.services.ReferencesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReferencesController {

    private final ReferencesService referencesService;

    public ReferencesController(ReferencesService referencesService) {
        this.referencesService = referencesService;
    }

    @PostMapping("/create/references")
    public ResponseEntity<MetadataResponse> createReferences(@RequestBody ReferencesDTO referencesDTO) throws Exception {
        referencesDTO = referencesService.createReferences(referencesDTO);
        if(referencesDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create references"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new MetadataResponse(referencesDTO.getReferencesId(), "Successful to create references"));
    }

    @GetMapping("/fetch/references/{referencesId}")
    public ResponseEntity<ReferencesDTO> getReferencesByReferencesId(@PathVariable("referencesId") Long referencesId) throws Exception {
        ReferencesDTO dto = referencesService.getReferencesByReferencesId(referencesId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/fetch/references/by/{personalDataId}")
    public ResponseEntity<List<ReferencesDTO>> getReferencesByPersonalDataId(@PathVariable("personalDataId") Long personalDataId) throws Exception {
        List<ReferencesDTO> dtoList = referencesService.getReferencesByPersonalDataId(personalDataId);
        return ResponseEntity.ok(dtoList);
    }

    @PutMapping("/references/update/{referencesId}")
    public Boolean updateReferences(@PathVariable("referencesId") Long referencesId, @Valid @RequestBody Map<String, Object> updates) throws Exception {
        return referencesService.updateReferences(referencesId, updates);
    }

    @DeleteMapping("/references/delete/{referencesId}")
    public Boolean deleteReferences(@PathVariable("referencesId") Long referencesId) throws Exception {
        return referencesService.deleteReferences(referencesId);
    }
}