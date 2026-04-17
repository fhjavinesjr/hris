package com.administrative.services;

import com.administrative.entitymodels.ApprovalWorkflow;

import java.util.List;

public interface ApprovalWorkflowService {

    List<ApprovalWorkflow> getByUnitAndRequest(Long businessUnitId, Long employeeRequestId);

    ApprovalWorkflow save(ApprovalWorkflow approvalWorkflow);

    void delete(Long id);
}
