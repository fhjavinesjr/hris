package com.humanresource.controllers;

import com.humanresource.dtos.EmployeeDTO;
import com.humanresource.services.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/hris/installAuth")
    public void installAuth() throws Exception {
        employeeService.installAuth();
    }

    @PostMapping("/employee/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> credentials) {
        String token = employeeService.loginEmployee(credentials.get("employeeNo"), credentials.get("employeePassword"));
        return ResponseEntity.ok(token);
    }

    @PostMapping("/employee/register")
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody EmployeeDTO employeeDTO) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(employeeDTO));
    }

    @GetMapping("/employee/{employeeId}")
    public EmployeeDTO getEmployeeDisplayById(@PathVariable("employeeId") Long employeeId) throws Exception {
        return employeeService.getEmployeeDisplayById(employeeId);
    }

    @PutMapping("/employee/update/{employeeId}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable("employeeId") Long employeeId, @Valid @RequestBody Map<String, Object> updates) throws Exception {
        EmployeeDTO updateEmployee = employeeService.updateEmployee(employeeId, updates);
        return ResponseEntity.ok(updateEmployee);
    }

    @GetMapping("/employees/basicInfo")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployeeNoAndName() throws Exception {
        return ResponseEntity.ok(employeeService.getAllEmployeeNoAndName());
    }

    @PutMapping("/employee/password/update/{employeeId}")
    public ResponseEntity<EmployeeDTO> updateEmployeePassword(@PathVariable("employeeId") Long employeeId, @Valid @RequestBody Map<String, Object> updates) throws Exception {
        EmployeeDTO updateEmployee = employeeService.updateEmployeePassword(employeeId, updates);
        return ResponseEntity.ok(updateEmployee);
    }
}