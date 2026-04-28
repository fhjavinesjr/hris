package com.administrative.controllers;

import com.administrative.dtos.SalaryPeriodSettingDTO;
import com.administrative.services.SalaryPeriodSettingService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salary-period-setting")
public class SalaryPeriodSettingController {

    private final SalaryPeriodSettingService service;

    public SalaryPeriodSettingController(SalaryPeriodSettingService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<MetadataResponse> create(@RequestBody SalaryPeriodSettingDTO dto) throws Exception {
        SalaryPeriodSettingDTO created = service.create(dto);
        if (created == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create salary period setting."));
        }
        return ResponseEntity.ok(new MetadataResponse(created.getSalaryPeriodSettingId(), "Salary period setting created successfully."));
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<SalaryPeriodSettingDTO>> getAll() throws Exception {
        return ResponseEntity.ok(service.getAll());
    }

    /**
     * Used by DTR, Leave, and Payroll consumer modules.
     * Returns active settings matching the given context, plus any BOTH settings.
     *
     * @param context  "PAYROLL" or "LEAVE"
     */
    @GetMapping("/get-by-context")
    public ResponseEntity<List<SalaryPeriodSettingDTO>> getByContext(@RequestParam String context) throws Exception {
        return ResponseEntity.ok(service.getByContext(context));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<MetadataResponse> update(
            @PathVariable Long id,
            @RequestBody SalaryPeriodSettingDTO dto) throws Exception {
        SalaryPeriodSettingDTO updated = service.update(id, dto);
        if (updated == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update salary period setting."));
        }
        return ResponseEntity.ok(new MetadataResponse(updated.getSalaryPeriodSettingId(), "Salary period setting updated successfully."));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<MetadataResponse> delete(@PathVariable Long id) throws Exception {
        Boolean deleted = service.delete(id);
        if (!Boolean.TRUE.equals(deleted)) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete salary period setting."));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Salary period setting deleted successfully."));
    }
}
