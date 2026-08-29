package com.primehr.gap.domain;

import com.primehr.positionprofile.domain.RequirementClassification;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;
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
@Table(name = "prime_gap_priority_rule", uniqueConstraints =
        @UniqueConstraint(name = "uk_prime_gap_rule_order", columnNames = {"scheme_id", "display_order"}))
public class GapPriorityRule {
    @Id @Column(length = 36, nullable = false, updatable = false) private String id;
    @Column(name = "agency_id", length = 64, nullable = false, updatable = false) private String agencyId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "scheme_id", nullable = false, updatable = false) private GapPriorityScheme scheme;
    @Column(name = "gap_classification", length = 30, nullable = false) private String gapClassification;
    @Column(name = "minimum_gap") private Integer minimumGap;
    @Column(name = "maximum_gap") private Integer maximumGap;
    @Column(name = "requirement_classification", length = 30) private String requirementClassification;
    @Column(name = "criticality_code", length = 50) private String criticalityCode;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "priority_level_id", nullable = false) private GapPriorityLevel priorityLevel;
    @Nationalized @Column(length = 1000) private String explanation;
    @Column(nullable = false) private boolean active;
    @Column(name = "display_order", nullable = false) private int displayOrder;
    @Version @Column(name = "record_version", nullable = false) private long version;
    @CreatedBy @Column(name = "created_by", length = 100, nullable = false, updatable = false) private String createdBy;
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @LastModifiedBy @Column(name = "updated_by", length = 100, nullable = false) private String updatedBy;
    @LastModifiedDate @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected GapPriorityRule() { }

    public GapPriorityRule(String agencyId, GapPriorityScheme scheme, GapClassification classification,
                           Integer minimumGap, Integer maximumGap,
                           RequirementClassification requirementClassification, String criticalityCode,
                           GapPriorityLevel priorityLevel, String explanation, int displayOrder) {
        this.agencyId = requireText(agencyId, "agencyId");
        this.scheme = java.util.Objects.requireNonNull(scheme, "scheme");
        if (!agencyId.equals(scheme.getAgencyId())) throw new IllegalArgumentException("Priority rule agency does not match");
        apply(classification, minimumGap, maximumGap, requirementClassification, criticalityCode,
                priorityLevel, explanation, displayOrder);
        active = true;
    }

    public void updateDraft(GapClassification classification, Integer minimumGap, Integer maximumGap,
                            RequirementClassification requirementClassification, String criticalityCode,
                            GapPriorityLevel priorityLevel, String explanation, int displayOrder) {
        requireDraftScheme();
        if (!active) throw new IllegalLifecycleTransitionException("An archived priority rule cannot be changed");
        apply(classification, minimumGap, maximumGap, requirementClassification, criticalityCode,
                priorityLevel, explanation, displayOrder);
    }
    public void archiveDraft() { requireDraftScheme(); active = false; }
    public GapPriorityRule copyTo(GapPriorityScheme successor, GapPriorityLevel copiedLevel) {
        if (!active) throw new IllegalLifecycleTransitionException("Archived priority rules cannot be copied");
        return new GapPriorityRule(agencyId, successor, getGapClassification(), minimumGap, maximumGap,
                getRequirementClassification(), criticalityCode, copiedLevel, explanation, displayOrder);
    }

    public boolean matches(GapClassification classification, Integer gap,
                           RequirementClassification requirement, String criticality) {
        if (!active || getGapClassification() != classification) return false;
        if (minimumGap != null && (gap == null || gap < minimumGap)) return false;
        if (maximumGap != null && (gap == null || gap > maximumGap)) return false;
        if (requirementClassification != null && getRequirementClassification() != requirement) return false;
        return criticalityCode == null || criticalityCode.equalsIgnoreCase(normalize(criticality));
    }
    public boolean isFallbackFor(GapClassification classification) {
        return active && getGapClassification() == classification && minimumGap == null && maximumGap == null
                && requirementClassification == null && criticalityCode == null;
    }

    private void apply(GapClassification classification, Integer minimumGap, Integer maximumGap,
                       RequirementClassification requirementClassification, String criticalityCode,
                       GapPriorityLevel priorityLevel, String explanation, int displayOrder) {
        requireDraftScheme();
        if (classification != GapClassification.BELOW && classification != GapClassification.NOT_ASSESSED) {
            throw new IllegalArgumentException("Priority rules apply only to BELOW or NOT_ASSESSED gaps");
        }
        if (minimumGap != null && minimumGap < 1 || maximumGap != null && maximumGap < 1
                || minimumGap != null && maximumGap != null && maximumGap < minimumGap) {
            throw new IllegalArgumentException("Gap range must contain positive values in ascending order");
        }
        if (classification == GapClassification.NOT_ASSESSED && (minimumGap != null || maximumGap != null)) {
            throw new IllegalArgumentException("NOT_ASSESSED rules cannot define a numeric gap range");
        }
        if (displayOrder < 0) throw new IllegalArgumentException("displayOrder cannot be negative");
        this.priorityLevel = java.util.Objects.requireNonNull(priorityLevel, "priorityLevel");
        if (!priorityLevel.isActive() || priorityLevel.getScheme() == null
                || scheme.getId() == null || !scheme.getId().equals(priorityLevel.getScheme().getId())) {
            throw new IllegalArgumentException("Priority level must be active and belong to this scheme");
        }
        this.gapClassification = classification.name();
        this.minimumGap = minimumGap;
        this.maximumGap = maximumGap;
        this.requirementClassification = requirementClassification == null ? null : requirementClassification.name();
        this.criticalityCode = normalize(criticalityCode);
        this.explanation = normalize(explanation);
        this.displayOrder = displayOrder;
    }
    private void requireDraftScheme() {
        if (scheme == null || !scheme.isDraft()) throw new IllegalLifecycleTransitionException(
                "Priority rules may be changed only on a DRAFT gap priority scheme");
    }
    @PrePersist void assignId() { if (id == null) id = UUID.randomUUID().toString(); }
    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public String getId() { return id; }
    public GapPriorityScheme getScheme() { return scheme; }
    public GapClassification getGapClassification() { return GapClassification.valueOf(gapClassification); }
    public Integer getMinimumGap() { return minimumGap; }
    public Integer getMaximumGap() { return maximumGap; }
    public RequirementClassification getRequirementClassification() {
        return requirementClassification == null ? null : RequirementClassification.valueOf(requirementClassification);
    }
    public String getCriticalityCode() { return criticalityCode; }
    public GapPriorityLevel getPriorityLevel() { return priorityLevel; }
    public String getExplanation() { return explanation; }
    public boolean isActive() { return active; }
    public int getDisplayOrder() { return displayOrder; }
    public long getVersion() { return version; }
}
