package com.humanresource.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OvertimeRequestDTO implements Serializable {

    private Long overtimeRequestId;
    private Long employeeId;
    private String groupRequestId;
    private Long filedByEmployeeId;
    private Long businessUnitId;
    private Boolean supervisorFiled;
    private List<Long> participantEmployeeIds;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateFiled;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTimeFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTimeTo;

    private Double totalHours;
    private String workType;
    private String dutyShiftCode;
    private String authorityReference;
    private Boolean emergencyPostFiling;
    private String emergencyJustification;
    private Integer breakMinutes;
    private Double netAuthorizedHours;
    private String purpose;
    private String expectedOutput;
    private String discrepancyRemarks;
    private LocalDateTime discrepancyReportedAt;
    private String groupDiscrepancySummary;
    private String status;
    private Long approvedById;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime approvedAt;

    private String approvalRemarks;

    private String recommendationStatus;
    private Long recommendedById;
    private String recommendationRemarks;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime updatedAt;

    public OvertimeRequestDTO() {}

    public Long getOvertimeRequestId() { return overtimeRequestId; }
    public void setOvertimeRequestId(Long overtimeRequestId) { this.overtimeRequestId = overtimeRequestId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getGroupRequestId() { return groupRequestId; }
    public void setGroupRequestId(String groupRequestId) { this.groupRequestId = groupRequestId; }
    public Long getFiledByEmployeeId() { return filedByEmployeeId; }
    public void setFiledByEmployeeId(Long filedByEmployeeId) { this.filedByEmployeeId = filedByEmployeeId; }
    public Long getBusinessUnitId() { return businessUnitId; }
    public void setBusinessUnitId(Long businessUnitId) { this.businessUnitId = businessUnitId; }
    public Boolean getSupervisorFiled() { return supervisorFiled; }
    public void setSupervisorFiled(Boolean supervisorFiled) { this.supervisorFiled = supervisorFiled; }
    public List<Long> getParticipantEmployeeIds() { return participantEmployeeIds; }
    public void setParticipantEmployeeIds(List<Long> participantEmployeeIds) { this.participantEmployeeIds = participantEmployeeIds; }

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
    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    public String getDiscrepancyRemarks() { return discrepancyRemarks; }
    public void setDiscrepancyRemarks(String discrepancyRemarks) { this.discrepancyRemarks = discrepancyRemarks; }
    public LocalDateTime getDiscrepancyReportedAt() { return discrepancyReportedAt; }
    public void setDiscrepancyReportedAt(LocalDateTime discrepancyReportedAt) { this.discrepancyReportedAt = discrepancyReportedAt; }
    public String getGroupDiscrepancySummary() { return groupDiscrepancySummary; }
    public void setGroupDiscrepancySummary(String groupDiscrepancySummary) { this.groupDiscrepancySummary = groupDiscrepancySummary; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getApprovedById() { return approvedById; }
    public void setApprovedById(Long approvedById) { this.approvedById = approvedById; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getApprovalRemarks() { return approvalRemarks; }
    public void setApprovalRemarks(String approvalRemarks) { this.approvalRemarks = approvalRemarks; }

    public String getRecommendationStatus() { return recommendationStatus; }
    public void setRecommendationStatus(String s) { this.recommendationStatus = s; }

    public Long getRecommendedById() { return recommendedById; }
    public void setRecommendedById(Long id) { this.recommendedById = id; }

    public String getRecommendationRemarks() { return recommendationRemarks; }
    public void setRecommendationRemarks(String s) { this.recommendationRemarks = s; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
    public String getDutyShiftCode() { return dutyShiftCode; }
    public void setDutyShiftCode(String dutyShiftCode) { this.dutyShiftCode = dutyShiftCode; }
    public String getAuthorityReference() { return authorityReference; }
    public void setAuthorityReference(String authorityReference) { this.authorityReference = authorityReference; }
    public Boolean getEmergencyPostFiling() { return emergencyPostFiling; }
    public void setEmergencyPostFiling(Boolean emergencyPostFiling) { this.emergencyPostFiling = emergencyPostFiling; }
    public String getEmergencyJustification() { return emergencyJustification; }
    public void setEmergencyJustification(String emergencyJustification) { this.emergencyJustification = emergencyJustification; }
    public Integer getBreakMinutes() { return breakMinutes; }
    public void setBreakMinutes(Integer breakMinutes) { this.breakMinutes = breakMinutes; }
    public Double getNetAuthorizedHours() { return netAuthorizedHours; }
    public void setNetAuthorizedHours(Double netAuthorizedHours) { this.netAuthorizedHours = netAuthorizedHours; }

}
