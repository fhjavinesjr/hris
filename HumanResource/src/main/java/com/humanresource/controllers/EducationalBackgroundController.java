package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.EducationalBackgroundDTO;
import com.humanresource.services.EducationalBackgroundService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EducationalBackgroundController {

    private final EducationalBackgroundService educationalBackgroundService;

    public EducationalBackgroundController(EducationalBackgroundService educationalBackgroundService) {
        this.educationalBackgroundService = educationalBackgroundService;
    }

    @PostMapping("/create/educationalBackground")
    public ResponseEntity<MetadataResponse> createEducationalBackground(@RequestBody EducationalBackgroundDTO educationalBackgroundDTO) throws Exception {
        educationalBackgroundDTO = educationalBackgroundService.createEducationalBackground(educationalBackgroundDTO);
        if(educationalBackgroundDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create educationalBackground"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new MetadataResponse(educationalBackgroundDTO.getEducationalBackgroundId(), "Successful to create educationalBackground"));
    }

    @GetMapping("/fetch/educationalBackground/{educationalBackgroundId}")
    public ResponseEntity<EducationalBackgroundDTO> getEducationalBackgroundByEducationalBackgroundId(@PathVariable("educationalBackgroundId") Long educationalBackgroundId) throws Exception {
        EducationalBackgroundDTO dto = educationalBackgroundService.getEducationalBackgroundByEducationalBackgroundId(educationalBackgroundId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/fetch/educationalBackground/by/{personalDataId}")
    public ResponseEntity<List<EducationalBackgroundDTO>> getEducationalBackgroundByPersonalDataId(@PathVariable("personalDataId") Long personalDataId) throws Exception {
        List<EducationalBackgroundDTO> dtoList = educationalBackgroundService.getEducationalBackgroundByPersonalDataId(personalDataId);
        return ResponseEntity.ok(dtoList);
    }

    @PutMapping("/educationalBackground/update/{educationalBackgroundId}")
    public Boolean updateEducationalBackground(@PathVariable("educationalBackgroundId") Long educationalBackgroundId, @Valid @RequestBody Map<String, Object> updates) throws Exception {
        return educationalBackgroundService.updateEducationalBackground(educationalBackgroundId, updates);
    }

    @DeleteMapping("/educationalBackground/delete/{educationalBackgroundId}")
    public Boolean deleteEducationalBackground(@PathVariable("educationalBackgroundId") Long educationalBackgroundId) throws Exception {
        return educationalBackgroundService.deleteEducationalBackground(educationalBackgroundId);
    }

}
