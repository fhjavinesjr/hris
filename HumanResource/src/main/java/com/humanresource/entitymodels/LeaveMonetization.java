package com.humanresource.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_monetization")
public class LeaveMonetization implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leaveMonetizationId")
    private Long leaveMonetizationId;

    @NotNull(message = "employeeId is mandatory")
    @Column(name = "employeeId", nullable = false)
    private Long employeeId;

    @NotNull(message = "dateFiled is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "dateFiled", nullable = false)
    private LocalDate dateFiled;

    // SL days to be monetized (VL is deducted first per CSC rule)
    @Column(name = "noOfDaysSL")
    private Double noOfDaysSL = 0.0;

    // VL days to be monetized (deducted first per CSC rule)
    @Column(name = "noOfDaysVL")
    private Double noOfDaysVL = 0.0;

    // Total days = noOfDaysSL + noOfDaysVL (must be >= 10 per CSC MC No. 41 s. 1998)
    @Column(name = "totalDays")
    private Double totalDays = 0.0;

    // SL balance at time of filing (snapshot from latest LeaveInformation)
    @Column(name = "slBalanceBefore")
    private Double slBalanceBefore;

    // VL balance at time of filing (snapshot from latest LeaveInformation)
    @Column(name = "vlBalanceBefore")
    private Double vlBalanceBefore;

    // SL balance after deduction — computed at approval time
    @Column(name = "slBalanceAfter")
    private Double slBalanceAfter;

    // VL balance after deduction — computed at approval time
    @Column(name = "vlBalanceAfter")
    private Double vlBalanceAfter;

    // Employee's reason/justification for monetizing leave
    @Column(name = "reason", length = 500)
    private String reason;

    // Two-level approval: Level 1 — Recommending Authority
    @Column(name = "recommendationStatus", length = 50)
    private String recommendationStatus = "Pending";

    @Column(name = "recommendedById")
    private Long recommendedById;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "recommendedAt")
    private LocalDateTime recommendedAt;

    @Column(name = "recommendationRemarks", length = 500)
    private String recommendationRemarks;

    // Two-level approval: Level 2 — Final Approving Authority
    @Column(name = "approvalStatus", length = 50)
    private String approvalStatus = "Pending";

    @Column(name = "approvedById")
    private Long approvedById;

    // Date of final approval — used by LeaveProcessServiceImpl to link deduction to a cutoff period
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "approvedAt")
    private LocalDate approvedAt;

    @Column(name = "approvalRemarks", length = 500)
    private String approvalRemarks;

    // Flag: set to true by the payroll engine after picking up this monetization as a special earning.
    // Prevents double-pick-up in subsequent payroll runs.
    @Column(name = "payrollIncluded", nullable = false)
    private Boolean payrollIncluded = false;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    public LeaveMonetization() {
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public Long getLeaveMonetizationId() { return leaveMonetizationId; }
    public void setLeaveMonetizationId(Long leaveMonetizationId) { this.leaveMonetizationId = leaveMonetizationId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public LocalDate getDateFiled() { return dateFiled; }
    public void setDateFiled(LocalDate dateFiled) { this.dateFiled = dateFiled; }

    public Double getNoOfDaysSL() { return noOfDaysSL; }
    public void setNoOfDaysSL(Double noOfDaysSL) { this.noOfDaysSL = noOfDaysSL; }

    public Double getNoOfDaysVL() { return noOfDaysVL; }
    public void setNoOfDaysVL(Double noOfDaysVL) { this.noOfDaysVL = noOfDaysVL; }

    public Double getTotalDays() { return totalDays; }
    public void setTotalDays(Double totalDays) { this.totalDays = totalDays; }

    public Double getSlBalanceBefore() { return slBalanceBefore; }
    public void setSlBalanceBefore(Double slBalanceBefore) { this.slBalanceBefore = slBalanceBefore; }

    public Double getVlBalanceBefore() { return vlBalanceBefore; }
    public void setVlBalanceBefore(Double vlBalanceBefore) { this.vlBalanceBefore = vlBalanceBefore; }

    public Double getSlBalanceAfter() { return slBalanceAfter; }
    public void setSlBalanceAfter(Double slBalanceAfter) { this.slBalanceAfter = slBalanceAfter; }

    public Double getVlBalanceAfter() { return vlBalanceAfter; }
    public void setVlBalanceAfter(Double vlBalanceAfter) { this.vlBalanceAfter = vlBalanceAfter; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getRecommendationStatus() { return recommendationStatus; }
    public void setRecommendationStatus(String recommendationStatus) { this.recommendationStatus = recommendationStatus; }

    public Long getRecommendedById() { return recommendedById; }
    public void setRecommendedById(Long recommendedById) { this.recommendedById = recommendedById; }

    public LocalDateTime getRecommendedAt() { return recommendedAt; }
    public void setRecommendedAt(LocalDateTime recommendedAt) { this.recommendedAt = recommendedAt; }

    public String getRecommendationRemarks() { return recommendationRemarks; }
    public void setRecommendationRemarks(String recommendationRemarks) { this.recommendationRemarks = recommendationRemarks; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public Long getApprovedById() { return approvedById; }
    public void setApprovedById(Long approvedById) { this.approvedById = approvedById; }

    public LocalDate getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDate approvedAt) { this.approvedAt = approvedAt; }

    public String getApprovalRemarks() { return approvalRemarks; }
    public void setApprovalRemarks(String approvalRemarks) { this.approvalRemarks = approvalRemarks; }

    public Boolean getPayrollIncluded() { return payrollIncluded; }
    public void setPayrollIncluded(Boolean payrollIncluded) { this.payrollIncluded = payrollIncluded; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
