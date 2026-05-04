package com.humanresource.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * OvertimeRequest — Step 1 of the CSC MC No. 6, s. 2012 Compensatory Overtime Credit flow.
 *
 * Before rendering overtime or holiday duty, the supervisor/employee must file and receive
 * approval for an Overtime/Holiday Duty Order.  Once approved, the employee renders the work
 * and then files a COC (CompensatoryOvertimeCredit) referencing this approved request.
 *
 * Flow: File OT Request → Head-of-Agency Approves → Employee renders work → File COC (Step 2)
 */
@Entity
@Table(name = "overtime_request")
public class OvertimeRequest implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "overtimeRequestId")
    private Long overtimeRequestId;

    @NotNull(message = "employeeId is mandatory")
    @Column(name = "employeeId", nullable = false)
    private Long employeeId;

    @NotNull(message = "dateFiled is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "dateFiled", nullable = false)
    private LocalDate dateFiled;

    /** Planned start of overtime / holiday duty work. */
    @NotNull(message = "dateTimeFrom is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "dateTimeFrom", nullable = false)
    private LocalDateTime dateTimeFrom;

    /** Planned end of overtime / holiday duty work. */
    @NotNull(message = "dateTimeTo is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "dateTimeTo", nullable = false)
    private LocalDateTime dateTimeTo;

    /**
     * Computed and stored at creation: duration between dateTimeFrom and dateTimeTo in hours.
     * This becomes the ceiling for COC hoursWorked when the employee files COC for this OT order.
     */
    @NotNull(message = "totalHours is mandatory")
    @Column(name = "totalHours", nullable = false)
    private Double totalHours;

    /** Purpose / justification for overtime work. */
    @Column(name = "purpose", length = 500)
    private String purpose;

    /** Status: Pending, Approved, Disapproved */
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

    @Column(name = "recommendationStatus", length = 50)
    private String recommendationStatus;

    @Column(name = "recommendedById")
    private Long recommendedById;

    @Column(name = "recommendationRemarks", length = 300)
    private String recommendationRemarks;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public OvertimeRequest() {}

    public Long getOvertimeRequestId() { return overtimeRequestId; }
    public void setOvertimeRequestId(Long overtimeRequestId) { this.overtimeRequestId = overtimeRequestId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public LocalDate getDateFiled() { return dateFiled; }
    public void setDateFiled(LocalDate dateFiled) { this.dateFiled = dateFiled; }

    public LocalDateTime getDateTimeFrom() { return dateTimeFrom; }
    public void setDateTimeFrom(LocalDateTime dateTimeFrom) { this.dateTimeFrom = dateTimeFrom; }

    public LocalDateTime getDateTimeTo() { return dateTimeTo; }
    public void setDateTimeTo(LocalDateTime dateTimeTo) { this.dateTimeTo = dateTimeTo; }

    public Double getTotalHours() { return totalHours; }
    public void setTotalHours(Double totalHours) { this.totalHours = totalHours; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getApprovedById() { return approvedById; }
    public void setApprovedById(Long approvedById) { this.approvedById = approvedById; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getApprovalRemarks() { return approvalRemarks; }
    public void setApprovalRemarks(String approvalRemarks) { this.approvalRemarks = approvalRemarks; }

    public String getRecommendationStatus() { return recommendationStatus; }
    public void setRecommendationStatus(String recommendationStatus) { this.recommendationStatus = recommendationStatus; }

    public Long getRecommendedById() { return recommendedById; }
    public void setRecommendedById(Long recommendedById) { this.recommendedById = recommendedById; }

    public String getRecommendationRemarks() { return recommendationRemarks; }
    public void setRecommendationRemarks(String recommendationRemarks) { this.recommendationRemarks = recommendationRemarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
