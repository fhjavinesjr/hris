package com.primehr.positionprofile.domain;

import com.primehr.competency.domain.Competency;
import com.primehr.competency.domain.DefinitionStatus;
import com.primehr.competency.domain.ProficiencyLevel;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "prime_position_profile_requirement", uniqueConstraints =
        @UniqueConstraint(name = "uk_prime_profile_requirement_competency",
                columnNames = {"profile_id", "competency_id"}))
public class PositionProfileRequirement {
    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "agency_id", length = 64, nullable = false, updatable = false)
    private String agencyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false, updatable = false)
    private PositionProfile profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competency_id", nullable = false, updatable = false)
    private Competency competency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "required_proficiency_level_id", nullable = false)
    private ProficiencyLevel requiredProficiencyLevel;

    @Column(name = "classification", length = 30, nullable = false)
    private String classification;

    @Column(name = "criticality_code", length = 50)
    private String criticalityCode;

    @Column(name = "remarks", length = 2000)
    @Nationalized
    private String remarks;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Version
    @Column(name = "record_version", nullable = false)
    private long version;

    @CreatedBy
    @Column(name = "created_by", length = 100, nullable = false, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100, nullable = false)
    private String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PositionProfileRequirement() {
    }

    public PositionProfileRequirement(String agencyId, PositionProfile profile, Competency competency,
                                      ProficiencyLevel requiredProficiencyLevel,
                                      RequirementClassification classification, String criticalityCode,
                                      String remarks, int displayOrder) {
        this.agencyId = requireText(agencyId, "agencyId");
        this.profile = java.util.Objects.requireNonNull(profile, "profile");
        this.competency = java.util.Objects.requireNonNull(competency, "competency");
        requireCompatible(profile, competency, requiredProficiencyLevel);
        this.requiredProficiencyLevel = requiredProficiencyLevel;
        this.classification = java.util.Objects.requireNonNull(classification, "classification").name();
        this.criticalityCode = normalize(criticalityCode);
        this.remarks = normalize(remarks);
        setDisplayOrder(displayOrder);
        this.active = true;
    }

    public void updateDraft(ProficiencyLevel level, RequirementClassification classification,
                            String criticalityCode, String remarks, int displayOrder) {
        requireMutable();
        requireCompatible(profile, competency, level);
        requiredProficiencyLevel = level;
        this.classification = java.util.Objects.requireNonNull(classification, "classification").name();
        this.criticalityCode = normalize(criticalityCode);
        this.remarks = normalize(remarks);
        setDisplayOrder(displayOrder);
    }

    public void archiveDraft() {
        requireMutable();
        active = false;
    }

    public PositionProfileRequirement copyTo(PositionProfile successor) {
        if (!active) throw new IllegalLifecycleTransitionException("Archived requirements cannot be copied");
        return new PositionProfileRequirement(agencyId, successor, competency, requiredProficiencyLevel,
                getClassification(), criticalityCode, remarks, displayOrder);
    }

    @PrePersist
    void assignIdentifier() {
        if (id == null) id = UUID.randomUUID().toString();
    }

    private void requireMutable() {
        if (!profile.isDraft()) throw new IllegalLifecycleTransitionException(
                "Requirements may be changed only on a DRAFT position profile");
        if (!active) throw new IllegalLifecycleTransitionException("An archived requirement cannot be changed");
    }

    private static void requireCompatible(PositionProfile profile, Competency competency, ProficiencyLevel level) {
        if (!profile.isDraft()) throw new IllegalLifecycleTransitionException(
                "Requirements may be added only to a DRAFT position profile");
        if (!profile.getAgencyId().equals(competency.getAgencyId())
                || !profile.getAgencyId().equals(level.getAgencyId())) {
            throw new IllegalArgumentException("Profile, competency, and level must use the same agency");
        }
        if (competency.getDefinitionStatus() != DefinitionStatus.ACTIVE || !competency.isActive()
                || competency.getProficiencyScale().getStatus() != DefinitionStatus.ACTIVE
                || !competency.getProficiencyScale().isActive()) {
            throw new IllegalArgumentException("The exact competency version must be published");
        }
        if (!level.isActive() || !competency.getProficiencyScale().getId().equals(level.getScale().getId())) {
            throw new IllegalArgumentException(
                    "The required level must belong to the competency's exact published scale version");
        }
    }

    private void setDisplayOrder(int displayOrder) {
        if (displayOrder < 0) throw new IllegalArgumentException("displayOrder cannot be negative");
        this.displayOrder = displayOrder;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public String getId() { return id; }
    public String getAgencyId() { return agencyId; }
    public PositionProfile getProfile() { return profile; }
    public Competency getCompetency() { return competency; }
    public ProficiencyLevel getRequiredProficiencyLevel() { return requiredProficiencyLevel; }
    public RequirementClassification getClassification() { return RequirementClassification.valueOf(classification); }
    public String getCriticalityCode() { return criticalityCode; }
    public String getRemarks() { return remarks; }
    public boolean isActive() { return active; }
    public int getDisplayOrder() { return displayOrder; }
    public long getVersion() { return version; }
    public String getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public Instant getUpdatedAt() { return updatedAt; }
}
