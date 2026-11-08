package com.primehr.operations.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="primehr_reconciliation_finding")
public class ReconciliationFinding extends RspAuditedEntity {
    public enum Kind { SOURCE_DRIFT, SOURCE_MISSING, SOURCE_UNAVAILABLE, STORAGE_MISSING, STORAGE_SIZE_MISMATCH, STORAGE_UNAVAILABLE }
    public enum Status { OPEN, ACKNOWLEDGED, RESOLVED }
    @Column(name="run_id",nullable=false,length=36) private String runId;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=40) private Kind kind;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Column(name="record_type",nullable=false,length=80) private String recordType;
    @Column(name="record_id",nullable=false,length=100) private String recordId;
    @Column(name="source_id",length=100) private String sourceId;
    @Column(name="stored_version") private Long storedVersion;
    @Column(name="stored_fingerprint",length=64) private String storedFingerprint;
    @Column(name="observed_version") private Long observedVersion;
    @Column(name="observed_fingerprint",length=64) private String observedFingerprint;
    @Column(nullable=false,length=1000) private String summary;
    @Column(name="acknowledged_by",length=100) private String acknowledgedBy;
    @Column(name="acknowledged_at") private Instant acknowledgedAt;
    @Column(name="resolved_by",length=100) private String resolvedBy;
    @Column(name="resolved_at") private Instant resolvedAt;
    @Column(name="resolution_note",length=2000) private String resolutionNote;
    protected ReconciliationFinding() {}
    public ReconciliationFinding(String agency,String run,Kind kind,String type,String record,String source,Long storedVersion,String storedFingerprint,Long observedVersion,String observedFingerprint,String summary){super(agency);runId=requiredText(run,"runId");this.kind=kind;status=Status.OPEN;recordType=requiredText(type,"recordType");recordId=requiredText(record,"recordId");sourceId=optionalText(source);this.storedVersion=storedVersion;this.storedFingerprint=optionalText(storedFingerprint);this.observedVersion=observedVersion;this.observedFingerprint=optionalText(observedFingerprint);this.summary=requiredText(summary,"summary");}
    public void acknowledge(String actor,Instant at){if(status==Status.RESOLVED)throw new IllegalStateException("Resolved finding cannot be acknowledged");status=Status.ACKNOWLEDGED;acknowledgedBy=requiredText(actor,"actor");acknowledgedAt=at;}
    public void resolve(String actor,String note,Instant at){status=Status.RESOLVED;resolvedBy=requiredText(actor,"actor");resolutionNote=requiredText(note,"resolutionNote");resolvedAt=at;}
    public String getRunId(){return runId;} public Kind getKind(){return kind;} public Status getStatus(){return status;} public String getRecordType(){return recordType;} public String getRecordId(){return recordId;} public String getSourceId(){return sourceId;} public Long getStoredVersion(){return storedVersion;} public String getStoredFingerprint(){return storedFingerprint;} public Long getObservedVersion(){return observedVersion;} public String getObservedFingerprint(){return observedFingerprint;} public String getSummary(){return summary;} public String getAcknowledgedBy(){return acknowledgedBy;} public Instant getAcknowledgedAt(){return acknowledgedAt;} public String getResolvedBy(){return resolvedBy;} public Instant getResolvedAt(){return resolvedAt;} public String getResolutionNote(){return resolutionNote;}
}
