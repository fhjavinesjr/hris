package com.primehr.rewardsrecognition.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;

@Entity
@Table(name="rr_award_cycle", uniqueConstraints=@UniqueConstraint(name="uk_rr_cycle_code",columnNames={"agency_id","normalized_code"}))
public class RrAwardCycle extends RspAuditedEntity {
    public enum Status { DRAFT, OPEN, CLOSED, CANCELLED }
    @Column(nullable=false,length=80) private String code;
    @Column(name="normalized_code",nullable=false,length=80) private String normalizedCode;
    @Column(nullable=false,length=200) private String title;
    @Column(length=2000) private String description;
    @Column(name="policy_version_id",nullable=false,length=36) private String policyVersionId;
    @Column(name="policy_definition_version",nullable=false) private int policyDefinitionVersion;
    @Column(name="policy_fingerprint",nullable=false,length=64) private String policyFingerprint;
    @Column(name="nomination_opens_on",nullable=false) private LocalDate nominationOpensOn;
    @Column(name="nomination_closes_on",nullable=false) private LocalDate nominationClosesOn;
    @Column(name="evaluation_opens_on",nullable=false) private LocalDate evaluationOpensOn;
    @Column(name="evaluation_closes_on",nullable=false) private LocalDate evaluationClosesOn;
    @Column(name="decision_due_on",nullable=false) private LocalDate decisionDueOn;
    @Column(name="conferment_on") private LocalDate confermentOn;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Column(name="opened_by",length=100) private String openedBy;
    @Column(name="opened_at") private Instant openedAt;
    @Column(name="closed_by",length=100) private String closedBy;
    @Column(name="closed_at") private Instant closedAt;
    @Column(name="transition_reason",length=1000) private String transitionReason;
    protected RrAwardCycle(){}
    public RrAwardCycle(String agency,String code,String title,String description,String policyVersionId,int policyDefinitionVersion,String policyFingerprint,LocalDate nominationOpensOn,LocalDate nominationClosesOn,LocalDate evaluationOpensOn,LocalDate evaluationClosesOn,LocalDate decisionDueOn,LocalDate confermentOn){super(agency);this.code=requiredText(code,"code");normalizedCode=this.code.toUpperCase(Locale.ROOT);status=Status.DRAFT;apply(title,description,policyVersionId,policyDefinitionVersion,policyFingerprint,nominationOpensOn,nominationClosesOn,evaluationOpensOn,evaluationClosesOn,decisionDueOn,confermentOn);}
    public void update(String title,String description,String policyVersionId,int policyDefinitionVersion,String policyFingerprint,LocalDate nominationOpensOn,LocalDate nominationClosesOn,LocalDate evaluationOpensOn,LocalDate evaluationClosesOn,LocalDate decisionDueOn,LocalDate confermentOn){draft();apply(title,description,policyVersionId,policyDefinitionVersion,policyFingerprint,nominationOpensOn,nominationClosesOn,evaluationOpensOn,evaluationClosesOn,decisionDueOn,confermentOn);}
    private void apply(String title,String description,String policyVersionId,int policyDefinitionVersion,String policyFingerprint,LocalDate no,LocalDate nc,LocalDate eo,LocalDate ec,LocalDate dd,LocalDate co){if(no==null||nc==null||eo==null||ec==null||dd==null)throw new IllegalArgumentException("All cycle windows except conferment are required");if(nc.isBefore(no)||eo.isBefore(nc)||ec.isBefore(eo)||dd.isBefore(ec)||co!=null&&co.isBefore(dd))throw new IllegalArgumentException("Cycle windows must be chronological");if(policyDefinitionVersion<1)throw new IllegalArgumentException("policyDefinitionVersion must be positive");this.title=requiredText(title,"title");this.description=optionalText(description);this.policyVersionId=requiredText(policyVersionId,"policyVersionId");this.policyDefinitionVersion=policyDefinitionVersion;this.policyFingerprint=requiredText(policyFingerprint,"policyFingerprint");nominationOpensOn=no;nominationClosesOn=nc;evaluationOpensOn=eo;evaluationClosesOn=ec;decisionDueOn=dd;confermentOn=co;}
    public void open(String actor,String reason,Instant at){draft();status=Status.OPEN;openedBy=requiredText(actor,"actor");openedAt=at;transitionReason=requiredText(reason,"reason");}
    public void close(String actor,String reason,Instant at){if(status!=Status.OPEN)throw new IllegalLifecycleTransitionException("Only an open award cycle may be closed");status=Status.CLOSED;closedBy=requiredText(actor,"actor");closedAt=at;transitionReason=requiredText(reason,"reason");}
    public void cancel(String actor,String reason,Instant at){if(status!=Status.DRAFT&&status!=Status.OPEN)throw new IllegalLifecycleTransitionException("Only a draft or open award cycle may be cancelled");status=Status.CANCELLED;closedBy=requiredText(actor,"actor");closedAt=at;transitionReason=requiredText(reason,"reason");}
    public boolean acceptsNominations(LocalDate day){return status==Status.OPEN&&!day.isBefore(nominationOpensOn)&&!day.isAfter(nominationClosesOn);}
    private void draft(){if(status!=Status.DRAFT)throw new IllegalLifecycleTransitionException("Only a draft award cycle may be changed");}
    public String getCode(){return code;}public String getTitle(){return title;}public String getDescription(){return description;}public String getPolicyVersionId(){return policyVersionId;}public int getPolicyDefinitionVersion(){return policyDefinitionVersion;}public String getPolicyFingerprint(){return policyFingerprint;}public LocalDate getNominationOpensOn(){return nominationOpensOn;}public LocalDate getNominationClosesOn(){return nominationClosesOn;}public LocalDate getEvaluationOpensOn(){return evaluationOpensOn;}public LocalDate getEvaluationClosesOn(){return evaluationClosesOn;}public LocalDate getDecisionDueOn(){return decisionDueOn;}public LocalDate getConfermentOn(){return confermentOn;}public Status getStatus(){return status;}public String getOpenedBy(){return openedBy;}public Instant getOpenedAt(){return openedAt;}public String getClosedBy(){return closedBy;}public Instant getClosedAt(){return closedAt;}public String getTransitionReason(){return transitionReason;}
}
