package com.primehr.learningdevelopment.plan.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="ld_referral_disposition",uniqueConstraints=@UniqueConstraint(name="uk_ld_referral_disposition",columnNames={"agency_id","referral_id","referral_version"}))
public class LdReferralDisposition extends RspAuditedEntity {
    public enum Decision { ACCEPTED, RETURNED, DECLINED }
    @Column(name="referral_id",nullable=false,length=36) private String referralId;
    @Column(name="referral_version",nullable=false) private long referralVersion;
    @Column(name="referral_fingerprint",nullable=false,length=64) private String referralFingerprint;
    @Column(name="employee_no",nullable=false,length=100) private String employeeNo;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Decision decision;
    @Column(name="learning_need_id",length=36) private String learningNeedId;
    @Column(name="linked_idp_item_id",length=36) private String linkedIdpItemId;
    @Column(name="decided_by",nullable=false,length=100) private String decidedBy;
    @Column(name="decided_at",nullable=false) private Instant decidedAt;
    @Column(name="reason",nullable=false,length=2000) private String reason;
    protected LdReferralDisposition(){}
    public LdReferralDisposition(String agency,String referralId,long referralVersion,String fingerprint,String employeeNo,
            Decision decision,String needId,String itemId,String actor,Instant at,String reason){super(agency);
        if(referralVersion<0)throw new IllegalArgumentException("referralVersion cannot be negative");
        this.referralId=requiredText(referralId,"referralId");this.referralVersion=referralVersion;referralFingerprint=requiredText(fingerprint,"referralFingerprint");
        this.employeeNo=requiredText(employeeNo,"employeeNo");this.decision=java.util.Objects.requireNonNull(decision);learningNeedId=optionalText(needId);linkedIdpItemId=optionalText(itemId);
        decidedBy=requiredText(actor,"actor");decidedAt=java.util.Objects.requireNonNull(at);this.reason=requiredText(reason,"reason");
        if(decision==Decision.ACCEPTED&&learningNeedId==null)throw new IllegalArgumentException("Accepted referral requires a learning need");
        if(decision!=Decision.ACCEPTED&&(learningNeedId!=null||linkedIdpItemId!=null))throw new IllegalArgumentException("Only accepted referrals may link a need or IDP item");
    }
    public void linkIdpItem(String itemId){
        if(decision!=Decision.ACCEPTED)throw new IllegalStateException("Only an accepted referral may link an IDP item");
        String normalized=requiredText(itemId,"itemId");
        if(linkedIdpItemId!=null&&!linkedIdpItemId.equals(normalized))throw new IllegalStateException("Accepted referral is already linked to another IDP item");
        linkedIdpItemId=normalized;
    }
    public void unlinkIdpItem(String itemId){
        if(linkedIdpItemId!=null&&linkedIdpItemId.equals(requiredText(itemId,"itemId")))linkedIdpItemId=null;
    }
    public String getReferralId(){return referralId;}public long getReferralVersion(){return referralVersion;}public String getReferralFingerprint(){return referralFingerprint;}public String getEmployeeNo(){return employeeNo;}public Decision getDecision(){return decision;}public String getLearningNeedId(){return learningNeedId;}public String getLinkedIdpItemId(){return linkedIdpItemId;}public String getDecidedBy(){return decidedBy;}public Instant getDecidedAt(){return decidedAt;}public String getReason(){return reason;}
}
