package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.OvertimeRequestDTO;
import com.humanresource.services.OvertimeRequestService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
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

    /**
     * Administrative emergency override only. Secure this endpoint with the project's
     * admin permission/role configuration; normal employee portal requests must use /create.
     */
    @PostMapping("/overtime-request/admin-override/create")
    public ResponseEntity<MetadataResponse> createEmergencyOverride(@RequestBody OvertimeRequestDTO dto) throws Exception {
        OvertimeRequestDTO created = overtimeRequestService.createEmergencyOverride(dto);
        if (created == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create emergency overtime override"));
        }
        return ResponseEntity.ok(new MetadataResponse(created.getOvertimeRequestId(), "Emergency overtime override created successfully"));
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

    @PutMapping("/overtime-request/recommend/{id}")
    public ResponseEntity<MetadataResponse> recommend(@PathVariable Long id,
                                                      @RequestBody Map<String, Object> body) throws Exception {
        Long recommendedById = body.get("recommendedById") != null ? Long.valueOf(body.get("recommendedById").toString()) : null;
        String remarks = body.get("remarks") != null ? body.get("remarks").toString() : "";
        String dutyShiftCode = body.get("dutyShiftCode") != null ? body.get("dutyShiftCode").toString() : null;
        Integer breakMinutes = body.get("breakMinutes") != null ? Integer.valueOf(body.get("breakMinutes").toString()) : null;
        OvertimeRequestDTO result = overtimeRequestService.recommend(id, recommendedById, remarks, dutyShiftCode, breakMinutes);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to recommend overtime request"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Overtime request recommended"));
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

    /**
     * HRM maintenance edit. This updates the request details regardless of its
     * workflow status while preserving recommendation/final-decision audit data.
     * Restrict this endpoint with the project's HRM edit permission policy.
     */
    @PutMapping("/overtime-request/hrm-update/{id}")
    public ResponseEntity<MetadataResponse> administrativeUpdate(@PathVariable Long id,
                                                                 @RequestBody OvertimeRequestDTO dto) throws Exception {
        OvertimeRequestDTO result = overtimeRequestService.administrativeUpdate(id, dto);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to administratively update overtime request"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Overtime request administratively updated successfully"));
    }

    @DeleteMapping("/overtime-request/delete/{id}")
    public ResponseEntity<MetadataResponse> delete(@PathVariable Long id) throws Exception {
        Boolean deleted = overtimeRequestService.delete(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete overtime request"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Overtime request deleted successfully"));
    }

    /**
     * HRM maintenance delete. This is intentionally independent of workflow
     * status. Restrict this endpoint with the project's HRM delete permission policy.
     */
    @DeleteMapping("/overtime-request/hrm-delete/{id}")
    public ResponseEntity<MetadataResponse> administrativeDelete(@PathVariable Long id) throws Exception {
        Boolean deleted = overtimeRequestService.administrativeDelete(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MetadataResponse("Overtime request not found or could not be deleted"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Overtime request administratively deleted successfully"));
    }

    @GetMapping("/overtime-request/report/{id}")
    public void downloadOvertimeAuthorization(@PathVariable Long id,
                                              HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=\"OvertimeAuthorization_" + id + ".pdf\"");
        overtimeRequestService.generateOvertimeAuthorization(id, response.getOutputStream());
    }
}
