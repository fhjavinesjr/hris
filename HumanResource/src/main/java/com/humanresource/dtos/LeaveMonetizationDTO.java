package com.humanresource.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveMonetizationDTO implements Serializable {

    private Long leaveMonetizationId;

    @NotNull(message = "employeeId is mandatory")
    private Long employeeId;

    // Enriched employee info (populated by service when returning responses)
    private String employeeNo;
    private String employeeName;

    @NotNull(message = "dateFiled is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateFiled;

    private Double noOfDaysSL;
    private Double noOfDaysVL;
    private Double totalDays;

    private Double slBalanceBefore;
    private Double vlBalanceBefore;
    private Double slBalanceAfter;
    private Double vlBalanceAfter;

    private String reason;

    // Level 1 — Recommending Authority
    private String recommendationStatus;
    private Long recommendedById;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recommendedAt;

    private String recommendationRemarks;

    // Level 2 — Final Approving Authority
    private String approvalStatus;
    private Long approvedById;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate approvedAt;

    private String approvalRemarks;

    // Payroll hook — true after payroll engine picks this up as a special earning
    private Boolean payrollIncluded;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public LeaveMonetizationDTO() {
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public Long getLeaveMonetizationId() { return leaveMonetizationId; }
    public void setLeaveMonetizationId(Long leaveMonetizationId) { this.leaveMonetizationId = leaveMonetizationId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

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
