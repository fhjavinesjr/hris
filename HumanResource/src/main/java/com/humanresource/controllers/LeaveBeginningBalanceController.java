package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.LeaveBeginningBalanceDTO;
import com.humanresource.services.LeaveBeginningBalanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LeaveBeginningBalanceController {

    private final LeaveBeginningBalanceService leaveBeginningBalanceService;

    public LeaveBeginningBalanceController(LeaveBeginningBalanceService leaveBeginningBalanceService) {
        this.leaveBeginningBalanceService = leaveBeginningBalanceService;
    }

    @PostMapping("/leave-beginning-balance/save-all/{employeeId}")
    public ResponseEntity<MetadataResponse> saveAll(
            @PathVariable Long employeeId,
            @RequestBody List<LeaveBeginningBalanceDTO> dtoList) throws Exception {
        List<LeaveBeginningBalanceDTO> result = leaveBeginningBalanceService.saveAll(employeeId, dtoList);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to save leave beginning balances"));
        }
        return ResponseEntity.ok(new MetadataResponse(employeeId, "Successfully saved leave beginning balances"));
    }

    @GetMapping("/leave-beginning-balance/get-all")
    public ResponseEntity<List<LeaveBeginningBalanceDTO>> getAll() throws Exception {
        List<LeaveBeginningBalanceDTO> list = leaveBeginningBalanceService.getAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/leave-beginning-balance/get-all/{employeeId}")
    public ResponseEntity<List<LeaveBeginningBalanceDTO>> getAllByEmployeeId(
            @PathVariable Long employeeId) throws Exception {
        List<LeaveBeginningBalanceDTO> list = leaveBeginningBalanceService.getAllByEmployeeId(employeeId);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/leave-beginning-balance/delete/{id}")
    public ResponseEntity<MetadataResponse> deleteById(@PathVariable Long id) throws Exception {
        Boolean deleted = leaveBeginningBalanceService.deleteById(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete leave beginning balance"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Successfully deleted leave beginning balance"));
    }
}
