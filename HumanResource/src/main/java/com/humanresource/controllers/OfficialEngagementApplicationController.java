package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.OfficialEngagementApplicationDTO;
import com.humanresource.services.OfficialEngagementApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OfficialEngagementApplicationController {

    private final OfficialEngagementApplicationService service;

    public OfficialEngagementApplicationController(OfficialEngagementApplicationService service) {
        this.service = service;
    }

    @PostMapping("/official-engagement/create")
    public ResponseEntity<MetadataResponse> create(@RequestBody OfficialEngagementApplicationDTO dto) throws Exception {
        OfficialEngagementApplicationDTO created = service.create(dto);
        if (created == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create Official Engagement application"));
        }
        return ResponseEntity.ok(new MetadataResponse(created.getOfficialEngagementApplicationId(), "Official Engagement application created successfully"));
    }

    @GetMapping("/official-engagement/get-all")
    public ResponseEntity<List<OfficialEngagementApplicationDTO>> getAll() throws Exception {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/official-engagement/get-all/{employeeId}")
    public ResponseEntity<List<OfficialEngagementApplicationDTO>> getAllByEmployeeId(@PathVariable Long employeeId) throws Exception {
        return ResponseEntity.ok(service.getAllByEmployeeId(employeeId));
    }

    @GetMapping("/official-engagement/get-pending")
    public ResponseEntity<List<OfficialEngagementApplicationDTO>> getPending() throws Exception {
        return ResponseEntity.ok(service.getPendingAll());
    }

    @PutMapping("/official-engagement/approve/{id}")
    public ResponseEntity<MetadataResponse> approve(@PathVariable Long id,
                                                     @RequestBody Map<String, Object> body) throws Exception {
        Long approvedById = body.get("approvedById") != null ? Long.valueOf(body.get("approvedById").toString()) : null;
        String remarks = body.get("remarks") != null ? body.get("remarks").toString() : "";
        OfficialEngagementApplicationDTO result = service.approve(id, approvedById, remarks);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to approve Official Engagement application"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Official Engagement application approved"));
    }

    @PutMapping("/official-engagement/disapprove/{id}")
    public ResponseEntity<MetadataResponse> disapprove(@PathVariable Long id,
                                                        @RequestBody Map<String, Object> body) throws Exception {
        Long approvedById = body.get("approvedById") != null ? Long.valueOf(body.get("approvedById").toString()) : null;
        String remarks = body.get("remarks") != null ? body.get("remarks").toString() : "";
        OfficialEngagementApplicationDTO result = service.disapprove(id, approvedById, remarks);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to disapprove Official Engagement application"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Official Engagement application disapproved"));
    }

    @PutMapping("/official-engagement/update/{id}")
    public ResponseEntity<MetadataResponse> update(@PathVariable Long id,
                                                    @RequestBody OfficialEngagementApplicationDTO dto) throws Exception {
        OfficialEngagementApplicationDTO result = service.update(id, dto);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MetadataResponse("Official Engagement application not found"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Official Engagement application updated"));
    }

    @DeleteMapping("/official-engagement/delete/{id}")
    public ResponseEntity<MetadataResponse> delete(@PathVariable Long id) throws Exception {
        Boolean result = service.delete(id);
        if (!result) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MetadataResponse("Official Engagement application not found"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Official Engagement application deleted"));
    }
}
