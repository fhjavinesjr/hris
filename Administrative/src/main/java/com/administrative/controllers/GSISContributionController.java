package com.administrative.controllers;

import com.administrative.dtos.GSISContributionDTO;
import com.administrative.services.GSISContributionService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GSISContributionController {

    private final GSISContributionService gsisContributionService;

    public GSISContributionController(GSISContributionService gsisContributionService) {
        this.gsisContributionService = gsisContributionService;
    }

    @PostMapping("/gsisContribution/create")
    public ResponseEntity<MetadataResponse> createGSISContribution(@RequestBody GSISContributionDTO gsisContributionDTO) throws Exception {
        gsisContributionDTO = gsisContributionService.createGSISContribution(gsisContributionDTO);
        if(gsisContributionDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update GSISContribution"));
        }

        return ResponseEntity.ok(new MetadataResponse(gsisContributionDTO.getGsisContributionId(), "Successful to update GSISContribution"));
    }

    @GetMapping("/gsisContribution/get-all")
    public ResponseEntity<List<GSISContributionDTO>> getAllGSISContribution() throws Exception {
        List<GSISContributionDTO> gsisContributionDTOList = gsisContributionService.getAllGSISContribution();
        return ResponseEntity.ok(gsisContributionDTOList);
    }

    @PutMapping("/gsisContribution/update/{gsisContributionId}")
    public ResponseEntity<MetadataResponse> updateGSISContribution(@PathVariable("gsisContributionId") Long gsisContributionId, @RequestBody GSISContributionDTO gsisContributionDTO) throws Exception {
        gsisContributionDTO = gsisContributionService.updateGSISContribution(gsisContributionId, gsisContributionDTO);
        if(gsisContributionDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update gsisContribution"));
        }

        return ResponseEntity.ok(new MetadataResponse(gsisContributionDTO.getGsisContributionId(), "Successful to update gsisContribution"));
    }

    @DeleteMapping("/gsisContribution/delete/{gsisContributionId}")
    public ResponseEntity<MetadataResponse> deleteGSISContribution(@PathVariable("gsisContributionId") Long gsisContributionId) throws Exception {
        Boolean boolDel = gsisContributionService.deleteGSISContribution(gsisContributionId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete gsisContribution"));
        }

        return ResponseEntity.ok(new MetadataResponse(gsisContributionId, "Successful to gsisContribution"));
    }

}