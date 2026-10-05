package com.primehr.performancemanagement.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "spms_calibration_case", uniqueConstraints = @UniqueConstraint(
        name = "uk_spms_calibration_rating", columnNames = {"agency_id", "rating_case_id"}))
public class PerformanceCalibrationCase extends RspAuditedEntity {
    public enum Status { OPEN, PROPOSAL_SUBMITTED, APPROVED, REJECTED, FINALIZED }

    @Column(name = "rating_case_id", nullable = false, length = 36)
    private String ratingCaseId;
    @Column(name = "supervisor_assessment_id", nullable = false, length = 36)
    private String supervisorAssessmentId;
    @Column(name = "pmt_id", nullable = false, length = 36)
    private String pmtId;
    @Column(name = "roster_revision", nullable = false)
    private int rosterRevision;
    @Column(name = "quorum_required", nullable = false)
    private int quorumRequired;
    @Column(name = "eligible_voters", nullable = false)
    private int eligibleVoters;
    @Column(name = "decision_count", nullable = false)
    private int decisionCount;
    @Column(name = "proposal_assessment_id", length = 36)
    private String proposalAssessmentId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;
    @Column(name = "opened_by", nullable = false, length = 100)
    private String openedBy;
    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    protected PerformanceCalibrationCase() {}

    public PerformanceCalibrationCase(String agencyId, String ratingCaseId, String supervisorAssessmentId,
                                      String pmtId, int rosterRevision, int eligibleVoters,
                                      String actor, Instant openedAt) {
        super(agencyId);
        this.ratingCaseId = requiredText(ratingCaseId, "ratingCaseId");
        this.supervisorAssessmentId = requiredText(supervisorAssessmentId, "supervisorAssessmentId");
        this.pmtId = requiredText(pmtId, "pmtId");
        this.rosterRevision = rosterRevision;
        if (eligibleVoters < 1) {
            throw new IllegalArgumentException("At least one non-conflicted PMT voter is required");
        }
        this.eligibleVoters = eligibleVoters;
        this.quorumRequired = eligibleVoters / 2 + 1;
        this.status = Status.OPEN;
        this.openedBy = requiredText(actor, "actor");
        this.openedAt = openedAt;
    }

    public void proposalSubmitted(String proposalAssessmentId, int decisionEligibleVoters) {
        requireStatus(Status.OPEN, "Calibration proposal is not open");
        if (decisionEligibleVoters < 1) {
            throw new IllegalStateException("At least one independent PMT voter is required");
        }
        this.proposalAssessmentId = requiredText(proposalAssessmentId, "proposalAssessmentId");
        eligibleVoters = decisionEligibleVoters;
        quorumRequired = decisionEligibleVoters / 2 + 1;
        status = Status.PROPOSAL_SUBMITTED;
    }

    public void decisionRecorded() {
        requireStatus(Status.PROPOSAL_SUBMITTED, "Calibration proposal is not awaiting decision");
        decisionCount++;
    }

    public void approve() {
        requireStatus(Status.PROPOSAL_SUBMITTED, "Calibration proposal is not awaiting decision");
        status = Status.APPROVED;
    }

    public void reject() {
        requireStatus(Status.PROPOSAL_SUBMITTED, "Calibration proposal is not awaiting decision");
        status = Status.REJECTED;
    }

    public void finalized() {
        requireStatus(Status.APPROVED, "Only approved calibration may be finalized");
        status = Status.FINALIZED;
    }

    private void requireStatus(Status expected, String message) {
        if (status != expected) throw new IllegalStateException(message);
    }

    public String getRatingCaseId() { return ratingCaseId; }
    public String getSupervisorAssessmentId() { return supervisorAssessmentId; }
    public String getPmtId() { return pmtId; }
    public int getRosterRevision() { return rosterRevision; }
    public int getQuorumRequired() { return quorumRequired; }
    public int getEligibleVoters() { return eligibleVoters; }
    public int getDecisionCount() { return decisionCount; }
    public String getProposalAssessmentId() { return proposalAssessmentId; }
    public Status getStatus() { return status; }
    public String getOpenedBy() { return openedBy; }
    public Instant getOpenedAt() { return openedAt; }
}
