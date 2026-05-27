package com.administrative.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Pag-IBIG (HDMF) mandatory contribution table.
 * Mirrors the old HRIS PagIbig model (csShareEe / csShareEr).
 *
 * The Payroll batch reads the latest record (by effectivityDate) to determine
 * the mandatory employee and employer contribution amounts per salary period.
 */
@Entity
@Table(name = "pagibig_contribution")
public class PagIbigContribution implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pagIbigContributionId")
    private Long pagIbigContributionId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy HH:mm:ss")
    @Column(name = "effectivityDate")
    private LocalDateTime effectivityDate;

    /** Employee mandatory share (csShareEe). Typically ₱100. */
    @Column(name = "employeeShare")
    private Double employeeShare;

    /** Employer share (csShareEr). Typically ₱100. */
    @Column(name = "employerShare")
    private Double employerShare;

    public PagIbigContribution() {}

    public PagIbigContribution(Long pagIbigContributionId, LocalDateTime effectivityDate,
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
