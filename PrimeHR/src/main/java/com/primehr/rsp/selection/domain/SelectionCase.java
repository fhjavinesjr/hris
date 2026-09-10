package com.primehr.rsp.selection.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "rsp_selection_case")
public class SelectionCase extends RspAuditedEntity {
    public enum Status { DRAFT, SUBMITTED, RETURNED, APPROVED, FINALIZED, CANCELLED, SUPERSEDED }
    public enum Outcome { SELECTED, NO_SELECTION, DEFERRED }

    @Column(name="proceeding_id",nullable=false,length=36) private String proceedingId;
    @Column(name="vacancy_publication_id",nullable=false,length=36) private String publicationId;
    @Column(name="comparative_evaluation_id",nullable=false,length=36) private String comparativeEvaluationId;
    @Column(name="comparative_fingerprint",nullable=false,length=64) private String comparativeFingerprint;
    @Column(name="meeting_id",nullable=false,length=36) private String meetingId;
    @Column(name="case_revision",nullable=false) private int caseRevision;
    @Column(name="supersedes_id",length=36) private String supersedesId;
    @Column(name="current_publication_key",length=36) private String currentPublicationKey;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Enumerated(EnumType.STRING) @Column(length=20) private Outcome outcome;
    @Column(name="selected_candidate_id",length=36) private String selectedCandidateId;
    @Column(name="assigned_approver_employee_no",nullable=false,length=100) private String assignedApproverEmployeeNo;
    @Nationalized @Column(name="decision_reason",length=3000) private String decisionReason;
    @Nationalized @Column(name="variance_basis",length=1000) private String varianceBasis;
    @Nationalized @Column(name="variance_reason",length=3000) private String varianceReason;
    @Column(name="review_date") private LocalDate reviewDate;
    @Column(name="offer_response_deadline",nullable=false) private Instant offerResponseDeadline;
    @Nationalized @Column(name="selected_safe_text",nullable=false,length=2000) private String selectedSafeText;
    @Nationalized @Column(name="non_selected_safe_text",nullable=false,length=2000) private String nonSelectedSafeText;
    @Nationalized @Column(name="source_snapshot",nullable=false,length=32000) private String sourceSnapshot;
    @Column(name="source_fingerprint",nullable=false,length=64) private String sourceFingerprint;
    @Column(name="submitted_by",length=100) private String submittedBy;
    @Column(name="submitted_at") private Instant submittedAt;
    @Column(name="approved_by",length=100) private String approvedBy;
    @Column(name="approved_at") private Instant approvedAt;
    @Column(name="finalized_by",length=100) private String finalizedBy;
    @Column(name="finalized_at") private Instant finalizedAt;
    @Column(name="returned_by",length=100) private String returnedBy;
    @Column(name="returned_at") private Instant returnedAt;
    @Nationalized @Column(name="return_reason",length=2000) private String returnReason;
    @Nationalized @Column(name="administrator_exception_reason",length=2000) private String administratorExceptionReason;

    protected SelectionCase() {}

    public SelectionCase(String agency, String proceedingId, String publicationId, String comparativeEvaluationId,
                         String comparativeFingerprint, String meetingId, int revision, String supersedesId,
                         String assignedApprover, Instant offerDeadline, String selectedSafeText,
                         String nonSelectedSafeText, String sourceSnapshot, String sourceFingerprint) {
        super(agency);
        this.proceedingId=requiredText(proceedingId,"proceedingId");
        this.publicationId=requiredText(publicationId,"publicationId");
        this.comparativeEvaluationId=requiredText(comparativeEvaluationId,"comparativeEvaluationId");
        this.comparativeFingerprint=requiredText(comparativeFingerprint,"comparativeFingerprint");
        this.meetingId=requiredText(meetingId,"meetingId");
        if(revision<1) throw new IllegalArgumentException("caseRevision must be positive");
        caseRevision=revision;
        this.supersedesId=optionalText(supersedesId);
        currentPublicationKey=publicationId;
        assignedApproverEmployeeNo=requiredText(assignedApprover,"assignedApproverEmployeeNo");
        offerResponseDeadline=Objects.requireNonNull(offerDeadline,"offerResponseDeadline");
        if(!offerDeadline.isAfter(Instant.now())) throw new IllegalArgumentException("offerResponseDeadline must be in the future");
        this.selectedSafeText=requiredText(selectedSafeText,"selectedSafeText");
        this.nonSelectedSafeText=requiredText(nonSelectedSafeText,"nonSelectedSafeText");
        this.sourceSnapshot=requiredText(sourceSnapshot,"sourceSnapshot");
        this.sourceFingerprint=requiredText(sourceFingerprint,"sourceFingerprint");
        status=Status.DRAFT;
    }

    public void decide(Outcome value,String selectedCandidate,String reason,String basis,String varianceReason,LocalDate reviewDate){
        requireEditable(); outcome=Objects.requireNonNull(value,"outcome"); decisionReason=requiredText(reason,"decisionReason");
        if(value==Outcome.SELECTED){selectedCandidateId=requiredText(selectedCandidate,"selectedCandidateId");this.reviewDate=null;}
        else {if(selectedCandidate!=null)throw new IllegalArgumentException("selectedCandidateId is valid only for SELECTED");selectedCandidateId=null;this.reviewDate=value==Outcome.DEFERRED?Objects.requireNonNull(reviewDate,"reviewDate"):null;}
        varianceBasis=optionalText(basis);this.varianceReason=optionalText(varianceReason);
        if((varianceBasis==null)!=(this.varianceReason==null))throw new IllegalArgumentException("varianceBasis and varianceReason must be supplied together");
    }
    public void submit(String actor,Instant at){requireEditable();if(outcome==null)throw new IllegalLifecycleTransitionException("A decision is required before submission");submittedBy=requiredText(actor,"actor");submittedAt=Objects.requireNonNull(at);returnedBy=null;returnedAt=null;returnReason=null;status=Status.SUBMITTED;}
    public void approve(String actor,boolean administrator,String exceptionReason,Instant at){if(status!=Status.SUBMITTED)throw new IllegalLifecycleTransitionException("Only a SUBMITTED selection may be approved");boolean assigned=assignedApproverEmployeeNo.equalsIgnoreCase(actor);boolean self=submittedBy.equalsIgnoreCase(actor);if((!assigned||self)&&!administrator)throw new IllegalLifecycleTransitionException("The assigned appointing authority must be independent from the submitter");if(!assigned||self)administratorExceptionReason=requiredText(exceptionReason,"administratorExceptionReason");approvedBy=requiredText(actor,"actor");approvedAt=Objects.requireNonNull(at);status=Status.APPROVED;}
    public void returnForCorrection(String actor,String reason,Instant at){if(status!=Status.SUBMITTED)throw new IllegalLifecycleTransitionException("Only a SUBMITTED selection may be returned");returnedBy=requiredText(actor,"actor");returnedAt=Objects.requireNonNull(at);returnReason=requiredText(reason,"reason");status=Status.RETURNED;}
    public void finalizeCase(String actor,Instant at){if(status!=Status.APPROVED)throw new IllegalLifecycleTransitionException("Only an APPROVED selection may be finalized");finalizedBy=requiredText(actor,"actor");finalizedAt=Objects.requireNonNull(at);status=Status.FINALIZED;}
    public void cancel(String reason){if(status==Status.FINALIZED||status==Status.CANCELLED||status==Status.SUPERSEDED)throw new IllegalLifecycleTransitionException("A final selection cannot be cancelled");decisionReason=requiredText(reason,"reason");currentPublicationKey=null;status=Status.CANCELLED;}
    public void supersede(){if(status!=Status.FINALIZED)throw new IllegalLifecycleTransitionException("Only a FINALIZED selection may be superseded");currentPublicationKey=null;status=Status.SUPERSEDED;}
    private void requireEditable(){if(status!=Status.DRAFT&&status!=Status.RETURNED)throw new IllegalLifecycleTransitionException("Only a DRAFT or RETURNED selection may be edited");}

    public String getProceedingId(){return proceedingId;} public String getPublicationId(){return publicationId;} public String getComparativeEvaluationId(){return comparativeEvaluationId;} public String getComparativeFingerprint(){return comparativeFingerprint;} public String getMeetingId(){return meetingId;} public int getCaseRevision(){return caseRevision;} public String getSupersedesId(){return supersedesId;} public Status getStatus(){return status;} public Outcome getOutcome(){return outcome;} public String getSelectedCandidateId(){return selectedCandidateId;} public String getAssignedApproverEmployeeNo(){return assignedApproverEmployeeNo;} public String getDecisionReason(){return decisionReason;} public String getVarianceBasis(){return varianceBasis;} public String getVarianceReason(){return varianceReason;} public LocalDate getReviewDate(){return reviewDate;} public Instant getOfferResponseDeadline(){return offerResponseDeadline;} public String getSelectedSafeText(){return selectedSafeText;} public String getNonSelectedSafeText(){return nonSelectedSafeText;} public String getSourceSnapshot(){return sourceSnapshot;} public String getSourceFingerprint(){return sourceFingerprint;} public String getSubmittedBy(){return submittedBy;} public Instant getSubmittedAt(){return submittedAt;} public String getApprovedBy(){return approvedBy;} public Instant getApprovedAt(){return approvedAt;} public String getFinalizedBy(){return finalizedBy;} public Instant getFinalizedAt(){return finalizedAt;} public String getReturnReason(){return returnReason;} public String getAdministratorExceptionReason(){return administratorExceptionReason;}
}
