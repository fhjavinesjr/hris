package com.humanresource.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CompensatoryTimeOffDTO implements Serializable {

    private Long ctoId;

    @NotNull(message = "employeeId is mandatory")
    private Long employeeId;

    @NotNull(message = "dateFiled is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateFiled;

    @NotNull(message = "dateOfOffset is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfOffset;

    @NotNull(message = "hoursUsed is mandatory")
    private Double hoursUsed;

    private Double cocBalanceAtFiling;

    private String reason;

    @NotNull(message = "status is mandatory")
    private String status;

    private Long approvedById;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime approvedAt;

    private String approvalRemarks;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime updatedAt;

    // Computed: current COC balance after this and all previous transactions
    private Double currentBalance;

    public CompensatoryTimeOffDTO() {
    }

    public Long getCtoId() { return ctoId; }
    public void setCtoId(Long ctoId) { this.ctoId = ctoId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public LocalDate getDateFiled() { return dateFiled; }
    public void setDateFiled(LocalDate dateFiled) { this.dateFiled = dateFiled; }

    public LocalDate getDateOfOffset() { return dateOfOffset; }
    public void setDateOfOffset(LocalDate dateOfOffset) { this.dateOfOffset = dateOfOffset; }

    public Double getHoursUsed() { return hoursUsed; }
    public void setHoursUsed(Double hoursUsed) { this.hoursUsed = hoursUsed; }

    public Double getCocBalanceAtFiling() { return cocBalanceAtFiling; }
    public void setCocBalanceAtFiling(Double cocBalanceAtFiling) { this.cocBalanceAtFiling = cocBalanceAtFiling; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getApprovedById() { return approvedById; }
    public void setApprovedById(Long approvedById) { this.approvedById = approvedById; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getApprovalRemarks() { return approvalRemarks; }
    public void setApprovalRemarks(String approvalRemarks) { this.approvalRemarks = approvalRemarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Double getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(Double currentBalance) { this.currentBalance = currentBalance; }
}
