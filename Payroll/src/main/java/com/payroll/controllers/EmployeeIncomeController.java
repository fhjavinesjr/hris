package com.payroll.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.payroll.dtos.EmployeeIncomeDTO;
import com.payroll.services.EmployeeIncomeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EmployeeIncomeController {

    private final EmployeeIncomeService employeeIncomeService;

    public EmployeeIncomeController(EmployeeIncomeService employeeIncomeService) {
        this.employeeIncomeService = employeeIncomeService;
    }

    @PostMapping("/employeeIncome/create-bulk")
    public ResponseEntity<MetadataResponse> createBulk(@RequestBody List<EmployeeIncomeDTO> dtoList) throws Exception {
        List<EmployeeIncomeDTO> saved = employeeIncomeService.createBulkEmployeeIncome(dtoList);
        if (saved == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create EmployeeIncome records"));
        }
        return ResponseEntity.ok(new MetadataResponse((long) saved.size(), "Successfully created " + saved.size() + " EmployeeIncome record(s)"));
    }

    @GetMapping("/employeeIncome/get-all")
    public ResponseEntity<List<EmployeeIncomeDTO>> getAll() throws Exception {
        return ResponseEntity.ok(employeeIncomeService.getAllEmployeeIncome());
    }

    @GetMapping("/employeeIncome/get-by-period")
    public ResponseEntity<List<EmployeeIncomeDTO>> getByPeriod(@RequestParam int month,
                                                               @RequestParam int year) throws Exception {
        return ResponseEntity.ok(employeeIncomeService.getByMonthAndYear(month, year));
    }

    @PutMapping("/employeeIncome/update/{id}")
    public ResponseEntity<MetadataResponse> update(@PathVariable("id") Long id,
                                                   @RequestBody EmployeeIncomeDTO dto) throws Exception {
        EmployeeIncomeDTO updated = employeeIncomeService.updateEmployeeIncome(id, dto);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update EmployeeIncome"));
        }
        return ResponseEntity.ok(new MetadataResponse(updated.getId(), "Successfully updated EmployeeIncome"));
    }

    @DeleteMapping("/employeeIncome/delete/{id}")
    public ResponseEntity<MetadataResponse> delete(@PathVariable("id") Long id) throws Exception {
        Boolean deleted = employeeIncomeService.deleteEmployeeIncome(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete EmployeeIncome"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Successfully deleted EmployeeIncome"));
    }
}
