package com.administrative.controllers;

import com.administrative.dtos.SystemConfigDTO;
import com.administrative.services.SystemConfigService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @GetMapping("/system-config/get-all")
    public ResponseEntity<List<SystemConfigDTO>> getAllConfigs() throws Exception {
        List<SystemConfigDTO> configs = systemConfigService.getAllConfigs();
        return ResponseEntity.ok(configs);
    }

    @GetMapping("/public/runtime-config")
    public ResponseEntity<Map<String, String>> getPublicRuntimeConfig() {
        return ResponseEntity.ok(systemConfigService.getPublicRuntimeConfig());
    }

    @GetMapping("/system-config/get/{configKey}")
    public ResponseEntity<?> getByKey(@PathVariable("configKey") String configKey) throws Exception {
        SystemConfigDTO dto = systemConfigService.getByKey(configKey);
        if (dto == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MetadataResponse("Config key not found: " + configKey));
        }
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/system-config/update/{configKey}")
    public ResponseEntity<MetadataResponse> updateConfig(
            @PathVariable("configKey") String configKey,
            @RequestBody SystemConfigDTO dto) throws Exception {
        SystemConfigDTO updated = systemConfigService.updateConfig(configKey, dto);
        if (updated == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MetadataResponse("Failed to update config: " + configKey));
        }
        return ResponseEntity.ok(new MetadataResponse(configKey, "Configuration updated successfully"));
    }
}
