package com.primehr.rsp.screening.domain;

import com.primehr.rsp.applicant.domain.PositionApplication;
import com.primehr.rsp.domain.RspAuditedEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "rsp_screening_case", uniqueConstraints = {
        @UniqueConstraint(name = "uk_rsp_screening_case_revision", columnNames = {"agency_id", "application_id", "case_revision"})
})
public class ScreeningCase extends RspAuditedEntity {
    public enum Status { DRAFT, RETURNED, SUBMITTED, QUALIFIED, DISQUALIFIED, CANCELLED }
    public enum Outcome { QUALIFIED, DISQUALIFIED }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private PositionApplication application;
    @Column(name = "vacancy_publication_id", nullable = false, length = 36) private String publicationId;
    @Column(name = "screening_policy_id", nullable = false, length = 36) private String policyId;
    @Column(name = "policy_definition_version", nullable = false) private int policyDefinitionVersion;
    @Column(name = "case_revision", nullable = false) private int caseRevision;
    @Column(name = "supersedes_id", length = 36) private String supersedesId;
    @Column(name = "current_application_key", length = 36) private String currentApplicationKey;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Status status;
    @Enumerated(EnumType.STRING) @Column(length = 20) private Outcome recommendation;
    @Column(name = "recommendation_reason_code_id", length = 36) private String recommendationReasonCodeId;
    @Column(name = "recommendation_reason_code", length = 80) private String recommendationReasonCode;
    @Nationalized @Column(name = "recommendation_explanation", length = 2000) private String recommendationExplanation;
    @Nationalized @Column(name = "recommendation_safe_reason", length = 1000) private String recommendationSafeReason;
    @Nationalized @Column(name = "policy_snapshot", nullable = false, length = 32000) private String policySnapshot;
    @Nationalized @Column(name = "application_snapshot", nullable = false, length = 32000) private String applicationSnapshot;
    @Column(name = "opened_by", nullable = false, length = 100) private String openedBy;
    @Column(name = "opened_at", nullable = false) private Instant openedAt;
    @Column(name = "submitted_by", length = 100) private String submittedBy;
    @Column(name = "submitted_at") private Instant submittedAt;
    @Column(name = "returned_by", length = 100) private String returnedBy;
    @Column(name = "returned_at") private Instant returnedAt;
    @Nationalized @Column(name = "return_reason", length = 2000) private String returnReason;
    @Column(name = "finalized_by", length = 100) private String finalizedBy;
    @Column(name = "finalized_at") private Instant finalizedAt;
    @Column(name = "cancelled_by", length = 100) private String cancelledBy;
    @Column(name = "cancelled_at") private Instant cancelledAt;
    @Nationalized @Column(name = "cancellation_reason", length = 2000) private String cancellationReason;

    protected ScreeningCase() {}

    public ScreeningCase(String agencyId, PositionApplication application, String publicationId,
                         String policyId, int policyDefinitionVersion, int caseRevision, String supersedesId,
                         String policySnapshot, String applicationSnapshot, String actor, Instant at) {
        super(agencyId);
        this.application = Objects.requireNonNull(application, "application");
        this.publicationId = requiredText(publicationId, "publicationId");
        this.policyId = requiredText(policyId, "policyId");
        if (policyDefinitionVersion < 1 || caseRevision < 1) throw new IllegalArgumentException("Versions must be positive");
        this.policyDefinitionVersion = policyDefinitionVersion;
        this.caseRevision = caseRevision;
        this.supersedesId = optionalText(supersedesId);
        this.currentApplicationKey = application.getId();
        this.policySnapshot = requiredText(policySnapshot, "policySnapshot");
        this.applicationSnapshot = requiredText(applicationSnapshot, "applicationSnapshot");
        this.openedBy = requiredText(actor, "actor");
        this.openedAt = Objects.requireNonNull(at, "at");
        this.status = Status.DRAFT;
    }

    public void submit(Outcome outcome, String reasonCodeId, String reasonCode, String explanation,
                       String safeReason, String actor, Instant at) {
        requireEditable();
        recommendation = Objects.requireNonNull(outcome, "recommendation");
        recommendationReasonCodeId = optionalText(reasonCodeId); recommendationReasonCode = optionalText(reasonCode);
        recommendationExplanation = optionalText(explanation); recommendationSafeReason = requiredText(safeReason, "applicantSafeReason");
        submittedBy = requiredText(actor, "actor"); submittedAt = Objects.requireNonNull(at, "at");
        returnedBy = null; returnedAt = null; returnReason = null; status = Status.SUBMITTED;
    }
    public void returnToScreener(String actor, String reason, Instant at) {
        require(Status.SUBMITTED);
        returnedBy = requiredText(actor, "actor"); returnReason = requiredText(reason, "reason");
        returnedAt = Objects.requireNonNull(at, "at"); status = Status.RETURNED;
    }
    public void finalizeDecision(Outcome outcome, String actor, Instant at) {
        require(Status.SUBMITTED);
        if (recommendation != outcome) throw new IllegalLifecycleTransitionException("Validator outcome must match the submitted recommendation");
        finalizedBy = requiredText(actor, "actor"); finalizedAt = Objects.requireNonNull(at, "at");
        status = outcome == Outcome.QUALIFIED ? Status.QUALIFIED : Status.DISQUALIFIED;
    }
    public void overrideDecision(Outcome outcome, String actor, Instant at) {
        require(Status.SUBMITTED); finalizedBy = requiredText(actor, "actor"); finalizedAt = Objects.requireNonNull(at, "at");
        status = outcome == Outcome.QUALIFIED ? Status.QUALIFIED : Status.DISQUALIFIED;
    }
    public void cancel(String actor, String reason, Instant at) {
        if (status == Status.QUALIFIED || status == Status.DISQUALIFIED || status == Status.CANCELLED) {
            throw new IllegalLifecycleTransitionException("A final screening case cannot be cancelled");
        }
        cancelledBy = requiredText(actor, "actor"); cancellationReason = requiredText(reason, "reason");
        cancelledAt = Objects.requireNonNull(at, "at"); status = Status.CANCELLED; currentApplicationKey = null;
    }
    public void supersede() {
        if (status != Status.QUALIFIED && status != Status.DISQUALIFIED) throw new IllegalLifecycleTransitionException("Only a final case may be superseded");
        currentApplicationKey = null;
    }
    private void requireEditable() { if (status != Status.DRAFT && status != Status.RETURNED) throw new IllegalLifecycleTransitionException("Only DRAFT or RETURNED screening may be submitted"); }
    private void require(Status expected) { if (status != expected) throw new IllegalLifecycleTransitionException("Screening case must be " + expected); }

    public PositionApplication getApplication(){return application;} public String getPublicationId(){return publicationId;}
    public String getPolicyId(){return policyId;} public int getPolicyDefinitionVersion(){return policyDefinitionVersion;}
    public int getCaseRevision(){return caseRevision;} public String getSupersedesId(){return supersedesId;}
    public boolean isCurrent(){return currentApplicationKey != null;} public Status getStatus(){return status;}
    public Outcome getRecommendation(){return recommendation;} public String getPolicySnapshot(){return policySnapshot;}
    public String getRecommendationReasonCodeId(){return recommendationReasonCodeId;} public String getRecommendationReasonCode(){return recommendationReasonCode;}
    public String getRecommendationExplanation(){return recommendationExplanation;} public String getRecommendationSafeReason(){return recommendationSafeReason;}
    public String getApplicationSnapshot(){return applicationSnapshot;} public String getOpenedBy(){return openedBy;}
    public Instant getOpenedAt(){return openedAt;} public String getSubmittedBy(){return submittedBy;}
    public Instant getSubmittedAt(){return submittedAt;} public String getReturnedBy(){return returnedBy;}
    public Instant getReturnedAt(){return returnedAt;} public String getReturnReason(){return returnReason;}
    public String getFinalizedBy(){return finalizedBy;} public Instant getFinalizedAt(){return finalizedAt;}
    public String getCancelledBy(){return cancelledBy;} public Instant getCancelledAt(){return cancelledAt;}
    public String getCancellationReason(){return cancellationReason;}
}
