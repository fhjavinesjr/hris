package com.administrative.controllers;

import com.administrative.dtos.OfficialEngagementDTO;
import com.administrative.services.OfficialEngagementService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OfficialEngagementController {

    private final OfficialEngagementService officialEngagementService;

    public OfficialEngagementController(OfficialEngagementService officialEngagementService) {
        this.officialEngagementService = officialEngagementService;
    }

    @PostMapping("/officialEngagement/create")
    public ResponseEntity<MetadataResponse> createOfficialEngagement(@RequestBody OfficialEngagementDTO officialEngagementDTO) throws Exception {
        officialEngagementDTO = officialEngagementService.createOfficialEngagement(officialEngagementDTO);
        if(officialEngagementDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update OfficialEngagement"));
        }

        return ResponseEntity.ok(new MetadataResponse(officialEngagementDTO.getOfficialEngagementId(), "Successful to update OfficialEngagement"));
    }

    @GetMapping("/officialEngagement/get-all")
    public ResponseEntity<List<OfficialEngagementDTO>> getAllOfficialEngagement() throws Exception {
        List<OfficialEngagementDTO> officialEngagementDTOList = officialEngagementService.getAllOfficialEngagement();
        return ResponseEntity.ok(officialEngagementDTOList);
    }

    @PutMapping("/officialEngagement/update/{officialEngagementId}")
    public ResponseEntity<MetadataResponse> updateOfficialEngagement(@PathVariable("officialEngagementId") Long officialEngagementId, @RequestBody OfficialEngagementDTO officialEngagementDTO) throws Exception {
        officialEngagementDTO = officialEngagementService.updateOfficialEngagement(officialEngagementId, officialEngagementDTO);
        if(officialEngagementDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update officialEngagement"));
        }

        return ResponseEntity.ok(new MetadataResponse(officialEngagementDTO.getOfficialEngagementId(), "Successful to update officialEngagement"));
    }

    @DeleteMapping("/officialEngagement/delete/{officialEngagementId}")
    public ResponseEntity<MetadataResponse> deleteOfficialEngagement(@PathVariable("officialEngagementId") Long officialEngagementId) throws Exception {
        Boolean boolDel = officialEngagementService.deleteOfficialEngagement(officialEngagementId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete officialEngagement"));
        }

        return ResponseEntity.ok(new MetadataResponse(officialEngagementId, "Successful to officialEngagement"));
    }

}