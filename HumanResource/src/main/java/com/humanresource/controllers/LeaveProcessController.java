package com.humanresource.controllers;

import com.humanresource.dtos.LeaveProcessRequestDTO;
import com.humanresource.dtos.LeaveProcessResultDTO;
import com.humanresource.services.LeaveProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Trigger endpoint for the Leave Information computation engine.
 *
 * Usage:
 *   POST /api/leave-information/process
 *   Body: {
 *     "salaryPeriodSettingId": 1,
 *     "cutoffStartDate": "2024-11-16",
 *     "cutoffEndDate": "2024-11-30",
 *     "scope": "ALL",          // or "EMPLOYEE"
 *     "employeeId": null,      // required when scope = EMPLOYEE
 *     "processedById": 1
 *   }
 *
 * The frontend (HR Management UI) resolves the cutoff dates from the
 * SalaryPeriodSetting, then passes the already-resolved dates here.
 * This keeps the engine independent of date-resolution logic.
 */
@RestController
@RequestMapping("/api/leave-information")
@CrossOrigin("*")
public class LeaveProcessController {

    private final LeaveProcessService processService;

    public LeaveProcessController(LeaveProcessService processService) {
        this.processService = processService;
    }

    @PostMapping("/process")
    public ResponseEntity<LeaveProcessResultDTO> process(
            @RequestBody LeaveProcessRequestDTO request) {
        try {
            if (request.getCutoffStartDate() == null || request.getCutoffEndDate() == null) {
                LeaveProcessResultDTO err = new LeaveProcessResultDTO(
                        0, 0, java.util.List.of("cutoffStartDate and cutoffEndDate are required"), java.util.List.of());
                return ResponseEntity.badRequest().body(err);
            }
            if (request.getScope() == null) {
                request.setScope("ALL");
            }
            LeaveProcessResultDTO result = processService.process(request);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            LeaveProcessResultDTO errResult = new LeaveProcessResultDTO(
                    0, 0,
                    java.util.List.of("Server error: " + ex.getMessage()),
                    java.util.List.of());
            return ResponseEntity.internalServerError().body(errResult);
        }
    }
}
