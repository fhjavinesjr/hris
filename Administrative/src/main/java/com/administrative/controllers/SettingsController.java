package com.administrative.controllers;

import com.administrative.dtos.SettingsDTO;
import com.administrative.services.SettingsService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @PostMapping("/settings/create")
    public ResponseEntity<MetadataResponse> createSettings(@RequestBody SettingsDTO settingsDTO) throws Exception {
        settingsDTO = settingsService.createSettings(settingsDTO);
        if(settingsDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update Settings"));
        }

        return ResponseEntity.ok(new MetadataResponse(settingsDTO.getSettingsId(), "Successful to update Settings"));
    }

    @GetMapping("/settings/get-all")
    public ResponseEntity<List<SettingsDTO>> getAllSettings() throws Exception {
        List<SettingsDTO> settingsDTOList = settingsService.getAllSettings();
        return ResponseEntity.ok(settingsDTOList);
    }

    @PutMapping("/settings/update/{settingsId}")
    public ResponseEntity<MetadataResponse> updateSettings(@PathVariable("settingsId") Long settingsId, @RequestBody SettingsDTO settingsDTO) throws Exception {
        settingsDTO = settingsService.updateSettings(settingsId, settingsDTO);
        if(settingsDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update settings"));
        }

        return ResponseEntity.ok(new MetadataResponse(settingsDTO.getSettingsId(), "Successful to update settings"));
    }

    @DeleteMapping("/settings/delete/{settingsId}")
    public ResponseEntity<MetadataResponse> deleteSettings(@PathVariable("settingsId") Long settingsId) throws Exception {
        Boolean boolDel = settingsService.deleteSettings(settingsId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete settings"));
        }

        return ResponseEntity.ok(new MetadataResponse(settingsId, "Successful to settings"));
    }

}