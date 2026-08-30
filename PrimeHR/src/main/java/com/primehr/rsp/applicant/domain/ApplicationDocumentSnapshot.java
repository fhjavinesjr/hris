package com.primehr.rsp.applicant.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "rsp_application_document_snapshot", uniqueConstraints =
        @UniqueConstraint(name = "uk_rsp_application_document",
                columnNames = {"agency_id", "application_id", "applicant_document_id"}))
public class ApplicationDocumentSnapshot extends RspAuditedEntity {
    @Column(name = "application_id", nullable = false, length = 36) private String applicationId;
    @Column(name = "applicant_document_id", nullable = false, length = 36) private String applicantDocumentId;
    @Column(name = "document_type", nullable = false, length = 100) private String documentType;
    @Column(name = "original_filename", nullable = false, length = 255) private String originalFilename;
    @Column(name = "storage_provider", nullable = false, length = 20) private String storageProvider;
    @Column(name = "storage_object_key", nullable = false, length = 500) private String storageObjectKey;
    @Column(name = "media_type", nullable = false, length = 150) private String mediaType;
    @Column(name = "byte_size", nullable = false) private long byteSize;
    @Column(name = "sha256_checksum", nullable = false, length = 64) private String checksum;
    @Column(nullable = false, length = 30) private String classification;
    @Column(name = "display_order", nullable = false) private int displayOrder;

    protected ApplicationDocumentSnapshot() {}

    public ApplicationDocumentSnapshot(String agencyId, String applicationId, ApplicantDocument source,
                                       int displayOrder) {
        super(agencyId);
        this.applicationId = requiredText(applicationId, "applicationId");
        applicantDocumentId = source.getId();
        documentType = source.getDocumentType();
        originalFilename = source.getOriginalFilename();
        storageProvider = source.getStorageProvider();
        storageObjectKey = source.getStorageObjectKey();
        mediaType = source.getMediaType();
        byteSize = source.getByteSize();
        checksum = source.getChecksum();
        classification = source.getClassification();
        this.displayOrder = displayOrder;
    }

    public String getApplicationId() { return applicationId; }
    public String getApplicantDocumentId() { return applicantDocumentId; }
    public String getDocumentType() { return documentType; }
    public String getOriginalFilename() { return originalFilename; }
    public String getStorageProvider() { return storageProvider; }
    public String getStorageObjectKey() { return storageObjectKey; }
    public String getMediaType() { return mediaType; }
    public long getByteSize() { return byteSize; }
    public String getChecksum() { return checksum; }
    public String getClassification() { return classification; }
    public int getDisplayOrder() { return displayOrder; }
}
