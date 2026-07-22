package com.humanresource.controllers;

import com.humanresource.dtos.EmployeeDTO;
import com.humanresource.dtos.EmployeePayrollInfoResponse;
import com.humanresource.services.EmployeeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EmployeeController {

    private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/hris/installAuth")
    public ResponseEntity<Map<String, String>> installAuth() throws Exception {
        log.info("installAuth request received");

        try {
            employeeService.installAuth();
            log.info("installAuth completed successfully");
            return ResponseEntity.ok(Map.of(
                    "message", "Installation account is ready."
            ));
        } catch (Exception exception) {
            // The stack trace is intentionally logged so Render shows the
            // underlying database/configuration error instead of only a 401/500.
            log.error("installAuth failed", exception);
            throw exception;
        }
    }

    @PostMapping("/employee/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> credentials) {
        String token = employeeService.loginEmployee(credentials.get("employeeNo"), credentials.get("employeePassword"));
        return ResponseEntity.ok(token);
    }

    @PostMapping("/singleSignOn/validateToken")
    public ResponseEntity<String> singleSignOn(@RequestBody String token) {
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

    @PutMapping("/employee/admin/reset-password/{employeeId}")
    public ResponseEntity<Void> adminResetPassword(@PathVariable("employeeId") Long employeeId,
            @RequestBody Map<String, String> body) throws Exception {
        employeeService.adminResetPassword(employeeId, body.get("newPassword"));
        return ResponseEntity.ok().build();
    }

    /**
     * Bulk payroll info — called by the Payroll batch service before computation.
     * Returns all employees with an active appointment, including isExcludedFromPayroll
     * derived from the nature-of-appointment's isContractual flag.
     */
    @GetMapping("/employee/payroll-info/bulk")
    public ResponseEntity<List<EmployeePayrollInfoResponse>> getPayrollInfoBulk(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String employeeNo) {
        return ResponseEntity.ok(employeeService.getPayrollInfoBulk(department, employeeNo));
    }
}
