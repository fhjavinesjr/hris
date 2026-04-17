package com.administrative.impl;

import com.administrative.entitymodels.ApprovalWorkflow;
import com.administrative.repositories.ApprovalWorkflowRepository;
import com.administrative.services.ApprovalWorkflowService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApprovalWorkflowImpl implements ApprovalWorkflowService {

    private final ApprovalWorkflowRepository approvalWorkflowRepository;

    public ApprovalWorkflowImpl(ApprovalWorkflowRepository approvalWorkflowRepository) {
        this.approvalWorkflowRepository = approvalWorkflowRepository;
    }

    @Override
    public List<ApprovalWorkflow> getByUnitAndRequest(Long businessUnitId, Long employeeRequestId) {
        return approvalWorkflowRepository.findByBusinessUnitIdAndEmployeeRequestId(businessUnitId, employeeRequestId);
    }

    @Transactional
    @Override
    public ApprovalWorkflow save(ApprovalWorkflow approvalWorkflow) {
        return approvalWorkflowRepository.save(approvalWorkflow);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        approvalWorkflowRepository.deleteById(id);
    }
}
