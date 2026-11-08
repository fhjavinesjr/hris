package com.primehr.learningdevelopment.completion.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name="ld_completion_evidence")
public class LdCompletionEvidence extends RspAuditedEntity{
    public enum Type{OUTPUT,CERTIFICATE,ATTENDANCE,OTHER}
    @Column(name="completion_case_id",nullable=false,length=36)private String completionCaseId;
    @Column(name="output_id",length=36)private String outputId;
    @Enumerated(EnumType.STRING)@Column(name="evidence_type",nullable=false,length=20)private Type evidenceType;
    @Column(name="original_filename",nullable=false,length=255)private String originalFilename;
    @Column(name="storage_provider",nullable=false,length=30)private String storageProvider;
    @Column(name="storage_object_key",nullable=false,length=500)private String storageObjectKey;
    @Column(name="media_type",nullable=false,length=120)private String mediaType;
    @Column(name="byte_size",nullable=false)private long byteSize;
    @Column(nullable=false,length=64)private String checksum;
    @Column(name="certificate_number",length=200)private String certificateNumber;
    @Column(name="certificate_issuer",length=300)private String certificateIssuer;
    @Column(name="certificate_issue_date")private LocalDate certificateIssueDate;
    @Column(name="uploaded_by",nullable=false,length=100)private String uploadedBy;
    @Column(name="uploaded_at",nullable=false)private Instant uploadedAt;
    protected LdCompletionEvidence(){}
    public LdCompletionEvidence(String agency,String caseId,String outputId,Type type,String filename,String provider,String key,String media,long size,String checksum,String certificateNumber,String certificateIssuer,LocalDate certificateIssueDate,String actor,Instant at){super(agency);completionCaseId=requiredText(caseId,"completionCaseId");evidenceType=java.util.Objects.requireNonNull(type,"evidenceType");this.outputId=optionalText(outputId);if(type==Type.OUTPUT&&this.outputId==null)throw new IllegalArgumentException("Output evidence requires outputId");if(type!=Type.OUTPUT&&this.outputId!=null)throw new IllegalArgumentException("outputId is allowed only for output evidence");this.certificateNumber=optionalText(certificateNumber);this.certificateIssuer=optionalText(certificateIssuer);this.certificateIssueDate=certificateIssueDate;if(type==Type.CERTIFICATE&&(this.certificateNumber==null||this.certificateIssuer==null||certificateIssueDate==null))throw new IllegalArgumentException("Certificate number, issuer and issue date are required");originalFilename=requiredText(filename,"originalFilename");storageProvider=requiredText(provider,"storageProvider");storageObjectKey=requiredText(key,"storageObjectKey");mediaType=requiredText(media,"mediaType");if(size<1)throw new IllegalArgumentException("byteSize must be positive");byteSize=size;this.checksum=requiredText(checksum,"checksum");uploadedBy=requiredText(actor,"actor");uploadedAt=at==null?Instant.now():at;}
    public String getCompletionCaseId(){return completionCaseId;}public String getOutputId(){return outputId;}public Type getEvidenceType(){return evidenceType;}public String getOriginalFilename(){return originalFilename;}public String getStorageProvider(){return storageProvider;}public String getStorageObjectKey(){return storageObjectKey;}public String getMediaType(){return mediaType;}public long getByteSize(){return byteSize;}public String getChecksum(){return checksum;}public String getCertificateNumber(){return certificateNumber;}public String getCertificateIssuer(){return certificateIssuer;}public LocalDate getCertificateIssueDate(){return certificateIssueDate;}public String getUploadedBy(){return uploadedBy;}public Instant getUploadedAt(){return uploadedAt;}
}
