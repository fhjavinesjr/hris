package com.administrative.dtos;

public class ApprovalWorkflowDTO {

    private Long approvalWorkflowId;
    private Long employeeId;
    private Long businessUnitId;
    private Long areaId;
    private Long employeeRequestId;
    private Integer approvalLevel;

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
