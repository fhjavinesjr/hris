package com.timekeeping.controllers;

import com.timekeeping.dtos.AdmsSyncResultDTO;
import com.timekeeping.entitymodels.AdmsPunchLog;
import com.timekeeping.services.AdmsSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/adms")
public class AdmsSyncController {

    private final AdmsSyncService admsSyncService;

    public AdmsSyncController(AdmsSyncService admsSyncService) {
        this.admsSyncService = admsSyncService;
    }

    @PostMapping("/sync")
    public ResponseEntity<AdmsSyncResultDTO> syncNewPunches() {
        return ResponseEntity.ok(admsSyncService.syncNewPunches());
    }

    @GetMapping("/sync/status")
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        return ResponseEntity.ok(admsSyncService.getSyncStatus());
    }

    @GetMapping("/unmapped")
    public ResponseEntity<List<AdmsPunchLog>> getUnmappedPunches() {
        return ResponseEntity.ok(admsSyncService.getUnmappedPunches());
    }
}
