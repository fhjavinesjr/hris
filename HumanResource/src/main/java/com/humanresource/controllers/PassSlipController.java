package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.PassSlipDTO;
import com.humanresource.services.PassSlipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PassSlipController {

    private final PassSlipService passSlipService;

    public PassSlipController(PassSlipService passSlipService) {
        this.passSlipService = passSlipService;
    }

    @PostMapping("/pass-slip/create")
    public ResponseEntity<MetadataResponse> createPassSlip(@RequestBody PassSlipDTO dto) throws Exception {
        PassSlipDTO created = passSlipService.create(dto);
        if (created == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create Pass Slip"));
        }
        return ResponseEntity.ok(new MetadataResponse(created.getPassSlipId(), "Pass Slip created successfully"));
    }

    @GetMapping("/pass-slip/get-all")
    public ResponseEntity<List<PassSlipDTO>> getAll() throws Exception {
        return ResponseEntity.ok(passSlipService.getAll());
    }

    @GetMapping("/pass-slip/get-all/{employeeId}")
    public ResponseEntity<List<PassSlipDTO>> getAllByEmployeeId(@PathVariable Long employeeId) throws Exception {
        return ResponseEntity.ok(passSlipService.getAllByEmployeeId(employeeId));
    }

    @GetMapping("/pass-slip/get-pending")
    public ResponseEntity<List<PassSlipDTO>> getPending() throws Exception {
        return ResponseEntity.ok(passSlipService.getPendingAll());
    }

    @PutMapping("/pass-slip/approve/{passSlipId}")
    public ResponseEntity<MetadataResponse> approve(@PathVariable Long passSlipId,
                                                     @RequestBody Map<String, Object> body) throws Exception {
        Long approvedById = body.get("approvedById") != null ? Long.valueOf(body.get("approvedById").toString()) : null;
        String remarks = body.get("remarks") != null ? body.get("remarks").toString() : "";
        PassSlipDTO result = passSlipService.approve(passSlipId, approvedById, remarks);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to approve Pass Slip"));
        }
        return ResponseEntity.ok(new MetadataResponse(passSlipId, "Pass Slip approved"));
    }

    @PutMapping("/pass-slip/disapprove/{passSlipId}")
    public ResponseEntity<MetadataResponse> disapprove(@PathVariable Long passSlipId,
                                                        @RequestBody Map<String, Object> body) throws Exception {
        Long approvedById = body.get("approvedById") != null ? Long.valueOf(body.get("approvedById").toString()) : null;
        String remarks = body.get("remarks") != null ? body.get("remarks").toString() : "";
        PassSlipDTO result = passSlipService.disapprove(passSlipId, approvedById, remarks);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to disapprove Pass Slip"));
        }
        return ResponseEntity.ok(new MetadataResponse(passSlipId, "Pass Slip disapproved"));
    }

    @PutMapping("/pass-slip/update/{passSlipId}")
    public ResponseEntity<MetadataResponse> update(@PathVariable Long passSlipId,
                                                    @RequestBody PassSlipDTO dto) throws Exception {
        PassSlipDTO result = passSlipService.update(passSlipId, dto);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update Pass Slip"));
        }
        return ResponseEntity.ok(new MetadataResponse(passSlipId, "Pass Slip updated successfully"));
    }

    @DeleteMapping("/pass-slip/delete/{passSlipId}")
    public ResponseEntity<MetadataResponse> delete(@PathVariable Long passSlipId) throws Exception {
        Boolean deleted = passSlipService.delete(passSlipId);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete Pass Slip"));
        }
        return ResponseEntity.ok(new MetadataResponse(passSlipId, "Pass Slip deleted successfully"));
    }
}
