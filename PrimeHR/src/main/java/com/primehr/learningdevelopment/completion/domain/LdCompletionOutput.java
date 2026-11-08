package com.primehr.learningdevelopment.completion.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.*;
import java.util.Locale;

@Entity
@Table(name="ld_completion_output",uniqueConstraints=
        @UniqueConstraint(name="uk_ld_completion_output_code",columnNames={"agency_id","completion_case_id","normalized_code"}))
public class LdCompletionOutput extends RspAuditedEntity{
    public enum Status{REQUIRED,SUBMITTED,VERIFIED,WAIVED}
    @Column(name="completion_case_id",nullable=false,length=36)private String completionCaseId;
    @Column(nullable=false,length=80)private String code;
    @Column(name="normalized_code",nullable=false,length=80)private String normalizedCode;
    @Column(nullable=false,length=300)private String title;
    @Column(nullable=false,length=3000)private String description;
    @Enumerated(EnumType.STRING)@Column(nullable=false,length=20)private Status status;
    @Column(name="submitted_by",length=100)private String submittedBy;@Column(name="submitted_at")private Instant submittedAt;
    @Column(name="verified_by",length=100)private String verifiedBy;@Column(name="verified_at")private Instant verifiedAt;
    @Column(name="decision_reason",length=2000)private String decisionReason;
    protected LdCompletionOutput(){}
    public LdCompletionOutput(String agency,String caseId,String code,String title,String description){super(agency);completionCaseId=requiredText(caseId,"completionCaseId");this.code=requiredText(code,"code");normalizedCode=this.code.toUpperCase(Locale.ROOT);this.title=requiredText(title,"title");this.description=requiredText(description,"description");status=Status.REQUIRED;}
    public void submitted(String actor,Instant at){if(status!=Status.REQUIRED&&status!=Status.SUBMITTED)throw new IllegalStateException("Only a required output may be submitted");status=Status.SUBMITTED;submittedBy=requiredText(actor,"actor");submittedAt=at==null?Instant.now():at;}
    public void verify(String actor,Instant at){if(status!=Status.SUBMITTED)throw new IllegalStateException("Only a submitted output may be verified");status=Status.VERIFIED;verifiedBy=requiredText(actor,"actor");verifiedAt=at==null?Instant.now():at;}
    public void waive(String reason,String actor,Instant at){if(status!=Status.REQUIRED&&status!=Status.SUBMITTED)throw new IllegalStateException("Only an unresolved output may be waived");status=Status.WAIVED;decisionReason=requiredText(reason,"reason");verifiedBy=requiredText(actor,"actor");verifiedAt=at==null?Instant.now():at;}
    public String getCompletionCaseId(){return completionCaseId;}public String getCode(){return code;}public String getNormalizedCode(){return normalizedCode;}public String getTitle(){return title;}public String getDescription(){return description;}public Status getStatus(){return status;}public String getSubmittedBy(){return submittedBy;}public Instant getSubmittedAt(){return submittedAt;}public String getVerifiedBy(){return verifiedBy;}public Instant getVerifiedAt(){return verifiedAt;}public String getDecisionReason(){return decisionReason;}
}
