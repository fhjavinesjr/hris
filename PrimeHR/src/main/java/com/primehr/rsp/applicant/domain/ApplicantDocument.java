package com.primehr.rsp.applicant.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="rsp_applicant_document",indexes=@Index(name="ix_rsp_document_owner",columnList="agency_id,applicant_id,active"))
public class ApplicantDocument extends RspAuditedEntity {
    public enum ScanStatus { PENDING_SCAN, CLEAN, REJECTED }
    @Column(name="applicant_id",nullable=false,length=36) private String applicantId;
    @Column(name="document_type",nullable=false,length=100) private String documentType;
    @Column(name="original_filename",nullable=false,length=255) private String originalFilename;
    @Column(name="storage_provider",nullable=false,length=20) private String storageProvider;
    @Column(name="storage_object_key",nullable=false,unique=true,length=500) private String storageObjectKey;
    @Column(name="media_type",nullable=false,length=150) private String mediaType;
    @Column(name="byte_size",nullable=false) private long byteSize;
    @Column(name="sha256_checksum",nullable=false,length=64) private String checksum;
    @Column(nullable=false,length=30) private String classification;
    @Enumerated(EnumType.STRING) @Column(name="scan_status",nullable=false,length=20) private ScanStatus scanStatus;
    @Column(name="uploaded_at",nullable=false) private Instant uploadedAt;
    @Column(nullable=false) private boolean active;
    @Column(name="replaces_document_id",length=36) private String replacesDocumentId;
    protected ApplicantDocument(){}
    public ApplicantDocument(String agency,String applicant,String type,String filename,String provider,String key,String media,long size,String checksum,String classification,String replaces){super(agency);applicantId=requiredText(applicant,"applicantId");documentType=requiredText(type,"documentType");originalFilename=requiredText(filename,"filename");storageProvider=requiredText(provider,"provider");storageObjectKey=requiredText(key,"objectKey");mediaType=requiredText(media,"mediaType");if(size<1)throw new IllegalArgumentException("Document is empty");byteSize=size;this.checksum=requiredText(checksum,"checksum");this.classification=requiredText(classification,"classification");scanStatus=ScanStatus.PENDING_SCAN;uploadedAt=Instant.now();active=true;replacesDocumentId=optionalText(replaces);}
    public void replace(){active=false;} public String getApplicantId(){return applicantId;} public String getDocumentType(){return documentType;} public String getOriginalFilename(){return originalFilename;} public String getStorageProvider(){return storageProvider;} public String getStorageObjectKey(){return storageObjectKey;} public String getMediaType(){return mediaType;} public long getByteSize(){return byteSize;} public String getChecksum(){return checksum;} public String getClassification(){return classification;} public ScanStatus getScanStatus(){return scanStatus;} public Instant getUploadedAt(){return uploadedAt;} public boolean isActive(){return active;} public String getReplacesDocumentId(){return replacesDocumentId;}
}
