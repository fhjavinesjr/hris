package com.humanresource.controllers;

import com.humanresource.dtos.EmployeeMweStatusDTO;
import com.humanresource.onboarding.HrmPermissionGuard;
import com.humanresource.services.EmployeeMweStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/employee-mwe-statuses")
public class EmployeeMweStatusController {
    private static final String FEATURE = "payroll.annualization";
    private final EmployeeMweStatusService service;
    private final HrmPermissionGuard permission;

    public EmployeeMweStatusController(EmployeeMweStatusService service,
                                       HrmPermissionGuard permission) {
        this.service = service;
        this.permission = permission;
    }

    @GetMapping
    public List<EmployeeMweStatusDTO> getAll(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) String employeeNo) {
        permission.requireAction(token, FEATURE, HrmPermissionGuard.Action.ACCESS);
        return service.getAll(employeeNo);
    }

    @PostMapping
    public EmployeeMweStatusDTO create(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody EmployeeMweStatusDTO request) {
        permission.requireAction(token, FEATURE, HrmPermissionGuard.Action.ADD);
        return service.create(request, authentication.getName());
    }

    @PutMapping("/{id}")
    public EmployeeMweStatusDTO update(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id,
            @RequestBody EmployeeMweStatusDTO request) {
        permission.requireAction(token, FEATURE, HrmPermissionGuard.Action.EDIT);
        return service.update(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id) {
        permission.requireAction(token, FEATURE, HrmPermissionGuard.Action.DELETE);
        service.delete(id);
    }
}
