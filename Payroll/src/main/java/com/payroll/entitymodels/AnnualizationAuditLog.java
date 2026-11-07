package com.payroll.entitymodels;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "annualization_audit_log",
        indexes = @Index(name = "idx_annualization_audit_lookup", columnList = "employeeNo,taxYear,performedAt"))
public class AnnualizationAuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 50) private String employeeNo;
    @Column(nullable = false) private Integer taxYear;
    @Column(nullable = false, length = 40) private String action;
    @Column(length = 20) private String oldStatus;
    @Column(nullable = false, length = 20) private String newStatus;
    @Column(length = 500) private String remarks;
    @Column(nullable = false, length = 100) private String performedBy;
    @Column(nullable = false) private LocalDateTime performedAt;
    public Long getId(){return id;} public void setId(Long v){id=v;} public String getEmployeeNo(){return employeeNo;} public void setEmployeeNo(String v){employeeNo=v;}
    public Integer getTaxYear(){return taxYear;} public void setTaxYear(Integer v){taxYear=v;} public String getAction(){return action;} public void setAction(String v){action=v;}
    public String getOldStatus(){return oldStatus;} public void setOldStatus(String v){oldStatus=v;} public String getNewStatus(){return newStatus;} public void setNewStatus(String v){newStatus=v;}
    public String getRemarks(){return remarks;} public void setRemarks(String v){remarks=v;} public String getPerformedBy(){return performedBy;} public void setPerformedBy(String v){performedBy=v;}
    public LocalDateTime getPerformedAt(){return performedAt;} public void setPerformedAt(LocalDateTime v){performedAt=v;}
}
