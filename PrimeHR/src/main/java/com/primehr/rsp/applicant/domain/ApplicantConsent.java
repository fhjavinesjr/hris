package com.primehr.rsp.applicant.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="rsp_applicant_consent",uniqueConstraints=@UniqueConstraint(name="uk_rsp_consent_notice",columnNames={"agency_id","applicant_id","privacy_notice_id"}))
public class ApplicantConsent extends RspAuditedEntity {
    @Column(name="applicant_id",nullable=false,length=36) private String applicantId;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="privacy_notice_id",nullable=false) private PrivacyNotice notice;
    @Column(name="notice_version",nullable=false) private int noticeVersion;
    @Column(name="accepted_at",nullable=false) private Instant acceptedAt;
    @Column(name="evidence_ip",length=64) private String evidenceIp;
    @Column(name="evidence_user_agent",length=500) private String evidenceUserAgent;
    @Column(name="withdrawn_at") private Instant withdrawnAt;
    protected ApplicantConsent(){}
    public ApplicantConsent(String agency,String applicant,PrivacyNotice notice,Instant at,String ip,String agent){super(agency);applicantId=requiredText(applicant,"applicantId");this.notice=java.util.Objects.requireNonNull(notice);noticeVersion=notice.getDefinitionVersion();acceptedAt=java.util.Objects.requireNonNull(at);evidenceIp=optionalText(ip);evidenceUserAgent=optionalText(agent);}
    public boolean active(){return withdrawnAt==null;} public String getApplicantId(){return applicantId;} public PrivacyNotice getNotice(){return notice;} public int getNoticeVersion(){return noticeVersion;} public Instant getAcceptedAt(){return acceptedAt;} public Instant getWithdrawnAt(){return withdrawnAt;}
}
