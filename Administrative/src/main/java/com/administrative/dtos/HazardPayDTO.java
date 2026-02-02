package com.administrative.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;

import java.io.Serializable;
import java.time.LocalDateTime;

public class HazardPayDTO implements Serializable {

    private Long hazardPayId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime effectivityDate;
    private String salaryGrade;
    private String basicPayPercentage;

    public HazardPayDTO() {

    }

    public HazardPayDTO(Long hazardPayId, LocalDateTime effectivityDate, String salaryGrade, String basicPayPercentage) {
        this.hazardPayId = hazardPayId;
        this.effectivityDate = effectivityDate;
        this.salaryGrade = salaryGrade;
        this.basicPayPercentage = basicPayPercentage;
    }

    public Long getHazardPayId() {
        return hazardPayId;
    }

    public void setHazardPayId(Long hazardPayId) {
        this.hazardPayId = hazardPayId;
    }

    public LocalDateTime getEffectivityDate() {
        return effectivityDate;
    }

    public void setEffectivityDate(LocalDateTime effectivityDate) {
        this.effectivityDate = effectivityDate;
    }

    public String getSalaryGrade() {
        return salaryGrade;
    }

    public void setSalaryGrade(String salaryGrade) {
        this.salaryGrade = salaryGrade;
    }

    public String getBasicPayPercentage() {
        return basicPayPercentage;
    }

    public void setBasicPayPercentage(String basicPayPercentage) {
        this.basicPayPercentage = basicPayPercentage;
    }
}