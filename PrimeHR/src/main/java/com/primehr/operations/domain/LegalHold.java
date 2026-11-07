package com.primehr.operations.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="primehr_legal_hold")
public class LegalHold extends RspAuditedEntity {
    @Column(name="record_type",nullable=false,length=80) private String recordType;
    @Column(name="record_id",nullable=false,length=100) private String recordId;
    @Column(nullable=false,length=2000) private String reason;
    @Column(name="reference_no",nullable=false,length=200) private String referenceNo;
    @Column(name="placed_by",nullable=false,length=100) private String placedBy;
    @Column(name="placed_at",nullable=false) private Instant placedAt;
    @Column(nullable=false) private boolean active;
    @Column(name="released_by",length=100) private String releasedBy;
    @Column(name="released_at") private Instant releasedAt;
    @Column(name="release_reason",length=2000) private String releaseReason;
    protected LegalHold() {}
    public LegalHold(String agency,String type,String id,String reason,String reference,String actor,Instant at){super(agency);recordType=requiredText(type,"recordType");recordId=requiredText(id,"recordId");this.reason=requiredText(reason,"reason");referenceNo=requiredText(reference,"referenceNo");placedBy=requiredText(actor,"actor");placedAt=at;active=true;}
    public void release(String actor,String reason,Instant at){if(!active)throw new IllegalStateException("Legal hold is already released");active=false;releasedBy=requiredText(actor,"actor");releaseReason=requiredText(reason,"releaseReason");releasedAt=at;}
    public String getRecordType(){return recordType;} public String getRecordId(){return recordId;} public String getReason(){return reason;} public String getReferenceNo(){return referenceNo;} public String getPlacedBy(){return placedBy;} public Instant getPlacedAt(){return placedAt;} public boolean isActive(){return active;} public String getReleasedBy(){return releasedBy;} public Instant getReleasedAt(){return releasedAt;} public String getReleaseReason(){return releaseReason;}
}
