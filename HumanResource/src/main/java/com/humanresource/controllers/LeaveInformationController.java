package com.humanresource.controllers;

import com.humanresource.dtos.LeaveInformationDTO;
import com.humanresource.services.LeaveInformationService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/leave-information")
@CrossOrigin("*")
public class LeaveInformationController {

    private final LeaveInformationService service;

    public LeaveInformationController(LeaveInformationService service) {
        this.service = service;
    }

    /**
     * GET /api/leave-information/get-all/{employeeId}
     * Returns all leave information records for an employee in chronological order.
     * Used for the employee's leave ledger (CSC Form 48 view).
     */
    @GetMapping("/get-all/{employeeId}")
    public ResponseEntity<List<LeaveInformationDTO>> getByEmployeeId(@PathVariable Long employeeId) {
        try {
            return ResponseEntity.ok(service.getByEmployeeId(employeeId));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/leave-information/get-by-period?start=yyyy-MM-dd&end=yyyy-MM-dd
     * Returns all leave information records for a given period (all employees).
     * Used for the HR period view / bulk report.
     */
    @GetMapping("/get-by-period")
    public ResponseEntity<List<LeaveInformationDTO>> getByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        try {
            return ResponseEntity.ok(service.getByPeriod(start, end));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/leave-information/get-by-salary-period/{salaryPeriodSettingId}
     * Returns all records associated with a salary period setting ID.
     */
    @GetMapping("/get-by-salary-period/{salaryPeriodSettingId}")
    public ResponseEntity<List<LeaveInformationDTO>> getBySalaryPeriod(
            @PathVariable Long salaryPeriodSettingId) {
        try {
            return ResponseEntity.ok(service.getBySalaryPeriodSettingId(salaryPeriodSettingId));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * PUT /api/leave-information/lock/{leaveInformationId}
     * Locks a leave information record. Locked records cannot be reprocessed or deleted.
     */
    @PutMapping("/lock/{leaveInformationId}")
    public ResponseEntity<MetadataResponse> lock(@PathVariable Long leaveInformationId) {
        try {
            LeaveInformationDTO locked = service.lock(leaveInformationId);
            if (locked == null) {
                return ResponseEntity.badRequest()
                        .body(new MetadataResponse("Record not found or could not be locked"));
            }
            return ResponseEntity.ok(new MetadataResponse(leaveInformationId, "Leave information locked successfully"));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new MetadataResponse("Error locking record: " + ex.getMessage()));
        }
    }

    /**
     * PUT /api/leave-information/unlock/{leaveInformationId}
     * Unlocks a leave information record to allow reprocessing. (Admin-level operation.)
     */
    @PutMapping("/unlock/{leaveInformationId}")
    public ResponseEntity<MetadataResponse> unlock(@PathVariable Long leaveInformationId) {
        try {
            LeaveInformationDTO unlocked = service.unlock(leaveInformationId);
            if (unlocked == null) {
                return ResponseEntity.badRequest()
                        .body(new MetadataResponse("Record not found or could not be unlocked"));
            }
            return ResponseEntity.ok(new MetadataResponse(leaveInformationId, "Leave information unlocked successfully"));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new MetadataResponse("Error unlocking record: " + ex.getMessage()));
        }
    }

    /**
     * DELETE /api/leave-information/delete/{leaveInformationId}
     * Deletes a leave information record. Locked records cannot be deleted.
     */
    @DeleteMapping("/delete/{leaveInformationId}")
    public ResponseEntity<MetadataResponse> delete(@PathVariable Long leaveInformationId) {
        try {
            Boolean deleted = service.delete(leaveInformationId);
            if (!Boolean.TRUE.equals(deleted)) {
                return ResponseEntity.badRequest()
                        .body(new MetadataResponse("Record not found, is locked, or could not be deleted"));
            }
            return ResponseEntity.ok(new MetadataResponse(leaveInformationId, "Leave information deleted successfully"));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new MetadataResponse("Error deleting record: " + ex.getMessage()));
        }
    }
}
