package com.humanresource.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "leave_application")
public class LeaveApplication implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leaveApplicationId")
    private Long leaveApplicationId;

    @NotNull(message = "employeeId is mandatory")
    @Column(name = "employeeId", nullable = false)
    private Long employeeId;

    @NotNull(message = "dateFiled is mandatory")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "dateFiled", nullable = false)
    private LocalDate dateFiled;

    @NotNull(message = "leaveType is mandatory")
    @Column(name = "leaveType", length = 100, nullable = false)
    private String leaveType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "startDate")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "endDate")
    private LocalDate endDate;

    @Column(name = "noOfDays")
    private Double noOfDays;

    @Column(name = "commutation", length = 50)
    private String commutation;

    @Column(name = "details", length = 500)
    private String details;

    @NotNull(message = "status is mandatory")
    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "recommendingApprovalById")
    private Long recommendingApprovalById;

    @Column(name = "authorizedOfficialId")
    private Long authorizedOfficialId;

    @Column(name = "approvedById")
    private Long approvedById;

    @Column(name = "recommendationStatus", length = 50)
    private String recommendationStatus;

    @Column(name = "recommendationMessage", length = 500)
    private String recommendationMessage;

    @Column(name = "approvedStatus", length = 50)
    private String approvedStatus;

    @Column(name = "approvalMessage", length = 500)
    private String approvalMessage;

    @Column(name = "dueExigencyService")
    private Boolean dueExigencyService;

    public LeaveApplication() {
    }

    public LeaveApplication(Long leaveApplicationId, Long employeeId, LocalDate dateFiled, String leaveType,
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
