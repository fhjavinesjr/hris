package com.primehr.learningdevelopment.evaluation.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.math.*;
import java.time.Instant;

@Entity
@Table(name="ld_evaluation_result",uniqueConstraints=@UniqueConstraint(name="uk_ld_evaluation_result_revision",columnNames={"agency_id","task_id","result_revision"}))
public class LdEvaluationResult extends RspAuditedEntity {
    public enum Status { SUBMITTED, VERIFIED, RETURNED }
    @Column(name="task_id",nullable=false,length=36) private String taskId;
    @Column(name="result_revision",nullable=false) private int resultRevision;
    @Column(name="supersedes_result_id",length=36) private String supersedesResultId;
    @Column(precision=19,scale=6) private BigDecimal score;
    @Column private Boolean passed;
    @Column(nullable=false,length=4000) private String narrative;
    @Column(name="evidence_reference",length=500) private String evidenceReference;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Column(name="submitted_by",nullable=false,length=100) private String submittedBy;
    @Column(name="submitted_at",nullable=false) private Instant submittedAt;
    @Column(name="decided_by",length=100) private String decidedBy;
    @Column(name="decided_at") private Instant decidedAt;
    @Column(name="decision_reason",length=2000) private String decisionReason;
    @Column(name="result_fingerprint",nullable=false,length=64) private String resultFingerprint;
    protected LdEvaluationResult() {}
    public LdEvaluationResult(String agency,String task,int revision,String prior,BigDecimal score,Boolean passed,String narrative,String evidence,String actor,Instant at,String fingerprint){super(agency);taskId=requiredText(task,"taskId");if(revision<1)throw new IllegalArgumentException("resultRevision must be positive");resultRevision=revision;supersedesResultId=optionalText(prior);this.score=score==null?null:score.setScale(6,RoundingMode.HALF_UP);this.passed=passed;this.narrative=requiredText(narrative,"narrative");evidenceReference=optionalText(evidence);status=Status.SUBMITTED;submittedBy=requiredText(actor,"actor");submittedAt=at;resultFingerprint=requiredText(fingerprint,"resultFingerprint");}
    public void decide(boolean approve,String reason,String actor,Instant at){if(status!=Status.SUBMITTED)throw new IllegalStateException("Only a SUBMITTED evaluation result may be decided");if(submittedBy.equalsIgnoreCase(actor))throw new IllegalArgumentException("Evaluator cannot verify the same result");status=approve?Status.VERIFIED:Status.RETURNED;decisionReason=requiredText(reason,"reason");decidedBy=requiredText(actor,"actor");decidedAt=at;}
    public String getTaskId(){return taskId;} public int getResultRevision(){return resultRevision;} public String getSupersedesResultId(){return supersedesResultId;} public BigDecimal getScore(){return score;} public Boolean getPassed(){return passed;} public String getNarrative(){return narrative;} public String getEvidenceReference(){return evidenceReference;} public Status getStatus(){return status;} public String getSubmittedBy(){return submittedBy;} public Instant getSubmittedAt(){return submittedAt;} public String getDecidedBy(){return decidedBy;} public Instant getDecidedAt(){return decidedAt;} public String getDecisionReason(){return decisionReason;} public String getResultFingerprint(){return resultFingerprint;}
}
