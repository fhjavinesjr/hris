package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.CompensatoryTimeOffDTO;
import com.humanresource.services.CompensatoryTimeOffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CompensatoryTimeOffController {

    private final CompensatoryTimeOffService ctoService;

    public CompensatoryTimeOffController(CompensatoryTimeOffService ctoService) {
        this.ctoService = ctoService;
    }

    @PostMapping("/cto/create")
    public ResponseEntity<MetadataResponse> create(@RequestBody CompensatoryTimeOffDTO dto) throws Exception {
        CompensatoryTimeOffDTO created = ctoService.create(dto);
        if (created == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create CTO. Check COC balance or duplicate date."));
        }
        return ResponseEntity.ok(new MetadataResponse(created.getCtoId(), "CTO filed successfully"));
    }

    @GetMapping("/cto/get-all")
    public ResponseEntity<List<CompensatoryTimeOffDTO>> getAll() throws Exception {
        return ResponseEntity.ok(ctoService.getAll());
    }

    @GetMapping("/cto/get-all/{employeeId}")
    public ResponseEntity<List<CompensatoryTimeOffDTO>> getAllByEmployeeId(@PathVariable Long employeeId) throws Exception {
        return ResponseEntity.ok(ctoService.getAllByEmployeeId(employeeId));
    }

    @GetMapping("/cto/get-pending")
    public ResponseEntity<List<CompensatoryTimeOffDTO>> getPending() throws Exception {
        return ResponseEntity.ok(ctoService.getPendingAll());
    }

    @PutMapping("/cto/approve/{ctoId}")
    public ResponseEntity<MetadataResponse> approve(@PathVariable Long ctoId,
                                                     @RequestBody Map<String, Object> body) throws Exception {
        Long approvedById = body.get("approvedById") != null ? Long.valueOf(body.get("approvedById").toString()) : null;
        String remarks = body.get("remarks") != null ? body.get("remarks").toString() : "";
        CompensatoryTimeOffDTO result = ctoService.approve(ctoId, approvedById, remarks);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to approve CTO"));
        }
        return ResponseEntity.ok(new MetadataResponse(ctoId, "CTO approved — day off granted"));
    }

    @PutMapping("/cto/disapprove/{ctoId}")
    public ResponseEntity<MetadataResponse> disapprove(@PathVariable Long ctoId,
                                                        @RequestBody Map<String, Object> body) throws Exception {
        Long approvedById = body.get("approvedById") != null ? Long.valueOf(body.get("approvedById").toString()) : null;
        String remarks = body.get("remarks") != null ? body.get("remarks").toString() : "";
        CompensatoryTimeOffDTO result = ctoService.disapprove(ctoId, approvedById, remarks);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to disapprove CTO"));
        }
        return ResponseEntity.ok(new MetadataResponse(ctoId, "CTO disapproved"));
    }

    @PutMapping("/cto/update/{ctoId}")
    public ResponseEntity<MetadataResponse> update(@PathVariable Long ctoId,
                                                    @RequestBody CompensatoryTimeOffDTO dto) throws Exception {
        CompensatoryTimeOffDTO result = ctoService.update(ctoId, dto);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update CTO"));
        }
        return ResponseEntity.ok(new MetadataResponse(ctoId, "CTO updated successfully"));
    }

    @DeleteMapping("/cto/delete/{ctoId}")
    public ResponseEntity<MetadataResponse> delete(@PathVariable Long ctoId) throws Exception {
        Boolean deleted = ctoService.delete(ctoId);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete CTO"));
        }
        return ResponseEntity.ok(new MetadataResponse(ctoId, "CTO deleted successfully"));
    }
}
