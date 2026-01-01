package com.humanresource.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;

import java.io.Serializable;
import java.time.LocalDateTime;

public class WorkExperienceDTO implements Serializable {

    private Long workExperienceId;

    private Long personalDataId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime fromDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime toDate;

    private String positionTitle;

    private String agencyName;

    private Double monthlySalary;

    private Long payGrade;

    private String workStatus;

    private String boolGovernmentService;

    public WorkExperienceDTO() {

    }

    public WorkExperienceDTO(Long workExperienceId, Long personalDataId, LocalDateTime fromDate, LocalDateTime toDate, String positionTitle, String agencyName, Double monthlySalary, Long payGrade, String workStatus, String boolGovernmentService) {
        this.workExperienceId = workExperienceId;
        this.personalDataId = personalDataId;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.positionTitle = positionTitle;
        this.agencyName = agencyName;
        this.monthlySalary = monthlySalary;
        this.payGrade = payGrade;
        this.workStatus = workStatus;
        this.boolGovernmentService = boolGovernmentService;
    }

    public Long getWorkExperienceId() {
        return workExperienceId;
    }

    public void setWorkExperienceId(Long workExperienceId) {
        this.workExperienceId = workExperienceId;
    }

    public Long getPersonalDataId() {
        return personalDataId;
    }

    public void setPersonalDataId(Long personalDataId) {
        this.personalDataId = personalDataId;
    }

    public LocalDateTime getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDateTime fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDateTime getToDate() {
        return toDate;
    }

    public void setToDate(LocalDateTime toDate) {
        this.toDate = toDate;
    }

    public String getPositionTitle() {
        return positionTitle;
    }

    public void setPositionTitle(String positionTitle) {
        this.positionTitle = positionTitle;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    public Double getMonthlySalary() {
        return monthlySalary;
    }

    public void setMonthlySalary(Double monthlySalary) {
        this.monthlySalary = monthlySalary;
    }

    public Long getPayGrade() {
        return payGrade;
    }

    public void setPayGrade(Long payGrade) {
        this.payGrade = payGrade;
    }

    public String getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(String workStatus) {
        this.workStatus = workStatus;
    }

    public String getBoolGovernmentService() {
        return boolGovernmentService;
    }

    public void setBoolGovernmentService(String boolGovernmentService) {
        this.boolGovernmentService = boolGovernmentService;
    }
}