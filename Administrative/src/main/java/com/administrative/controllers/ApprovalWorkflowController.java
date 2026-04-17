package com.administrative.controllers;

import com.administrative.entitymodels.ApprovalWorkflow;
import com.administrative.services.ApprovalWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approval-workflow")
public class ApprovalWorkflowController {

    @Autowired
    private ApprovalWorkflowService service;

    @GetMapping("/get-by-unit-and-request")
    public List<ApprovalWorkflow> getByUnitAndRequest(
            @RequestParam Long businessUnitId,
            @RequestParam Long employeeRequestId) {
        return service.getByUnitAndRequest(businessUnitId, employeeRequestId);
    }

    @PostMapping("/save")
    public ApprovalWorkflow save(@RequestBody ApprovalWorkflow approvalWorkflow) {
        return service.save(approvalWorkflow);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
