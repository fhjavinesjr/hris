package com.primehr.succession.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "succession_continuity_plan", uniqueConstraints =
        @UniqueConstraint(name = "uk_succ_continuity_plan_risk", columnNames = {"agency_id", "risk_review_id"}))
public class SuccessionContinuityPlan extends RspAuditedEntity {
    public enum Status { DRAFT, SUBMITTED, APPROVED, FINALIZED }
    public enum CoverageStatus { NOT_ASSESSED, NO_ACTIVE_BENCH, ACTIVE_BENCH }

    @Column(name="risk_review_id",nullable=false,length=36) private String riskReviewId;
    @Column(name="cycle_id",nullable=false,length=36) private String cycleId;
    @Column(name="designation_id",nullable=false,length=36) private String designationId;
    @Column(name="charter_version_id",nullable=false,length=36) private String charterVersionId;
    @Column(name="target_type",nullable=false,length=20) private String targetType;
    @Column(name="target_id",nullable=false) private Long targetId;
    @Column(name="job_position_id",nullable=false) private Long jobPositionId;
    @Column(name="position_name",nullable=false,length=250) private String positionName;
    @Column(name="plantilla_id") private Long plantillaId;
    @Column(name="plantilla_name",length=250) private String plantillaName;
    @Column(name="target_fingerprint",nullable=false,length=120) private String targetFingerprint;
    @Column(name="risk_level",nullable=false,length=20) private String riskLevel;
    @Column(name="risk_reason_snapshot",nullable=false,length=4000) private String riskReasonSnapshot;
    @Column(name="critical_functions",nullable=false,length=4000) private String criticalFunctions;
    @Column(name="interim_arrangement",nullable=false,length=4000) private String interimArrangement;
    @Column(name="continuity_actions",nullable=false,length=4000) private String continuityActions;
    @Column(name="activation_triggers",nullable=false,length=4000) private String activationTriggers;
    @Column(name="target_recovery_days",nullable=false) private int targetRecoveryDays;
    @Column(name="review_on",nullable=false) private LocalDate reviewOn;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Enumerated(EnumType.STRING) @Column(name="coverage_status",nullable=false,length=30) private CoverageStatus coverageStatus;
    @Column(name="bench_count",nullable=false) private int benchCount;
    @Column(name="submitted_by",length=100) private String submittedBy;
    @Column(name="submitted_at") private Instant submittedAt;
    @Column(name="approved_by",length=100) private String approvedBy;
    @Column(name="approved_at") private Instant approvedAt;
    @Column(name="approval_reason",length=4000) private String approvalReason;
    @Column(name="finalized_by",length=100) private String finalizedBy;
    @Column(name="finalized_at") private Instant finalizedAt;

    protected SuccessionContinuityPlan() { }

    public SuccessionContinuityPlan(String agency, SuccessionContinuityRiskReview risk,
                                    SuccessionCycle cycle, String criticalFunctions,
                                    String interimArrangement, String continuityActions,
                                    String activationTriggers, int targetRecoveryDays,
                                    LocalDate reviewOn) {
        super(agency);
        if (risk == null || risk.getStatus() != SuccessionContinuityRiskReview.Status.FINALIZED) {
            throw new IllegalArgumentException("A finalized continuity risk review is required");
        }
        riskReviewId = risk.getId(); cycleId = risk.getCycleId(); designationId = risk.getDesignationId();
        charterVersionId = requiredText(cycle.getCharterVersionId(), "charterVersionId");
        targetType = risk.getTargetType(); targetId = risk.getTargetId(); jobPositionId = risk.getJobPositionId();
        positionName = requiredText(risk.getPositionName(), "positionName"); plantillaId = risk.getPlantillaId();
        plantillaName = optionalText(risk.getPlantillaName()); targetFingerprint = requiredText(risk.getTargetFingerprint(), "targetFingerprint");
        riskLevel = risk.getRiskLevel().name(); riskReasonSnapshot = requiredText(risk.getRiskReason(), "riskReason");
        status = Status.DRAFT; coverageStatus = CoverageStatus.NOT_ASSESSED;
        update(criticalFunctions, interimArrangement, continuityActions, activationTriggers, targetRecoveryDays, reviewOn);
    }

    public void update(String functions, String interim, String actions, String triggers, int recoveryDays, LocalDate review) {
        require(Status.DRAFT);
        if (recoveryDays < 0 || review == null) throw new IllegalArgumentException("Recovery days and review date are required");
        criticalFunctions = requiredText(functions, "criticalFunctions");
        interimArrangement = requiredText(interim, "interimArrangement");
        continuityActions = requiredText(actions, "continuityActions");
        activationTriggers = requiredText(triggers, "activationTriggers");
        targetRecoveryDays = recoveryDays; reviewOn = review;
    }

    public void submit(String actor, Instant at) { require(Status.DRAFT); status=Status.SUBMITTED; submittedBy=requiredText(actor,"actor"); submittedAt=at; }
    public void approve(String actor,String reason,Instant at) { require(Status.SUBMITTED); if(actor.equalsIgnoreCase(submittedBy))throw new IllegalArgumentException("Submitter cannot approve the same plan"); status=Status.APPROVED;approvedBy=requiredText(actor,"actor");approvalReason=requiredText(reason,"reason");approvedAt=at; }
    public void finalizePlan(String actor,int count,Instant at) { require(Status.APPROVED);if(actor.equalsIgnoreCase(approvedBy))throw new IllegalArgumentException("Approver cannot finalize the same plan");if(count<0)throw new IllegalArgumentException("benchCount cannot be negative");benchCount=count;coverageStatus=count==0?CoverageStatus.NO_ACTIVE_BENCH:CoverageStatus.ACTIVE_BENCH;status=Status.FINALIZED;finalizedBy=requiredText(actor,"actor");finalizedAt=at; }
    private void require(Status expected){if(status!=expected)throw new IllegalLifecycleTransitionException("Continuity plan must be "+expected+" for this action");}

    public String getRiskReviewId(){return riskReviewId;} public String getCycleId(){return cycleId;} public String getDesignationId(){return designationId;} public String getCharterVersionId(){return charterVersionId;} public String getTargetType(){return targetType;} public Long getTargetId(){return targetId;} public Long getJobPositionId(){return jobPositionId;} public String getPositionName(){return positionName;} public Long getPlantillaId(){return plantillaId;} public String getPlantillaName(){return plantillaName;} public String getTargetFingerprint(){return targetFingerprint;} public String getRiskLevel(){return riskLevel;} public String getRiskReasonSnapshot(){return riskReasonSnapshot;} public String getCriticalFunctions(){return criticalFunctions;} public String getInterimArrangement(){return interimArrangement;} public String getContinuityActions(){return continuityActions;} public String getActivationTriggers(){return activationTriggers;} public int getTargetRecoveryDays(){return targetRecoveryDays;} public LocalDate getReviewOn(){return reviewOn;} public Status getStatus(){return status;} public CoverageStatus getCoverageStatus(){return coverageStatus;} public int getBenchCount(){return benchCount;} public String getSubmittedBy(){return submittedBy;} public Instant getSubmittedAt(){return submittedAt;} public String getApprovedBy(){return approvedBy;} public Instant getApprovedAt(){return approvedAt;} public String getApprovalReason(){return approvalReason;} public String getFinalizedBy(){return finalizedBy;} public Instant getFinalizedAt(){return finalizedAt;}
}
