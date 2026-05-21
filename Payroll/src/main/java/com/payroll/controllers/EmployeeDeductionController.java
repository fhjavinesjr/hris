package com.payroll.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.payroll.dtos.EmployeeDeductionDTO;
import com.payroll.services.EmployeeDeductionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EmployeeDeductionController {

    private final EmployeeDeductionService employeeDeductionService;

    public EmployeeDeductionController(EmployeeDeductionService employeeDeductionService) {
        this.employeeDeductionService = employeeDeductionService;
    }

    @PostMapping("/employeeDeduction/create-bulk")
    public ResponseEntity<MetadataResponse> createBulk(@RequestBody List<EmployeeDeductionDTO> dtoList) throws Exception {
        List<EmployeeDeductionDTO> saved = employeeDeductionService.createBulkEmployeeDeduction(dtoList);
        if (saved == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create EmployeeDeduction records"));
        }
        return ResponseEntity.ok(new MetadataResponse((long) saved.size(), "Successfully created " + saved.size() + " EmployeeDeduction record(s)"));
    }

    @GetMapping("/employeeDeduction/get-all")
    public ResponseEntity<List<EmployeeDeductionDTO>> getAll() throws Exception {
        return ResponseEntity.ok(employeeDeductionService.getAllEmployeeDeduction());
    }

    @PutMapping("/employeeDeduction/update/{id}")
    public ResponseEntity<MetadataResponse> update(@PathVariable("id") Long id, @RequestBody EmployeeDeductionDTO dto) throws Exception {
        EmployeeDeductionDTO updated = employeeDeductionService.updateEmployeeDeduction(id, dto);
        return ResponseEntity.ok(new MetadataResponse(updated.getId(), "Successfully updated EmployeeDeduction"));
    }

    @DeleteMapping("/employeeDeduction/delete/{id}")
    public ResponseEntity<MetadataResponse> delete(@PathVariable("id") Long id) throws Exception {
        Boolean deleted = employeeDeductionService.deleteEmployeeDeduction(id);
        if (!deleted) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete EmployeeDeduction"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Successfully deleted EmployeeDeduction"));
    }
}
