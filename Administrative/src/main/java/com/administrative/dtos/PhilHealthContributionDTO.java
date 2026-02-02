package com.administrative.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PhilHealthContributionDTO implements Serializable {

    private Long philhealthContributionId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime effectivityDate;
    private String ratePercentage;
    private String monthlySalaryRangeFrom;
    private String monthlySalaryRangeTo;
    private String personalShareFrom;
    private String personalShareTo;
    private String employerShareFrom;
    private String employerShareTo;

    public PhilHealthContributionDTO() {

    }

    public PhilHealthContributionDTO(Long philhealthContributionId, LocalDateTime effectivityDate, String ratePercentage, String monthlySalaryRangeFrom, String monthlySalaryRangeTo, String personalShareFrom, String personalShareTo, String employerShareFrom, String employerShareTo) {
        this.philhealthContributionId = philhealthContributionId;
        this.effectivityDate = effectivityDate;
        this.ratePercentage = ratePercentage;
        this.monthlySalaryRangeFrom = monthlySalaryRangeFrom;
        this.monthlySalaryRangeTo = monthlySalaryRangeTo;
        this.personalShareFrom = personalShareFrom;
        this.personalShareTo = personalShareTo;
        this.employerShareFrom = employerShareFrom;
        this.employerShareTo = employerShareTo;
    }

    public Long getPhilhealthContributionId() {
        return philhealthContributionId;
    }

    public void setPhilhealthContributionId(Long philhealthContributionId) {
        this.philhealthContributionId = philhealthContributionId;
    }

    public LocalDateTime getEffectivityDate() {
        return effectivityDate;
    }

    public void setEffectivityDate(LocalDateTime effectivityDate) {
        this.effectivityDate = effectivityDate;
    }

    public String getRatePercentage() {
        return ratePercentage;
    }

    public void setRatePercentage(String ratePercentage) {
        this.ratePercentage = ratePercentage;
    }

    public String getMonthlySalaryRangeFrom() {
        return monthlySalaryRangeFrom;
    }

    public void setMonthlySalaryRangeFrom(String monthlySalaryRangeFrom) {
        this.monthlySalaryRangeFrom = monthlySalaryRangeFrom;
    }

    public String getMonthlySalaryRangeTo() {
        return monthlySalaryRangeTo;
    }

    public void setMonthlySalaryRangeTo(String monthlySalaryRangeTo) {
        this.monthlySalaryRangeTo = monthlySalaryRangeTo;
    }

    public String getPersonalShareFrom() {
        return personalShareFrom;
    }

    public void setPersonalShareFrom(String personalShareFrom) {
        this.personalShareFrom = personalShareFrom;
    }

    public String getPersonalShareTo() {
        return personalShareTo;
    }

    public void setPersonalShareTo(String personalShareTo) {
        this.personalShareTo = personalShareTo;
    }

    public String getEmployerShareFrom() {
        return employerShareFrom;
    }

    public void setEmployerShareFrom(String employerShareFrom) {
        this.employerShareFrom = employerShareFrom;
    }

    public String getEmployerShareTo() {
        return employerShareTo;
    }

    public void setEmployerShareTo(String employerShareTo) {
        this.employerShareTo = employerShareTo;
    }
}