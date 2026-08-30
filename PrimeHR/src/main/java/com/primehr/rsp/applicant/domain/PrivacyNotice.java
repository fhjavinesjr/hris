package com.primehr.rsp.applicant.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity @Table(name="rsp_privacy_notice",uniqueConstraints=@UniqueConstraint(name="uk_rsp_privacy_version",columnNames={"agency_id","definition_version"}))
public class PrivacyNotice extends RspAuditedEntity {
    public enum Status { DRAFT, ACTIVE, ARCHIVED }
    @Column(nullable=false,length=200) private String title;
    @Column(nullable=false,length=8000) private String body;
    @Column(name="retention_summary",nullable=false,length=1000) private String retentionSummary;
    @Column(name="definition_version",nullable=false) private int definitionVersion;
    @Column(name="effective_from",nullable=false) private LocalDate effectiveFrom;
    @Column(name="effective_to") private LocalDate effectiveTo;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    protected PrivacyNotice(){}
    public PrivacyNotice(String agency,String title,String body,String retention,int version,LocalDate from,LocalDate to,Status status){super(agency);this.title=requiredText(title,"title");this.body=requiredText(body,"body");retentionSummary=requiredText(retention,"retentionSummary");if(version<1)throw new IllegalArgumentException("definitionVersion must be positive");definitionVersion=version;effectiveFrom=java.util.Objects.requireNonNull(from);if(to!=null&&to.isBefore(from))throw new IllegalArgumentException("effectiveTo cannot precede effectiveFrom");effectiveTo=to;this.status=java.util.Objects.requireNonNull(status);}
    public boolean effective(LocalDate date){return status==Status.ACTIVE&&!effectiveFrom.isAfter(date)&&(effectiveTo==null||!effectiveTo.isBefore(date));}
    public String getTitle(){return title;} public String getBody(){return body;} public String getRetentionSummary(){return retentionSummary;} public int getDefinitionVersion(){return definitionVersion;} public LocalDate getEffectiveFrom(){return effectiveFrom;} public LocalDate getEffectiveTo(){return effectiveTo;} public Status getStatus(){return status;}
}
