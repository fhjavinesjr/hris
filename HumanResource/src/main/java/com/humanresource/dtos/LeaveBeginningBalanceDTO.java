package com.humanresource.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveBeginningBalanceDTO implements Serializable {

    private Long leaveBeginningBalanceId;

    @NotNull(message = "employeeId is mandatory")
    private Long employeeId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate asOfDate;

    @NotNull(message = "leaveType is mandatory")
    private String leaveType;

    private Double balance;

    private Long leaveTypesId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public LeaveBeginningBalanceDTO() {
    }

    public LeaveBeginningBalanceDTO(Long leaveBeginningBalanceId, Long employeeId, LocalDate asOfDate,
                                     String leaveType, Double balance, Long leaveTypesId,
                                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.leaveBeginningBalanceId = leaveBeginningBalanceId;
        this.employeeId = employeeId;
        this.asOfDate = asOfDate;
        this.leaveType = leaveType;
        this.balance = balance;
        this.leaveTypesId = leaveTypesId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getLeaveBeginningBalanceId() {
        return leaveBeginningBalanceId;
    }

    public void setLeaveBeginningBalanceId(Long leaveBeginningBalanceId) {
        this.leaveBeginningBalanceId = leaveBeginningBalanceId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getAsOfDate() {
        return asOfDate;
    }

    public void setAsOfDate(LocalDate asOfDate) {
        this.asOfDate = asOfDate;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Long getLeaveTypesId() {
        return leaveTypesId;
    }

    public void setLeaveTypesId(Long leaveTypesId) {
        this.leaveTypesId = leaveTypesId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
