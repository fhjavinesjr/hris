package com.humanresource.saln;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="saln_correction", indexes=@Index(name="ix_saln_correction_saln", columnList="saln_id,resolved"))
public class SalnCorrection {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="correction_id") private Long id;
    @Column(name="saln_id", nullable=false) private Long salnId;
    @Column(name="version_no", nullable=false) private int versionNo;
    @Column(name="section_name", length=150) private String section;
    @Column(name="field_name", length=150) private String field;
    @Column(name="message", nullable=false, length=2000) private String message;
    @Column(name="requested_by", nullable=false, length=100) private String requestedBy;
    @Column(name="requested_at", nullable=false) private LocalDateTime requestedAt;
    @Column(name="resolved", nullable=false) private boolean resolved;
    @Column(name="resolved_at") private LocalDateTime resolvedAt;
    protected SalnCorrection() {}
    public SalnCorrection(Long salnId,int versionNo,String section,String field,String message,String requestedBy){this.salnId=salnId;this.versionNo=versionNo;this.section=section;this.field=field;this.message=message;this.requestedBy=requestedBy;this.requestedAt=LocalDateTime.now();}
    public void resolve(){resolved=true;resolvedAt=LocalDateTime.now();}
    public Long getId(){return id;} public Long getSalnId(){return salnId;} public int getVersionNo(){return versionNo;} public String getSection(){return section;} public String getField(){return field;} public String getMessage(){return message;} public String getRequestedBy(){return requestedBy;} public LocalDateTime getRequestedAt(){return requestedAt;} public boolean isResolved(){return resolved;} public LocalDateTime getResolvedAt(){return resolvedAt;}
}
