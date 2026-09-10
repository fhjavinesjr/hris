package com.primehr.rsp.handoff.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "rsp_appointment_handoff")
public class AppointmentHandoff extends RspAuditedEntity {
    public enum Status { DRAFT, READY, SENT, ACKNOWLEDGED, RETRYABLE_FAILURE, CANCELLED, CLOSED }

    @Column(name="selection_case_id",nullable=false,length=36) private String selectionCaseId;
    @Column(name="application_id",nullable=false,length=36) private String applicationId;
    @Column(name="applicant_id",nullable=false,length=36) private String applicantId;
    @Column(name="schema_version",nullable=false) private int schemaVersion;
    @Column(name="handoff_revision",nullable=false) private int handoffRevision;
    @Column(name="current_selection_key",length=36) private String currentSelectionKey;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private Status status;
    @Nationalized @Column(name="payload_snapshot",nullable=false,length=32000) private String payloadSnapshot;
    @Column(name="payload_fingerprint",nullable=false,length=64) private String payloadFingerprint;
    @Column(name="correlation_id",length=100) private String correlationId;
    @Column(name="receipt_id",length=36) private String receiptId;
    @Column(name="receipt_state",length=30) private String receiptState;
    @Column(name="receipt_record_version") private Long receiptRecordVersion;
    @Column(name="ready_at") private Instant readyAt;
    @Column(name="sent_at") private Instant sentAt;
    @Column(name="acknowledged_at") private Instant acknowledgedAt;
    @Column(name="cancelled_at") private Instant cancelledAt;
    @Column(name="closed_at") private Instant closedAt;
    @Nationalized @Column(name="last_failure",length=1000) private String lastFailure;

    protected AppointmentHandoff() {}

    public AppointmentHandoff(String id, String agency, String selectionCaseId, String applicationId, String applicantId,
                              int revision, String payload, String fingerprint, String correlationId) {
        super(id,agency);
        this.selectionCaseId=requiredText(selectionCaseId,"selectionCaseId");
        this.applicationId=requiredText(applicationId,"applicationId");
        this.applicantId=requiredText(applicantId,"applicantId");
        if(revision<1) throw new IllegalArgumentException("handoffRevision must be positive");
        schemaVersion=1;handoffRevision=revision;currentSelectionKey=selectionCaseId;status=Status.DRAFT;
        payloadSnapshot=requiredText(payload,"payloadSnapshot");payloadFingerprint=requiredText(fingerprint,"payloadFingerprint");
        this.correlationId=optionalText(correlationId);
    }

    public void ready(Instant at){if(status!=Status.DRAFT)throw invalid("Only a DRAFT handoff may become ready");readyAt=Objects.requireNonNull(at);status=Status.READY;}
    public void sent(Instant at){if(status!=Status.READY&&status!=Status.RETRYABLE_FAILURE)throw invalid("Only a READY or RETRYABLE_FAILURE handoff may be sent");sentAt=Objects.requireNonNull(at);lastFailure=null;status=Status.SENT;}
    public void acknowledge(String receipt,String state,long version,Instant at){if(status!=Status.SENT&&status!=Status.RETRYABLE_FAILURE)throw invalid("Only a delivered handoff may be acknowledged");receiptId=requiredText(receipt,"receiptId");receiptState=requiredText(state,"receiptState");receiptRecordVersion=version;acknowledgedAt=Objects.requireNonNull(at);lastFailure=null;status=Status.ACKNOWLEDGED;}
    public void deliveryFailed(String diagnostic){if(status!=Status.SENT)throw invalid("Only a SENT handoff may record delivery failure");lastFailure=requiredText(diagnostic,"diagnostic");status=Status.RETRYABLE_FAILURE;}
    public void reconcileReceipt(String state,long version){if(status!=Status.ACKNOWLEDGED)throw invalid("Only an ACKNOWLEDGED handoff may reconcile receipt state");receiptState=requiredText(state,"receiptState");receiptRecordVersion=version;}
    public void cancel(Instant at){if(status!=Status.READY&&status!=Status.RETRYABLE_FAILURE&&status!=Status.DRAFT)throw invalid("Only an unacknowledged handoff may be cancelled");cancelledAt=Objects.requireNonNull(at);currentSelectionKey=null;status=Status.CANCELLED;}
    public void close(Instant at){if(status!=Status.ACKNOWLEDGED||!"COMPLETED".equals(receiptState))throw invalid("Only a completed HRM receipt may close the handoff");closedAt=Objects.requireNonNull(at);currentSelectionKey=null;status=Status.CLOSED;}
    private IllegalLifecycleTransitionException invalid(String value){return new IllegalLifecycleTransitionException(value);}

    public String getSelectionCaseId(){return selectionCaseId;} public String getApplicationId(){return applicationId;} public String getApplicantId(){return applicantId;} public int getSchemaVersion(){return schemaVersion;} public int getHandoffRevision(){return handoffRevision;} public Status getStatus(){return status;} public String getPayloadSnapshot(){return payloadSnapshot;} public String getPayloadFingerprint(){return payloadFingerprint;} public String getCorrelationId(){return correlationId;} public String getReceiptId(){return receiptId;} public String getReceiptState(){return receiptState;} public Long getReceiptRecordVersion(){return receiptRecordVersion;} public Instant getReadyAt(){return readyAt;} public Instant getSentAt(){return sentAt;} public Instant getAcknowledgedAt(){return acknowledgedAt;} public Instant getCancelledAt(){return cancelledAt;} public Instant getClosedAt(){return closedAt;} public String getLastFailure(){return lastFailure;}
}
