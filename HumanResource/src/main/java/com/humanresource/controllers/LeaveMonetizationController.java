package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.LeaveMonetizationDTO;
import com.humanresource.services.LeaveMonetizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LeaveMonetizationController {

    private final LeaveMonetizationService leaveMonetizationService;

    public LeaveMonetizationController(LeaveMonetizationService leaveMonetizationService) {
        this.leaveMonetizationService = leaveMonetizationService;
    }

    /** File a new leave monetization request. */
    @PostMapping("/leave-monetization/create")
    public ResponseEntity<MetadataResponse> create(@RequestBody LeaveMonetizationDTO dto) throws Exception {
        LeaveMonetizationDTO created = leaveMonetizationService.create(dto);
        if (created == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create leave monetization"));
        }
        return ResponseEntity.ok(new MetadataResponse(created.getLeaveMonetizationId(), "Leave monetization filed successfully"));
    }

    /** All leave monetization records. */
    @GetMapping("/leave-monetization/get-all")
    public ResponseEntity<List<LeaveMonetizationDTO>> getAll() throws Exception {
        return ResponseEntity.ok(leaveMonetizationService.getAll());
    }

    /** All leave monetization records for a specific employee. */
    @GetMapping("/leave-monetization/get-all/{employeeId}")
    public ResponseEntity<List<LeaveMonetizationDTO>> getAllByEmployeeId(@PathVariable Long employeeId) throws Exception {
        return ResponseEntity.ok(leaveMonetizationService.getAllByEmployeeId(employeeId));
    }

    /** All records pending final approval — for the HR approval screen. */
    @GetMapping("/leave-monetization/get-pending")
    public ResponseEntity<List<LeaveMonetizationDTO>> getPending() throws Exception {
        return ResponseEntity.ok(leaveMonetizationService.getPending());
    }

    /** Level 1: Recommending authority approves the request. */
    @PutMapping("/leave-monetization/recommend/{id}")
    public ResponseEntity<MetadataResponse> recommend(@PathVariable Long id,
                                                      @RequestBody Map<String, Object> body) throws Exception {
        Long recommendedById = body.get("recommendedById") != null
                ? Long.valueOf(body.get("recommendedById").toString()) : null;
        String remarks = body.get("remarks") != null ? body.get("remarks").toString() : "";
        LeaveMonetizationDTO result = leaveMonetizationService.recommend(id, recommendedById, remarks);
        return ResponseEntity.ok(new MetadataResponse(id, "Leave monetization recommendation approved"));
    }

    /** Level 1: Recommending authority disapproves the request. */
    @PutMapping("/leave-monetization/disapprove-recommendation/{id}")
    public ResponseEntity<MetadataResponse> disapproveRecommendation(@PathVariable Long id,
                                                                     @RequestBody Map<String, Object> body) throws Exception {
        String remarks = body.get("remarks") != null ? body.get("remarks").toString() : "";
        leaveMonetizationService.disapproveRecommendation(id, remarks);
        return ResponseEntity.ok(new MetadataResponse(id, "Leave monetization recommendation disapproved"));
    }

    /**
     * Level 2: Final authority approves the request.
     * Enforces CSC MC No. 41 s. 1998: minimum 10 days, VL reserve >= 5, SL reserve >= 5.
     * Returns 400 Bad Request if any CSC rule is violated.
     */
    @PutMapping("/leave-monetization/approve/{id}")
    public ResponseEntity<MetadataResponse> approve(@PathVariable Long id,
                                                    @RequestBody Map<String, Object> body) {
        try {
            Long approvedById = body.get("approvedById") != null
                    ? Long.valueOf(body.get("approvedById").toString()) : null;
            String remarks = body.get("remarks") != null ? body.get("remarks").toString() : "";
            leaveMonetizationService.approve(id, approvedById, remarks);
            return ResponseEntity.ok(new MetadataResponse(id, "Leave monetization approved"));
        } catch (IllegalArgumentException e) {
            // CSC rule violation — return 400 with the reason
            return ResponseEntity.badRequest().body(new MetadataResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MetadataResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to approve leave monetization"));
        }
    }

    /** Level 2: Final authority disapproves the request. */
    @PutMapping("/leave-monetization/disapprove/{id}")
    public ResponseEntity<MetadataResponse> disapprove(@PathVariable Long id,
                                                       @RequestBody Map<String, Object> body) {
        try {
            String remarks = body.get("remarks") != null ? body.get("remarks").toString() : "";
            leaveMonetizationService.disapprove(id, remarks);
            return ResponseEntity.ok(new MetadataResponse(id, "Leave monetization disapproved"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MetadataResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to disapprove leave monetization"));
        }
    }

    /** Update editable fields of a pending leave monetization record. */
    @PutMapping("/leave-monetization/update/{id}")
    public ResponseEntity<MetadataResponse> update(@PathVariable Long id,
                                                   @RequestBody LeaveMonetizationDTO dto) {
        try {
            LeaveMonetizationDTO result = leaveMonetizationService.update(id, dto);
            if (result == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new MetadataResponse("Failed to update leave monetization"));
            }
            return ResponseEntity.ok(new MetadataResponse(id, "Leave monetization updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update leave monetization"));
        }
    }

    /** Delete a pending leave monetization record. Approved records cannot be deleted. */
    @DeleteMapping("/leave-monetization/delete/{id}")
    public ResponseEntity<MetadataResponse> delete(@PathVariable Long id) {
        try {
            Boolean deleted = leaveMonetizationService.delete(id);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new MetadataResponse("Failed to delete leave monetization"));
            }
            return ResponseEntity.ok(new MetadataResponse(id, "Leave monetization deleted"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MetadataResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete leave monetization"));
        }
    }
}
