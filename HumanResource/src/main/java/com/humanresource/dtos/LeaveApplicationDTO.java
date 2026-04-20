package com.humanresource.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;

public class LeaveApplicationDTO implements Serializable {

    private Long leaveApplicationId;

    @NotNull(message = "employeeId is mandatory")
    private Long employeeId;

    @NotNull(message = "dateFiled is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateFiled;

    @NotNull(message = "leaveType is mandatory")
    private String leaveType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private Double noOfDays;

    private String commutation;

    private String details;

    @NotNull(message = "status is mandatory")
    private String status;

    private Long recommendingApprovalById;

    private Long authorizedOfficialId;

    private Long approvedById;

    private String recommendationStatus;

    private String recommendationMessage;

    private String approvedStatus;

    private String approvalMessage;

    private Boolean dueExigencyService;

    public LeaveApplicationDTO() {
    }

    public LeaveApplicationDTO(Long leaveApplicationId, Long employeeId, LocalDate dateFiled, String leaveType,
                               LocalDate startDate, LocalDate endDate, Double noOfDays, String commutation,
                               String details, String status, Long recommendingApprovalById,
                               Long authorizedOfficialId, Long approvedById,
                               String recommendationStatus, String recommendationMessage,
                               String approvedStatus, String approvalMessage, Boolean dueExigencyService) {
        this.leaveApplicationId = leaveApplicationId;
        this.employeeId = employeeId;
        this.dateFiled = dateFiled;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.noOfDays = noOfDays;
        this.commutation = commutation;
        this.details = details;
        this.status = status;
        this.recommendingApprovalById = recommendingApprovalById;
        this.authorizedOfficialId = authorizedOfficialId;
        this.approvedById = approvedById;
        this.recommendationStatus = recommendationStatus;
        this.recommendationMessage = recommendationMessage;
        this.approvedStatus = approvedStatus;
        this.approvalMessage = approvalMessage;
        this.dueExigencyService = dueExigencyService;
    }

    public Long getLeaveApplicationId() {
        return leaveApplicationId;
    }

    public void setLeaveApplicationId(Long leaveApplicationId) {
        this.leaveApplicationId = leaveApplicationId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getDateFiled() {
        return dateFiled;
    }

    public void setDateFiled(LocalDate dateFiled) {
        this.dateFiled = dateFiled;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Double getNoOfDays() {
        return noOfDays;
    }

    public void setNoOfDays(Double noOfDays) {
        this.noOfDays = noOfDays;
    }

    public String getCommutation() {
        return commutation;
    }

    public void setCommutation(String commutation) {
        this.commutation = commutation;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getRecommendingApprovalById() {
        return recommendingApprovalById;
    }

    public void setRecommendingApprovalById(Long recommendingApprovalById) {
        this.recommendingApprovalById = recommendingApprovalById;
    }

    public Long getAuthorizedOfficialId() {
        return authorizedOfficialId;
    }

    public void setAuthorizedOfficialId(Long authorizedOfficialId) {
        this.authorizedOfficialId = authorizedOfficialId;
    }

    public Long getApprovedById() {
        return approvedById;
    }

    public void setApprovedById(Long approvedById) {
        this.approvedById = approvedById;
    }

    public String getRecommendationStatus() {
        return recommendationStatus;
    }

    public void setRecommendationStatus(String recommendationStatus) {
        this.recommendationStatus = recommendationStatus;
    }

    public String getRecommendationMessage() {
        return recommendationMessage;
    }

    public void setRecommendationMessage(String recommendationMessage) {
        this.recommendationMessage = recommendationMessage;
    }

    public String getApprovedStatus() {
        return approvedStatus;
    }

    public void setApprovedStatus(String approvedStatus) {
        this.approvedStatus = approvedStatus;
    }

    public String getApprovalMessage() {
        return approvalMessage;
    }

    public void setApprovalMessage(String approvalMessage) {
        this.approvalMessage = approvalMessage;
    }

    public Boolean getDueExigencyService() {
        return dueExigencyService;
    }

    public void setDueExigencyService(Boolean dueExigencyService) {
        this.dueExigencyService = dueExigencyService;
    }
}
