package com.administrative.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "philhealthcontribution")
public class PhilHealthContribution implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "philhealthContributionId")
    private Long philhealthContributionId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "effectivityDate")
    private LocalDateTime effectivityDate;

    @Column(name = "ratePercentage")
    private String ratePercentage;

    @Column(name = "monthlySalaryRangeFrom")
    private String monthlySalaryRangeFrom;

    @Column(name = "monthlySalaryRangeTo")
    private String monthlySalaryRangeTo;

    @Column(name = "personalShareFrom")
    private String personalShareFrom;

    @Column(name = "personalShareTo")
    private String personalShareTo;

    @Column(name = "employerShareFrom")
    private String employerShareFrom;

    @Column(name = "employerShareTo")
    private String employerShareTo;

    public PhilHealthContribution() {

    }

    public PhilHealthContribution(Long philhealthContributionId, LocalDateTime effectivityDate, String ratePercentage, String monthlySalaryRangeFrom, String monthlySalaryRangeTo, String personalShareFrom, String personalShareTo, String employerShareFrom, String employerShareTo) {
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