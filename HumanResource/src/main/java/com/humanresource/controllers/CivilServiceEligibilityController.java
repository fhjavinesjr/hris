package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.CivilServiceEligibilityDTO;
import com.humanresource.services.CivilServiceEligibilityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CivilServiceEligibilityController {

    private final CivilServiceEligibilityService civilServiceEligibilityService;

    public CivilServiceEligibilityController(CivilServiceEligibilityService civilServiceEligibilityService) {
        this.civilServiceEligibilityService = civilServiceEligibilityService;
    }

    @PostMapping("/create/civilServiceEligibility")
    public ResponseEntity<MetadataResponse> createCivilServiceEligibility(@RequestBody CivilServiceEligibilityDTO civilServiceEligibilityDTO) throws Exception {
        civilServiceEligibilityDTO = civilServiceEligibilityService.createCivilServiceEligibility(civilServiceEligibilityDTO);
        if(civilServiceEligibilityDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create civilServiceEligibility"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new MetadataResponse(civilServiceEligibilityDTO.getCivilServiceEligibilityId(), "Successful to create civil service eligibility"));
    }

    @GetMapping("/fetch/civilServiceEligibility/{civilServiceEligibilityId}")
    public ResponseEntity<CivilServiceEligibilityDTO> getCivilServiceEligibilityByCivilServiceEligibilityId(@PathVariable("civilServiceEligibilityId") Long civilServiceEligibilityId) throws Exception {
        CivilServiceEligibilityDTO dto = civilServiceEligibilityService.getCivilServiceEligibilityByCivilServiceEligibilityId(civilServiceEligibilityId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/fetch/civilServiceEligibility/by/{personalDataId}")
    public ResponseEntity<List<CivilServiceEligibilityDTO>> getCivilServiceEligibilityByPersonalDataId(@PathVariable("personalDataId") Long personalDataId) throws Exception {
        List<CivilServiceEligibilityDTO> dtoList = civilServiceEligibilityService.getCivilServiceEligibilityByPersonalDataId(personalDataId);
        return ResponseEntity.ok(dtoList);
    }

    @PutMapping("/civilServiceEligibility/update/{civilServiceEligibilityId}")
    public Boolean updateCivilServiceEligibility(@PathVariable("civilServiceEligibilityId") Long civilServiceEligibilityId, @Valid @RequestBody Map<String, Object> updates) throws Exception {
        return civilServiceEligibilityService.updateCivilServiceEligibility(civilServiceEligibilityId, updates);
    }

    @DeleteMapping("/civilServiceEligibility/delete/{civilServiceEligibilityId}")
    public Boolean deleteCivilServiceEligibility(@PathVariable("civilServiceEligibilityId") Long civilServiceEligibilityId) throws Exception {
        return civilServiceEligibilityService.deleteCivilServiceEligibility(civilServiceEligibilityId);
    }

}