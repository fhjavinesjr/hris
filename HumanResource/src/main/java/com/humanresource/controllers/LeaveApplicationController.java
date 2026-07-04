package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.ApprovedLeaveDTO;
import com.humanresource.dtos.LeaveApplicationDTO;
import com.humanresource.services.LeaveFormReportService;
import com.humanresource.services.LeaveApplicationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class LeaveApplicationController {

    private final LeaveApplicationService leaveApplicationService;
    private final LeaveFormReportService leaveFormReportService;

    public LeaveApplicationController(LeaveApplicationService leaveApplicationService,
                                      LeaveFormReportService leaveFormReportService) {
        this.leaveApplicationService = leaveApplicationService;
        this.leaveFormReportService = leaveFormReportService;
    }

    @GetMapping("/leave/bulk-approved")
    public ResponseEntity<List<ApprovedLeaveDTO>> getBulkApprovedLeaves(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) throws Exception {
        List<ApprovedLeaveDTO> list = leaveApplicationService.getBulkApprovedLeaves(from, to);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/leave-application/create")
    public ResponseEntity<MetadataResponse> createLeaveApplication(@RequestBody LeaveApplicationDTO leaveApplicationDTO) throws Exception {
        leaveApplicationDTO = leaveApplicationService.createLeaveApplication(leaveApplicationDTO);
        if (leaveApplicationDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create Leave Application"));
        }
        return ResponseEntity.ok(new MetadataResponse(leaveApplicationDTO.getLeaveApplicationId(), "Successfully created Leave Application"));
    }

    @GetMapping("/leave-application/get-all")
    public ResponseEntity<List<LeaveApplicationDTO>> getAllLeaveApplications() throws Exception {
        List<LeaveApplicationDTO> list = leaveApplicationService.getAllLeaveApplications();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/leave-application/get-all/{employeeId}")
    public ResponseEntity<List<LeaveApplicationDTO>> getAllLeaveApplicationsByEmployeeId(@PathVariable Long employeeId) throws Exception {
        List<LeaveApplicationDTO> list = leaveApplicationService.getAllLeaveApplicationsByEmployeeId(employeeId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/leave-application/get-all/{employeeId}/{leaveType}")
    public ResponseEntity<List<LeaveApplicationDTO>> getAllLeaveApplicationsByEmployeeIdAndLeaveType(
            @PathVariable Long employeeId,
            @PathVariable String leaveType) throws Exception {
        List<LeaveApplicationDTO> list = leaveApplicationService.getAllLeaveApplicationsByEmployeeIdAndLeaveType(employeeId, leaveType);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/leave-application/get/{leaveApplicationId}")
    public ResponseEntity<LeaveApplicationDTO> getLeaveApplicationById(@PathVariable Long leaveApplicationId) throws Exception {
        LeaveApplicationDTO dto = leaveApplicationService.getLeaveApplicationById(leaveApplicationId);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/leave-application/update/{leaveApplicationId}")
    public ResponseEntity<MetadataResponse> updateLeaveApplication(
            @PathVariable Long leaveApplicationId,
            @RequestBody LeaveApplicationDTO leaveApplicationDTO) throws Exception {
        leaveApplicationDTO = leaveApplicationService.updateLeaveApplication(leaveApplicationId, leaveApplicationDTO);
        if (leaveApplicationDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update Leave Application"));
        }
        return ResponseEntity.ok(new MetadataResponse(leaveApplicationDTO.getLeaveApplicationId(), "Successfully updated Leave Application"));
    }

    @DeleteMapping("/leave-application/delete/{leaveApplicationId}")
    public ResponseEntity<MetadataResponse> deleteLeaveApplication(@PathVariable Long leaveApplicationId) throws Exception {
        Boolean deleted = leaveApplicationService.deleteLeaveApplication(leaveApplicationId);
        if (!deleted) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete Leave Application"));
        }
        return ResponseEntity.ok(new MetadataResponse(leaveApplicationId, "Successfully deleted Leave Application"));
    }

    @GetMapping("/leave-application/report/{leaveApplicationId}")
    public void downloadLeaveApplicationReport(@PathVariable Long leaveApplicationId,
                                               HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=\"LeaveForm_" + leaveApplicationId + ".pdf\"");
        leaveFormReportService.generateLeaveForm(leaveApplicationId, response.getOutputStream());
    }
}
