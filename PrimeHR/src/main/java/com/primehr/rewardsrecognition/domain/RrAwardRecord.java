package com.primehr.rewardsrecognition.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "rr_award_record", uniqueConstraints = {
        @UniqueConstraint(name = "uk_rr_award_revision", columnNames = {"agency_id", "award_code", "revision_no"}),
        @UniqueConstraint(name = "uk_rr_award_record_key", columnNames = {"agency_id", "record_key"})
})
public class RrAwardRecord extends RspAuditedEntity {
    public enum RecordType { CONFERMENT, CORRECTION, REVOCATION }

    @Column(name="evaluation_case_id",nullable=false,length=36) private String evaluationCaseId;
    @Column(name="nomination_id",nullable=false,length=36) private String nominationId;
    @Column(name="cycle_id",nullable=false,length=36) private String cycleId;
    @Column(name="category_version_id",nullable=false,length=36) private String categoryVersionId;
    @Column(name="award_code",nullable=false,length=80) private String awardCode;
    @Column(name="revision_no",nullable=false) private int revisionNo;
    @Column(name="original_record",nullable=false) private boolean originalRecord;
    @Column(name="record_key",nullable=false,length=80) private String recordKey;
    @Column(name="root_award_id",nullable=false,length=36) private String rootAwardId;
    @Column(name="supersedes_award_id",length=36) private String supersedesAwardId;
    @Enumerated(EnumType.STRING) @Column(name="record_type",nullable=false,length=20) private RecordType recordType;
    @Column(name="nominee_type",nullable=false,length=30) private String nomineeType;
    @Column(name="nominee_display_name",nullable=false,length=500) private String nomineeDisplayName;
    @Column(name="category_title",nullable=false,length=300) private String categoryTitle;
    @Column(name="certificate_title",length=500) private String certificateTitle;
    @Column(name="publication_citation",length=6000) private String publicationCitation;
    @Column(name="conferred_on") private LocalDate conferredOn;
    @Column(name="change_reason",nullable=false,length=4000) private String changeReason;
    @Column(name="authorized_by",nullable=false,length=100) private String authorizedBy;
    @Column(name="authorized_at",nullable=false) private Instant authorizedAt;
    @Column(name="source_fingerprint",nullable=false,length=64) private String sourceFingerprint;

    protected RrAwardRecord() {}

    private RrAwardRecord(String id,String agency,String evaluationCaseId,String nominationId,String cycleId,
                          String categoryVersionId,String awardCode,int revisionNo,boolean originalRecord,
                          String rootAwardId,String supersedesAwardId,RecordType recordType,String nomineeType,
                          String nomineeDisplayName,String categoryTitle,String certificateTitle,
                          String publicationCitation,LocalDate conferredOn,String reason,String actor,Instant at,
                          String sourceFingerprint) {
        super(id,agency);this.evaluationCaseId=requiredText(evaluationCaseId,"evaluationCaseId");
        this.nominationId=requiredText(nominationId,"nominationId");this.cycleId=requiredText(cycleId,"cycleId");
        this.categoryVersionId=requiredText(categoryVersionId,"categoryVersionId");this.awardCode=requiredText(awardCode,"awardCode");
        if(revisionNo<1)throw new IllegalArgumentException("revisionNo must be positive");this.revisionNo=revisionNo;
        this.originalRecord=originalRecord;this.rootAwardId=requiredText(rootAwardId,"rootAwardId");
        this.supersedesAwardId=optionalText(supersedesAwardId);this.recordKey=originalRecord?"CASE:"+this.evaluationCaseId:"SUPERSEDES:"+requiredText(supersedesAwardId,"supersedesAwardId");this.recordType=java.util.Objects.requireNonNull(recordType,"recordType");
        this.nomineeType=requiredText(nomineeType,"nomineeType");this.nomineeDisplayName=requiredText(nomineeDisplayName,"nomineeDisplayName");
        this.categoryTitle=requiredText(categoryTitle,"categoryTitle");this.certificateTitle=optionalText(certificateTitle);
        this.publicationCitation=optionalText(publicationCitation);this.conferredOn=conferredOn;
        this.changeReason=requiredText(reason,"reason");this.authorizedBy=requiredText(actor,"actor");
        this.authorizedAt=java.util.Objects.requireNonNull(at,"authorizedAt");this.sourceFingerprint=requiredText(sourceFingerprint,"sourceFingerprint");
        if(recordType!=RecordType.REVOCATION&&(this.certificateTitle==null||this.publicationCitation==null||conferredOn==null))
            throw new IllegalArgumentException("Conferment and correction records require certificate, citation, and conferment date");
    }

    public static RrAwardRecord confer(String agency,String evaluationCaseId,String nominationId,String cycleId,
                                        String categoryVersionId,String awardCode,String nomineeType,String nomineeName,
                                        String categoryTitle,String certificateTitle,String citation,LocalDate conferredOn,
                                        String reason,String actor,Instant at,String fingerprint) {
        String id=UUID.randomUUID().toString();return new RrAwardRecord(id,agency,evaluationCaseId,nominationId,cycleId,
                categoryVersionId,awardCode,1,true,id,null,RecordType.CONFERMENT,nomineeType,nomineeName,categoryTitle,
                certificateTitle,citation,conferredOn,reason,actor,at,fingerprint);
    }

    public RrAwardRecord correct(String certificateTitle,String citation,LocalDate conferredOn,String reason,
                                 String actor,Instant at,String fingerprint) {
        if(recordType==RecordType.REVOCATION)throw new IllegalStateException("A revoked award cannot be corrected");
        return new RrAwardRecord(UUID.randomUUID().toString(),getAgencyId(),evaluationCaseId,nominationId,cycleId,
                categoryVersionId,awardCode,revisionNo+1,false,rootAwardId,getId(),RecordType.CORRECTION,nomineeType,
                nomineeDisplayName,categoryTitle,certificateTitle,citation,conferredOn,reason,actor,at,fingerprint);
    }

    public RrAwardRecord revoke(String reason,String actor,Instant at,String fingerprint) {
        if(recordType==RecordType.REVOCATION)throw new IllegalStateException("Award is already revoked");
        return new RrAwardRecord(UUID.randomUUID().toString(),getAgencyId(),evaluationCaseId,nominationId,cycleId,
                categoryVersionId,awardCode,revisionNo+1,false,rootAwardId,getId(),RecordType.REVOCATION,nomineeType,
                nomineeDisplayName,categoryTitle,null,null,null,reason,actor,at,fingerprint);
    }

    public String getEvaluationCaseId(){return evaluationCaseId;} public String getNominationId(){return nominationId;}
    public String getCycleId(){return cycleId;} public String getCategoryVersionId(){return categoryVersionId;}
    public String getAwardCode(){return awardCode;} public int getRevisionNo(){return revisionNo;}
    public boolean isOriginalRecord(){return originalRecord;} public String getRootAwardId(){return rootAwardId;}
    public String getSupersedesAwardId(){return supersedesAwardId;} public RecordType getRecordType(){return recordType;}
    public String getNomineeType(){return nomineeType;} public String getNomineeDisplayName(){return nomineeDisplayName;}
    public String getCategoryTitle(){return categoryTitle;} public String getCertificateTitle(){return certificateTitle;}
    public String getPublicationCitation(){return publicationCitation;} public LocalDate getConferredOn(){return conferredOn;}
    public String getChangeReason(){return changeReason;} public String getAuthorizedBy(){return authorizedBy;}
    public Instant getAuthorizedAt(){return authorizedAt;} public String getSourceFingerprint(){return sourceFingerprint;}
}
