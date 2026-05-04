package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.OvertimeRequestDTO;
import com.humanresource.services.OvertimeRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OvertimeRequestController {

    private final OvertimeRequestService overtimeRequestService;

    public OvertimeRequestController(OvertimeRequestService overtimeRequestService) {
        this.overtimeRequestService = overtimeRequestService;
    }

    @PostMapping("/overtime-request/create")
    public ResponseEntity<MetadataResponse> create(@RequestBody OvertimeRequestDTO dto) throws Exception {
        OvertimeRequestDTO created = overtimeRequestService.create(dto);
        if (created == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create overtime request"));
        }
        return ResponseEntity.ok(new MetadataResponse(created.getOvertimeRequestId(), "Overtime request filed successfully"));
    }

    @GetMapping("/overtime-request/get-all")
    public ResponseEntity<List<OvertimeRequestDTO>> getAll() throws Exception {
        return ResponseEntity.ok(overtimeRequestService.getAll());
    }

    @GetMapping("/overtime-request/get-all/{employeeId}")
    public ResponseEntity<List<OvertimeRequestDTO>> getAllByEmployeeId(@PathVariable Long employeeId) throws Exception {
        return ResponseEntity.ok(overtimeRequestService.getAllByEmployeeId(employeeId));
    }

    @GetMapping("/overtime-request/get-pending")
    public ResponseEntity<List<OvertimeRequestDTO>> getPending() throws Exception {
        return ResponseEntity.ok(overtimeRequestService.getPendingAll());
    }

    /** Used by COC filing: returns only Approved OT requests for the dropdown. */
    @GetMapping("/overtime-request/get-approved/{employeeId}")
    public ResponseEntity<List<OvertimeRequestDTO>> getApprovedByEmployee(@PathVariable Long employeeId) throws Exception {
        return ResponseEntity.ok(overtimeRequestService.getApprovedByEmployeeId(employeeId));
    }

    @PutMapping("/overtime-request/approve/{id}")
    public ResponseEntity<MetadataResponse> approve(@PathVariable Long id,
                                                    @RequestBody Map<String, Object> body) throws Exception {
        Long approvedById = body.get("approvedById") != null ? Long.valueOf(body.get("approvedById").toString()) : null;
        String remarks = body.get("remarks") != null ? body.get("remarks").toString() : "";
        OvertimeRequestDTO result = overtimeRequestService.approve(id, approvedById, remarks);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to approve overtime request"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Overtime request approved"));
    }

    @PutMapping("/overtime-request/disapprove/{id}")
    public ResponseEntity<MetadataResponse> disapprove(@PathVariable Long id,
                                                       @RequestBody Map<String, Object> body) throws Exception {
        Long approvedById = body.get("approvedById") != null ? Long.valueOf(body.get("approvedById").toString()) : null;
        String remarks = body.get("remarks") != null ? body.get("remarks").toString() : "";
        OvertimeRequestDTO result = overtimeRequestService.disapprove(id, approvedById, remarks);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to disapprove overtime request"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Overtime request disapproved"));
    }

    @PutMapping("/overtime-request/update/{id}")
    public ResponseEntity<MetadataResponse> update(@PathVariable Long id,
                                                   @RequestBody OvertimeRequestDTO dto) throws Exception {
        OvertimeRequestDTO result = overtimeRequestService.update(id, dto);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update overtime request"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Overtime request updated successfully"));
    }

    @DeleteMapping("/overtime-request/delete/{id}")
    public ResponseEntity<MetadataResponse> delete(@PathVariable Long id) throws Exception {
        Boolean deleted = overtimeRequestService.delete(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete overtime request — it may already be Approved"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Overtime request deleted successfully"));
    }
}
