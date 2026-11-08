package com.primehr.operations.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity @Table(name="primehr_retention_policy_version")
public class RetentionPolicyVersion extends RspAuditedEntity {
    @Column(nullable=false,length=100) private String code;
    @Column(name="policy_version",nullable=false) private int policyVersion;
    @Column(name="retention_tag",nullable=false,length=100) private String retentionTag;
    @Column(name="retention_days",nullable=false) private int retentionDays;
    @Column(name="effective_from",nullable=false) private LocalDate effectiveFrom;
    @Column(name="effective_to") private LocalDate effectiveTo;
    @Column(nullable=false,length=1000) private String basis;
    @Column(nullable=false) private boolean published;
    protected RetentionPolicyVersion() {}
    public RetentionPolicyVersion(String agency,String code,int version,String tag,int days,LocalDate from,LocalDate to,String basis,boolean published){super(agency);this.code=requiredText(code,"code").toUpperCase();if(version<1||days<1)throw new IllegalArgumentException("Policy version and retention days must be positive");policyVersion=version;retentionTag=requiredText(tag,"retentionTag");retentionDays=days;effectiveFrom=from;if(to!=null&&to.isBefore(from))throw new IllegalArgumentException("effectiveTo cannot precede effectiveFrom");effectiveTo=to;this.basis=requiredText(basis,"basis");this.published=published;}
    public boolean effectiveOn(LocalDate date){return published&&!date.isBefore(effectiveFrom)&&(effectiveTo==null||!date.isAfter(effectiveTo));}
    public String getCode(){return code;} public int getPolicyVersion(){return policyVersion;} public String getRetentionTag(){return retentionTag;} public int getRetentionDays(){return retentionDays;} public LocalDate getEffectiveFrom(){return effectiveFrom;} public LocalDate getEffectiveTo(){return effectiveTo;} public String getBasis(){return basis;} public boolean isPublished(){return published;}
}
