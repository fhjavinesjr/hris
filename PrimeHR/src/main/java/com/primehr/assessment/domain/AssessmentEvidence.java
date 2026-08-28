package com.primehr.assessment.domain;

import jakarta.persistence.*;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.*;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "prime_assessment_evidence")
public class AssessmentEvidence {
    @Id @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;
    @Column(name = "agency_id", length = 64, nullable = false, updatable = false)
    private String agencyId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_rating_id", nullable = false, updatable = false)
    private AssessmentRating rating;
    @Column(name = "evidence_type", length = 100, nullable = false)
    private String evidenceType;
    @Nationalized @Column(name = "title_reference", length = 500, nullable = false)
    private String titleReference;
    @Column(name = "evidence_date", nullable = false)
    private LocalDate evidenceDate;
    @Nationalized @Column(name = "description", length = 4000)
    private String description;
    @Column(name = "source_system", length = 100)
    private String sourceSystem;
    @Nationalized @Column(name = "source_reference", length = 500)
    private String sourceReference;
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

    protected AssessmentEvidence() { }

    public AssessmentEvidence(String agencyId, AssessmentRating rating, String evidenceType,
                              String titleReference, LocalDate evidenceDate, String description,
                              String sourceSystem, String sourceReference, String actor) {
        this.agencyId = requireText(agencyId, "agencyId");
        this.rating = java.util.Objects.requireNonNull(rating, "rating");
        requireMutable(actor);
        apply(evidenceType, titleReference, evidenceDate, description, sourceSystem, sourceReference);
        this.active = true;
    }

    public void update(String evidenceType, String titleReference, LocalDate evidenceDate,
                       String description, String sourceSystem, String sourceReference,
                       long expectedVersion, String actor) {
        version(expectedVersion); requireMutable(actor);
        apply(evidenceType, titleReference, evidenceDate, description, sourceSystem, sourceReference);
    }

    public void archive(long expectedVersion, String actor) {
        version(expectedVersion); requireMutable(actor); active = false;
    }

    private void requireMutable(String actor) {
        rating.requireMutableBy(actor);
        if (!agencyId.equals(rating.getAgencyId())) throw new IllegalArgumentException("Evidence agency does not match");
        if (!active && id != null) throw new IllegalLifecycleTransitionException("Archived evidence cannot be changed");
    }
    private void apply(String type, String title, LocalDate date, String detail, String system, String reference) {
        evidenceType = requireText(type, "evidenceType");
        titleReference = requireText(title, "titleReference");
        evidenceDate = java.util.Objects.requireNonNull(date, "evidenceDate");
        description = normalize(detail); sourceSystem = normalize(system); sourceReference = normalize(reference);
    }
    private void version(long expected) { if (version != expected)
        throw new com.primehr.shared.exception.OptimisticConflictException(
                "Expected recordVersion " + expected + " but current version is " + version); }
    @PrePersist void assignId() { if (id == null) id = UUID.randomUUID().toString(); }
    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    public String getId() { return id; }
    public AssessmentRating getRating() { return rating; }
    public String getEvidenceType() { return evidenceType; }
    public String getTitleReference() { return titleReference; }
    public LocalDate getEvidenceDate() { return evidenceDate; }
    public String getDescription() { return description; }
    public String getSourceSystem() { return sourceSystem; }
    public String getSourceReference() { return sourceReference; }
    public boolean isActive() { return active; }
    public long getVersion() { return version; }
}
