package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.CompensatoryOvertimeCreditDTO;
import com.humanresource.services.CompensatoryOvertimeCreditService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CompensatoryOvertimeCreditController {

    private final CompensatoryOvertimeCreditService cocService;

    public CompensatoryOvertimeCreditController(CompensatoryOvertimeCreditService cocService) {
        this.cocService = cocService;
    }

    @PostMapping("/coc/create")
    public ResponseEntity<MetadataResponse> create(@RequestBody CompensatoryOvertimeCreditDTO dto) throws Exception {
        CompensatoryOvertimeCreditDTO created = cocService.create(dto);
        if (created == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create COC filing"));
        }
        return ResponseEntity.ok(new MetadataResponse(created.getCocId(), "COC filing submitted successfully"));
    }

    @GetMapping("/coc/get-all")
    public ResponseEntity<List<CompensatoryOvertimeCreditDTO>> getAll() throws Exception {
        return ResponseEntity.ok(cocService.getAll());
    }

    @GetMapping("/coc/get-all/{employeeId}")
    public ResponseEntity<List<CompensatoryOvertimeCreditDTO>> getAllByEmployeeId(@PathVariable Long employeeId) throws Exception {
        return ResponseEntity.ok(cocService.getAllByEmployeeId(employeeId));
    }

    @GetMapping("/coc/get-pending")
    public ResponseEntity<List<CompensatoryOvertimeCreditDTO>> getPending() throws Exception {
        return ResponseEntity.ok(cocService.getPendingAll());
    }

    @GetMapping("/coc/balance/{employeeId}")
    public ResponseEntity<Map<String, Double>> getBalance(@PathVariable Long employeeId) throws Exception {
        Double balance = cocService.getAvailableBalance(employeeId);
        return ResponseEntity.ok(Map.of("availableHours", balance));
    }

    @PutMapping("/coc/approve/{cocId}")
    public ResponseEntity<MetadataResponse> approve(@PathVariable Long cocId,
                                                     @RequestBody Map<String, Object> body) throws Exception {
        Long approvedById = body.get("approvedById") != null ? Long.valueOf(body.get("approvedById").toString()) : null;
        String remarks = body.get("remarks") != null ? body.get("remarks").toString() : "";
        CompensatoryOvertimeCreditDTO result = cocService.approve(cocId, approvedById, remarks);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to approve COC"));
        }
        return ResponseEntity.ok(new MetadataResponse(cocId, "COC approved — hours credited to balance"));
    }

    @PutMapping("/coc/disapprove/{cocId}")
    public ResponseEntity<MetadataResponse> disapprove(@PathVariable Long cocId,
                                                        @RequestBody Map<String, Object> body) throws Exception {
        Long approvedById = body.get("approvedById") != null ? Long.valueOf(body.get("approvedById").toString()) : null;
        String remarks = body.get("remarks") != null ? body.get("remarks").toString() : "";
        CompensatoryOvertimeCreditDTO result = cocService.disapprove(cocId, approvedById, remarks);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to disapprove COC"));
        }
        return ResponseEntity.ok(new MetadataResponse(cocId, "COC disapproved"));
    }

    @PutMapping("/coc/update/{cocId}")
    public ResponseEntity<MetadataResponse> update(@PathVariable Long cocId,
                                                    @RequestBody CompensatoryOvertimeCreditDTO dto) throws Exception {
        CompensatoryOvertimeCreditDTO result = cocService.update(cocId, dto);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update COC"));
        }
        return ResponseEntity.ok(new MetadataResponse(cocId, "COC updated successfully"));
    }

    @DeleteMapping("/coc/delete/{cocId}")
    public ResponseEntity<MetadataResponse> delete(@PathVariable Long cocId) throws Exception {
        Boolean deleted = cocService.delete(cocId);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete COC"));
        }
        return ResponseEntity.ok(new MetadataResponse(cocId, "COC deleted successfully"));
    }
}
