package com.administrative.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "approval_workflow")
public class ApprovalWorkflow implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approvalWorkflowId")
    private Long approvalWorkflowId;

    @Column(name = "employeeId", nullable = false)
    private Long employeeId;

    @Column(name = "businessUnitId", nullable = false)
    private Long businessUnitId;

    @Column(name = "areaId", nullable = false)
    private Long areaId;

    @Column(name = "employeeRequestId", nullable = false)
    private Long employeeRequestId;

    @Column(name = "approvalLevel", nullable = false)
    private Integer approvalLevel;

    public ApprovalWorkflow() {}

    public Long getApprovalWorkflowId() { return approvalWorkflowId; }
    public void setApprovalWorkflowId(Long approvalWorkflowId) { this.approvalWorkflowId = approvalWorkflowId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getBusinessUnitId() { return businessUnitId; }
    public void setBusinessUnitId(Long businessUnitId) { this.businessUnitId = businessUnitId; }

    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }

    public Long getEmployeeRequestId() { return employeeRequestId; }
    public void setEmployeeRequestId(Long employeeRequestId) { this.employeeRequestId = employeeRequestId; }

    public Integer getApprovalLevel() { return approvalLevel; }
    public void setApprovalLevel(Integer approvalLevel) { this.approvalLevel = approvalLevel; }
}
