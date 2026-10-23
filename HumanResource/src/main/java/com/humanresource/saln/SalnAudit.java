package com.humanresource.saln;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="saln_audit", indexes=@Index(name="ix_saln_audit_saln_time", columnList="saln_id,performed_at"))
public class SalnAudit {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="audit_id") private Long id;
    @Column(name="saln_id", nullable=false) private Long salnId;
    @Column(name="employee_id", nullable=false) private Long employeeId;
    @Column(name="action", nullable=false, length=80) private String action;
    @Column(name="performed_by", nullable=false, length=100) private String performedBy;
    @Column(name="performed_at", nullable=false) private LocalDateTime performedAt;
    @Enumerated(EnumType.STRING) @Column(name="old_status", length=24) private SalnTypes.Status oldStatus;
    @Enumerated(EnumType.STRING) @Column(name="new_status", length=24) private SalnTypes.Status newStatus;
    @Column(name="remarks", length=2000) private String remarks;
    @Column(name="version_no", nullable=false) private int versionNo;
    protected SalnAudit() {}
    public SalnAudit(Long salnId,Long employeeId,String action,String performedBy,SalnTypes.Status oldStatus,SalnTypes.Status newStatus,String remarks,int versionNo){this.salnId=salnId;this.employeeId=employeeId;this.action=action;this.performedBy=performedBy;this.performedAt=LocalDateTime.now();this.oldStatus=oldStatus;this.newStatus=newStatus;this.remarks=remarks;this.versionNo=versionNo;}
    public Long getId(){return id;} public Long getSalnId(){return salnId;} public Long getEmployeeId(){return employeeId;} public String getAction(){return action;} public String getPerformedBy(){return performedBy;} public LocalDateTime getPerformedAt(){return performedAt;} public SalnTypes.Status getOldStatus(){return oldStatus;} public SalnTypes.Status getNewStatus(){return newStatus;} public String getRemarks(){return remarks;} public int getVersionNo(){return versionNo;}
}
