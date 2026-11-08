package com.primehr.succession.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name="succession_development_plan", uniqueConstraints={
        @UniqueConstraint(name="uk_succ_dev_plan_decision",columnNames={"agency_id","pool_decision_id"}),
        @UniqueConstraint(name="uk_succ_dev_plan_series_version",columnNames={"agency_id","series_id","plan_version"})})
public class SuccessionDevelopmentPlan extends RspAuditedEntity {
    public enum Status { DRAFT, ACTIVE, COMPLETED, SUPERSEDED, CANCELLED }
    public enum AcknowledgmentStatus { PENDING, ACKNOWLEDGED, RESPONSE_SUBMITTED }
    @Column(name="series_id",nullable=false,length=36)private String seriesId;
    @Column(name="plan_version",nullable=false)private int planVersion;
    @Column(name="supersedes_plan_id",length=36)private String supersedesPlanId;
    @Column(name="pool_decision_id",nullable=false,length=36)private String poolDecisionId;
    @Column(name="assessment_id",nullable=false,length=36)private String assessmentId;
    @Column(name="candidate_id",nullable=false,length=36)private String candidateId;
    @Column(name="employee_no",nullable=false,length=100)private String employeeNo;
    @Column(name="employee_name",nullable=false,length=300)private String employeeName;
    @Column(name="target_position_name",nullable=false,length=250)private String targetPositionName;
    @Column(name="readiness_level_code",nullable=false,length=80)private String readinessLevelCode;
    @Column(name="overall_goal",nullable=false,length=4000)private String overallGoal;
    @Column(name="starts_on",nullable=false)private LocalDate startsOn;
    @Column(name="target_completion_date",nullable=false)private LocalDate targetCompletionDate;
    @Column(name="next_review_on",nullable=false)private LocalDate nextReviewOn;
    @Enumerated(EnumType.STRING)@Column(nullable=false,length=20)private Status status;
    @Enumerated(EnumType.STRING)@Column(name="acknowledgment_status",nullable=false,length=30)private AcknowledgmentStatus acknowledgmentStatus;
    @Column(name="acknowledgment_response",length=4000)private String acknowledgmentResponse;
    @Column(name="acknowledged_at")private Instant acknowledgedAt;
    @Column(name="activated_by",length=100)private String activatedBy;
    @Column(name="activated_at")private Instant activatedAt;
    @Column(name="completed_by",length=100)private String completedBy;
    @Column(name="completed_at")private Instant completedAt;
    protected SuccessionDevelopmentPlan(){}
    public SuccessionDevelopmentPlan(String agency,SuccessionPoolDecision decision,SuccessionCandidate candidate,
            String goal,LocalDate starts,LocalDate target,LocalDate nextReview){super(agency);
        if(decision==null||decision.getOutcome()!=SuccessionPoolDecision.Outcome.INCLUDED)throw new IllegalArgumentException("An INCLUDED pool decision is required");
        seriesId=java.util.UUID.randomUUID().toString();planVersion=1;poolDecisionId=decision.getId();assessmentId=decision.getAssessmentId();candidateId=decision.getCandidateId();
        employeeNo=requiredText(candidate.getEmployeeNo(),"employeeNo");employeeName=requiredText(candidate.getEmployeeName(),"employeeName");targetPositionName=requiredText(candidate.getTargetPositionName(),"targetPositionName");readinessLevelCode=requiredText(decision.getReadinessLevelCode(),"readinessLevelCode");
        apply(goal,starts,target,nextReview);status=Status.DRAFT;acknowledgmentStatus=AcknowledgmentStatus.PENDING;
    }
    public void update(String goal,LocalDate starts,LocalDate target,LocalDate nextReview){editable();apply(goal,starts,target,nextReview);}
    private void apply(String goal,LocalDate starts,LocalDate target,LocalDate review){overallGoal=requiredText(goal,"overallGoal");startsOn=java.util.Objects.requireNonNull(starts,"startsOn");targetCompletionDate=java.util.Objects.requireNonNull(target,"targetCompletionDate");nextReviewOn=java.util.Objects.requireNonNull(review,"nextReviewOn");if(target.isBefore(starts))throw new IllegalArgumentException("Target completion date cannot precede start date");if(review.isBefore(starts)||review.isAfter(target))throw new IllegalArgumentException("Next review must fall within the plan period");}
    public void activate(String actor,Instant at){if(status!=Status.DRAFT)throw new IllegalLifecycleTransitionException("Only a draft succession development plan may be activated");status=Status.ACTIVE;activatedBy=requiredText(actor,"actor");activatedAt=at;}
    public void acknowledge(String employee,String response,Instant at){if(status!=Status.ACTIVE)throw new IllegalLifecycleTransitionException("Only an active plan may be acknowledged");if(!employeeNo.equalsIgnoreCase(employee))throw new IllegalArgumentException("Only the candidate may acknowledge the plan");acknowledgmentResponse=optionalText(response);acknowledgmentStatus=acknowledgmentResponse==null?AcknowledgmentStatus.ACKNOWLEDGED:AcknowledgmentStatus.RESPONSE_SUBMITTED;acknowledgedAt=at;}
    public void complete(String actor,Instant at){if(status!=Status.ACTIVE)throw new IllegalLifecycleTransitionException("Only an active plan may be completed");status=Status.COMPLETED;completedBy=requiredText(actor,"actor");completedAt=at;}
    private void editable(){if(status!=Status.DRAFT)throw new IllegalLifecycleTransitionException("Only a draft succession development plan may be changed");}
    public String getSeriesId(){return seriesId;}public int getPlanVersion(){return planVersion;}public String getSupersedesPlanId(){return supersedesPlanId;}public String getPoolDecisionId(){return poolDecisionId;}public String getAssessmentId(){return assessmentId;}public String getCandidateId(){return candidateId;}public String getEmployeeNo(){return employeeNo;}public String getEmployeeName(){return employeeName;}public String getTargetPositionName(){return targetPositionName;}public String getReadinessLevelCode(){return readinessLevelCode;}public String getOverallGoal(){return overallGoal;}public LocalDate getStartsOn(){return startsOn;}public LocalDate getTargetCompletionDate(){return targetCompletionDate;}public LocalDate getNextReviewOn(){return nextReviewOn;}public Status getStatus(){return status;}public AcknowledgmentStatus getAcknowledgmentStatus(){return acknowledgmentStatus;}public String getAcknowledgmentResponse(){return acknowledgmentResponse;}public Instant getAcknowledgedAt(){return acknowledgedAt;}public String getActivatedBy(){return activatedBy;}public Instant getActivatedAt(){return activatedAt;}public String getCompletedBy(){return completedBy;}public Instant getCompletedAt(){return completedAt;}
}
