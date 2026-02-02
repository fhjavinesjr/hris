package com.administrative.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;

import java.io.Serializable;
import java.time.LocalDateTime;

public class GSISContributionDTO implements Serializable {

    private Long gsisContributionId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime effectivityDate;
    private String employerSharePercentage;
    private String employeeSharePercentage;

    public GSISContributionDTO() {

    }

    public GSISContributionDTO(Long gsisContributionId, LocalDateTime effectivityDate, String employerSharePercentage, String employeeSharePercentage) {
        this.gsisContributionId = gsisContributionId;
        this.effectivityDate = effectivityDate;
        this.employerSharePercentage = employerSharePercentage;
        this.employeeSharePercentage = employeeSharePercentage;
    }

    public Long getGsisContributionId() {
        return gsisContributionId;
    }

    public void setGsisContributionId(Long gsisContributionId) {
        this.gsisContributionId = gsisContributionId;
    }

    public LocalDateTime getEffectivityDate() {
        return effectivityDate;
    }

    public void setEffectivityDate(LocalDateTime effectivityDate) {
        this.effectivityDate = effectivityDate;
    }

    public String getEmployerSharePercentage() {
        return employerSharePercentage;
    }

    public void setEmployerSharePercentage(String employerSharePercentage) {
        this.employerSharePercentage = employerSharePercentage;
    }

    public String getEmployeeSharePercentage() {
        return employeeSharePercentage;
    }

    public void setEmployeeSharePercentage(String employeeSharePercentage) {
        this.employeeSharePercentage = employeeSharePercentage;
    }
}