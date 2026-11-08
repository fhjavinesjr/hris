package com.primehr.learningdevelopment.evaluation.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name="ld_learning_action_plan",uniqueConstraints=@UniqueConstraint(name="uk_ld_lap_completion",columnNames={"agency_id","completion_case_id"}))
public class LdLearningActionPlan extends RspAuditedEntity {
    public enum Status { DRAFT, SUBMITTED, VERIFIED, RETURNED }
    @Column(name="completion_case_id",nullable=false,length=36) private String completionCaseId;
    @Column(name="employee_no",nullable=false,length=100) private String employeeNo;
    @Column(name="supervisor_employee_no",nullable=false,length=100) private String supervisorEmployeeNo;
    @Column(name="application_objective",nullable=false,length=3000) private String applicationObjective;
    @Column(name="success_measure",nullable=false,length=3000) private String successMeasure;
    @Column(name="target_date",nullable=false) private LocalDate targetDate;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Column(name="content_revision",nullable=false) private int contentRevision;
    @Column(name="submitted_by",length=100) private String submittedBy; @Column(name="submitted_at") private Instant submittedAt;
    @Column(name="follow_up_outcome",length=4000) private String followUpOutcome; @Column(name="evidence_reference",length=500) private String evidenceReference;
    @Column(name="decided_by",length=100) private String decidedBy; @Column(name="decided_at") private Instant decidedAt; @Column(name="decision_reason",length=2000) private String decisionReason;
    protected LdLearningActionPlan() {}
    public LdLearningActionPlan(String agency,String completion,String employee,String supervisor,String objective,String measure,LocalDate target){super(agency);completionCaseId=requiredText(completion,"completionCaseId");employeeNo=requiredText(employee,"employeeNo");supervisorEmployeeNo=requiredText(supervisor,"supervisorEmployeeNo");applicationObjective=requiredText(objective,"applicationObjective");successMeasure=requiredText(measure,"successMeasure");targetDate=target;if(target==null)throw new IllegalArgumentException("targetDate is required");status=Status.DRAFT;contentRevision=1;}
    public void update(String objective,String measure,LocalDate target){if(status!=Status.DRAFT&&status!=Status.RETURNED)throw new IllegalStateException("Only a DRAFT or RETURNED action plan may be edited");applicationObjective=requiredText(objective,"applicationObjective");successMeasure=requiredText(measure,"successMeasure");if(target==null)throw new IllegalArgumentException("targetDate is required");targetDate=target;status=Status.DRAFT;contentRevision++;}
    public void submit(String actor,Instant at){if(status!=Status.DRAFT)throw new IllegalStateException("Only a DRAFT action plan may be submitted");status=Status.SUBMITTED;submittedBy=requiredText(actor,"actor");submittedAt=at;}
    public void followUp(boolean approve,String outcome,String evidence,String reason,String actor,Instant at){if(status!=Status.SUBMITTED)throw new IllegalStateException("Only a SUBMITTED action plan may be followed up");if(submittedBy.equalsIgnoreCase(actor))throw new IllegalArgumentException("Submitter cannot verify the same action plan");followUpOutcome=requiredText(outcome,"followUpOutcome");evidenceReference=optionalText(evidence);decisionReason=requiredText(reason,"reason");decidedBy=requiredText(actor,"actor");decidedAt=at;status=approve?Status.VERIFIED:Status.RETURNED;}
    public String getCompletionCaseId(){return completionCaseId;} public String getEmployeeNo(){return employeeNo;} public String getSupervisorEmployeeNo(){return supervisorEmployeeNo;} public String getApplicationObjective(){return applicationObjective;} public String getSuccessMeasure(){return successMeasure;} public LocalDate getTargetDate(){return targetDate;} public Status getStatus(){return status;} public int getContentRevision(){return contentRevision;} public String getSubmittedBy(){return submittedBy;} public Instant getSubmittedAt(){return submittedAt;} public String getFollowUpOutcome(){return followUpOutcome;} public String getEvidenceReference(){return evidenceReference;} public String getDecidedBy(){return decidedBy;} public Instant getDecidedAt(){return decidedAt;} public String getDecisionReason(){return decisionReason;}
}
