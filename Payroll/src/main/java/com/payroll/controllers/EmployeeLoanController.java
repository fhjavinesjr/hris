package com.payroll.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.payroll.dtos.EmployeeLoanDTO;
import com.payroll.services.EmployeeLoanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EmployeeLoanController {

    private final EmployeeLoanService employeeLoanService;

    public EmployeeLoanController(EmployeeLoanService employeeLoanService) {
        this.employeeLoanService = employeeLoanService;
    }

    @PostMapping("/employeeLoan/create")
    public ResponseEntity<MetadataResponse> create(@RequestBody EmployeeLoanDTO dto) throws Exception {
        EmployeeLoanDTO saved = employeeLoanService.createEmployeeLoan(dto);
        if (saved == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create EmployeeLoan record"));
        }
        return ResponseEntity.ok(new MetadataResponse(saved.getId(), "Successfully created EmployeeLoan record"));
    }

    @GetMapping("/employeeLoan/get-all")
    public ResponseEntity<List<EmployeeLoanDTO>> getAll() throws Exception {
        return ResponseEntity.ok(employeeLoanService.getAllEmployeeLoans());
    }

    @PutMapping("/employeeLoan/update/{id}")
    public ResponseEntity<MetadataResponse> update(@PathVariable("id") Long id, @RequestBody EmployeeLoanDTO dto) throws Exception {
        EmployeeLoanDTO updated = employeeLoanService.updateEmployeeLoan(id, dto);
        return ResponseEntity.ok(new MetadataResponse(updated.getId(), "Successfully updated EmployeeLoan"));
    }

    @DeleteMapping("/employeeLoan/delete/{id}")
    public ResponseEntity<MetadataResponse> delete(@PathVariable("id") Long id) throws Exception {
        Boolean deleted = employeeLoanService.deleteEmployeeLoan(id);
        if (!deleted) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete EmployeeLoan"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Successfully deleted EmployeeLoan"));
    }
}
