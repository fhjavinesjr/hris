package com.administrative.repositories;

import com.administrative.entitymodels.ApprovalWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, Long> {

    List<ApprovalWorkflow> findByBusinessUnitIdAndEmployeeRequestId(Long businessUnitId, Long employeeRequestId);
}
