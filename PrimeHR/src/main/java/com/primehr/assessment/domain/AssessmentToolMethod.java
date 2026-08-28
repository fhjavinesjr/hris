package com.primehr.assessment.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "prime_assessment_tool_method", uniqueConstraints =
        @UniqueConstraint(name = "uk_prime_assessment_tool_method", columnNames = {"tool_id", "method_code"}))
public class AssessmentToolMethod {
    @Id @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;
    @Column(name = "agency_id", length = 64, nullable = false, updatable = false)
    private String agencyId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tool_id", nullable = false, updatable = false)
    private AssessmentTool tool;
    @Column(name = "method_code", length = 50, nullable = false, updatable = false)
    private String methodCode;
    @Column(name = "evidence_required", nullable = false)
    private boolean evidenceRequired;
    @Column(name = "active", nullable = false)
    private boolean active;
    @Version @Column(name = "record_version", nullable = false)
    private long version;
    @CreatedBy @Column(name = "created_by", length = 100, nullable = false, updatable = false)
    private String createdBy;
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @LastModifiedBy @Column(name = "updated_by", length = 100, nullable = false)
    private String updatedBy;
    @LastModifiedDate @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AssessmentToolMethod() { }

    public AssessmentToolMethod(String agencyId, AssessmentTool tool, AssessmentMethod method,
                                boolean evidenceRequired) {
        if (agencyId == null || agencyId.isBlank()) throw new IllegalArgumentException("agencyId is required");
        if (!agencyId.equals(tool.getAgencyId())) throw new IllegalArgumentException("Tool agency does not match");
        tool.requireDraft();
        this.agencyId = agencyId;
        this.tool = tool;
        this.methodCode = java.util.Objects.requireNonNull(method, "method").name();
        this.evidenceRequired = evidenceRequired;
        this.active = true;
    }

    public void updateDraft(boolean evidenceRequired) {
        tool.requireDraft();
        if (!active) throw new IllegalStateException("Archived tool methods cannot be changed");
        this.evidenceRequired = evidenceRequired;
    }

    public void archiveDraft() { tool.requireDraft(); active = false; }
    @PrePersist void assignId() { if (id == null) id = UUID.randomUUID().toString(); }
    public String getId() { return id; }
    public String getAgencyId() { return agencyId; }
    public AssessmentTool getTool() { return tool; }
    public AssessmentMethod getMethod() { return AssessmentMethod.valueOf(methodCode); }
    public boolean isEvidenceRequired() { return evidenceRequired; }
    public boolean isActive() { return active; }
    public long getVersion() { return version; }
}
