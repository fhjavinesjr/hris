package com.administrative.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PagIbigContributionDTO implements Serializable {

    private Long pagIbigContributionId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    private LocalDateTime effectivityDate;

    private Double employeeShare;
    private Double employerShare;

    public PagIbigContributionDTO() {}

    public PagIbigContributionDTO(Long pagIbigContributionId, LocalDateTime effectivityDate,
                                  Double employeeShare, Double employerShare) {
        this.pagIbigContributionId = pagIbigContributionId;
        this.effectivityDate = effectivityDate;
        this.employeeShare = employeeShare;
        this.employerShare = employerShare;
    }

    public Long getPagIbigContributionId() { return pagIbigContributionId; }
    public void setPagIbigContributionId(Long pagIbigContributionId) { this.pagIbigContributionId = pagIbigContributionId; }

    public LocalDateTime getEffectivityDate() { return effectivityDate; }
    public void setEffectivityDate(LocalDateTime effectivityDate) { this.effectivityDate = effectivityDate; }

    public Double getEmployeeShare() { return employeeShare; }
    public void setEmployeeShare(Double employeeShare) { this.employeeShare = employeeShare; }

    public Double getEmployerShare() { return employerShare; }
    public void setEmployerShare(Double employerShare) { this.employerShare = employerShare; }
}
