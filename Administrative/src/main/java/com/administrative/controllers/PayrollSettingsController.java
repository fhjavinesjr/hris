package com.administrative.controllers;

import com.administrative.dtos.PayrollSettingsDTO;
import com.administrative.services.PayrollSettingsService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PayrollSettingsController {

    private final PayrollSettingsService payrollSettingsService;

    public PayrollSettingsController(PayrollSettingsService payrollSettingsService) {
        this.payrollSettingsService = payrollSettingsService;
    }

    @PostMapping("/payrollSettings/create")
    public ResponseEntity<MetadataResponse> create(@RequestBody PayrollSettingsDTO dto) throws Exception {
        dto = payrollSettingsService.createPayrollSettings(dto);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create Payroll Settings"));
        }
        return ResponseEntity.ok(new MetadataResponse(dto.getPayrollSettingsId(), "Payroll Settings created successfully"));
    }

    @GetMapping("/payrollSettings/get-all")
    public ResponseEntity<List<PayrollSettingsDTO>> getAll() throws Exception {
        return ResponseEntity.ok(payrollSettingsService.getAllPayrollSettings());
    }

    @GetMapping("/payrollSettings/get-current")
    public ResponseEntity<PayrollSettingsDTO> getCurrent() throws Exception {
        return ResponseEntity.ok(payrollSettingsService.getCurrent());
    }

    @PutMapping("/payrollSettings/update/{id}")
    public ResponseEntity<MetadataResponse> update(@PathVariable Long id,
                                                   @RequestBody PayrollSettingsDTO dto) throws Exception {
        dto = payrollSettingsService.updatePayrollSettings(id, dto);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update Payroll Settings"));
        }
        return ResponseEntity.ok(new MetadataResponse(dto.getPayrollSettingsId(), "Payroll Settings updated successfully"));
    }

    @DeleteMapping("/payrollSettings/delete/{id}")
    public ResponseEntity<MetadataResponse> delete(@PathVariable Long id) throws Exception {
        Boolean deleted = payrollSettingsService.deletePayrollSettings(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete Payroll Settings"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Payroll Settings deleted successfully"));
    }

    @PutMapping("/payrollSettings/update-hazard-auto-compute")
    public ResponseEntity<MetadataResponse> updateHazardAutoCompute(@RequestBody java.util.Map<String, Boolean> request) throws Exception {
        Boolean autoCompute = request.get("autoComputeHazardPay");
        Boolean updated = payrollSettingsService.updateHazardAutoCompute(autoCompute);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update auto-compute setting"));
        }
        return ResponseEntity.ok(new MetadataResponse("Auto-compute hazard pay updated successfully"));
    }
}
