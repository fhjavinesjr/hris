package com.administrative.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "gsiscontribution")
public class GSISContribution implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gsisContributionId")
    private Long gsisContributionId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "effectivityDate")
    private LocalDateTime effectivityDate;

    @Column(name = "employerSharePercentage")
    private String employerSharePercentage;

    @Column(name = "employeeSharePercentage")
    private String employeeSharePercentage;

    public GSISContribution() {

    }

    public GSISContribution(Long gsisContributionId, LocalDateTime effectivityDate, String employerSharePercentage, String employeeSharePercentage) {
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