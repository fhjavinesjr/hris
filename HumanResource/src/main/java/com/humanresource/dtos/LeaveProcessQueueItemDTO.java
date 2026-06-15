package com.humanresource.dtos;

import java.io.Serializable;

public class LeaveProcessQueueItemDTO implements Serializable {

    private int seqNo;
    private Long employeeId;
    private String employeeNo;
    private String employeeName;
    private String status;
    private String message;

    public LeaveProcessQueueItemDTO() {
    }

    public LeaveProcessQueueItemDTO(int seqNo, Long employeeId, String employeeNo, String employeeName, String status, String message) {
        this.seqNo = seqNo;
        this.employeeId = employeeId;
        this.employeeNo = employeeNo;
        this.employeeName = employeeName;
        this.status = status;
        this.message = message;
    }

    public int getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
