package com.administrative.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "hazardpay")
public class HazardPay implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hazardPayId")
    private Long hazardPayId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "effectivityDate")
    private LocalDateTime effectivityDate;

    @Column(name = "salaryGrade")
    private String salaryGrade;

    @Column(name = "basicPayPercentage")
    private String basicPayPercentage;

    public HazardPay() {

    }

    public HazardPay(Long hazardPayId, LocalDateTime effectivityDate, String salaryGrade, String basicPayPercentage) {
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