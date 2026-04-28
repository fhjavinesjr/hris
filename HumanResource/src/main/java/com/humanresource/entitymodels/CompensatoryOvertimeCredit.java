package com.humanresource.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * COC = Compensatory Overtime Credit
 * An employee files a COC after working on a holiday or doing overtime.
 * Once approved by the supervisor, the credited hours are added to the employee's COC balance.
 * COC balance is then the source fund for filing a CTO (Compensatory Time Off).
 *
 * Flow: Holiday/Overtime Work → File COC → Supervisor Approves → COC Balance increases
 */
@Entity
@Table(name = "compensatory_overtime_credit")
public class CompensatoryOvertimeCredit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cocId")
    private Long cocId;

    @NotNull(message = "employeeId is mandatory")
    @Column(name = "employeeId", nullable = false)
    private Long employeeId;

    @NotNull(message = "dateFiled is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "dateFiled", nullable = false)
    private LocalDate dateFiled;

    @NotNull(message = "dateWorked is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "dateWorked", nullable = false)
    private LocalDate dateWorked;

    @NotNull(message = "hoursWorked is mandatory")
    @Column(name = "hoursWorked", nullable = false)
    private Double hoursWorked;

    @Column(name = "reason", length = 500)
    private String reason;

    // Nature of extra work: HOLIDAY_DUTY, OVERTIME, etc.
    @Column(name = "workType", length = 100)
    private String workType;

    // Status: Pending, Approved, Disapproved
    @NotNull(message = "status is mandatory")
    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "approvedById")
    private Long approvedById;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "approvedAt")
    private LocalDateTime approvedAt;

    @Column(name = "approvalRemarks", length = 300)
    private String approvalRemarks;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public CompensatoryOvertimeCredit() {
    }

    public Long getCocId() { return cocId; }
    public void setCocId(Long cocId) { this.cocId = cocId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public LocalDate getDateFiled() { return dateFiled; }
    public void setDateFiled(LocalDate dateFiled) { this.dateFiled = dateFiled; }

    public LocalDate getDateWorked() { return dateWorked; }
    public void setDateWorked(LocalDate dateWorked) { this.dateWorked = dateWorked; }

    public Double getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(Double hoursWorked) { this.hoursWorked = hoursWorked; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }

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
}
