package com.administrative.controllers;

import com.administrative.dtos.EmployeeRequestDTO;
import com.administrative.services.EmployeeRequestService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EmployeeRequestController {

    private final EmployeeRequestService employeeRequestService;

    public EmployeeRequestController(EmployeeRequestService employeeRequestService) {
        this.employeeRequestService = employeeRequestService;
    }

    @PostMapping("/employeeRequest/create")
    public ResponseEntity<MetadataResponse> createEmployeeRequest(@RequestBody EmployeeRequestDTO employeeRequestDTO) throws Exception {
        employeeRequestDTO = employeeRequestService.createEmployeeRequest(employeeRequestDTO);
        if (employeeRequestDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create EmployeeRequest"));
        }

        return ResponseEntity.ok(new MetadataResponse(employeeRequestDTO.getEmployeeRequestId(), "Successfully created EmployeeRequest"));
    }

    @GetMapping("/employeeRequest/get-all")
    public ResponseEntity<List<EmployeeRequestDTO>> getAllEmployeeRequests() throws Exception {
        List<EmployeeRequestDTO> employeeRequestDTOList = employeeRequestService.getAllEmployeeRequests();
        return ResponseEntity.ok(employeeRequestDTOList);
    }

    @PutMapping("/employeeRequest/update/{employeeRequestId}")
    public ResponseEntity<MetadataResponse> updateEmployeeRequest(
            @PathVariable("employeeRequestId") Long employeeRequestId,
            @RequestBody EmployeeRequestDTO employeeRequestDTO) throws Exception {

        employeeRequestDTO = employeeRequestService.updateEmployeeRequest(employeeRequestId, employeeRequestDTO);
        if (employeeRequestDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update EmployeeRequest"));
        }

        return ResponseEntity.ok(new MetadataResponse(employeeRequestId, "Successfully updated EmployeeRequest"));
    }

    @DeleteMapping("/employeeRequest/delete/{employeeRequestId}")
    public ResponseEntity<MetadataResponse> deleteEmployeeRequest(@PathVariable("employeeRequestId") Long employeeRequestId) throws Exception {
        Boolean boolDel = employeeRequestService.deleteEmployeeRequest(employeeRequestId);
        if (!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete EmployeeRequest"));
        }

        return ResponseEntity.ok(new MetadataResponse(employeeRequestId, "Successfully deleted EmployeeRequest"));
    }
}
