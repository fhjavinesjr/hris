package com.primehr.gap.domain;

import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "prime_gap_priority_level", uniqueConstraints = {
        @UniqueConstraint(name = "uk_prime_gap_level_code", columnNames = {"scheme_id", "code"}),
        @UniqueConstraint(name = "uk_prime_gap_level_rank", columnNames = {"scheme_id", "priority_rank"})
})
public class GapPriorityLevel {
    @Id @Column(length = 36, nullable = false, updatable = false) private String id;
    @Column(name = "agency_id", length = 64, nullable = false, updatable = false) private String agencyId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "scheme_id", nullable = false, updatable = false) private GapPriorityScheme scheme;
    @Column(length = 50, nullable = false) private String code;
    @Nationalized @Column(length = 150, nullable = false) private String label;
    @Nationalized @Column(length = 1000) private String description;
    @Column(name = "priority_rank", nullable = false) private int priorityRank;
    @Column(nullable = false) private boolean active;
    @Column(name = "display_order", nullable = false) private int displayOrder;
    @Version @Column(name = "record_version", nullable = false) private long version;
    @CreatedBy @Column(name = "created_by", length = 100, nullable = false, updatable = false) private String createdBy;
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @LastModifiedBy @Column(name = "updated_by", length = 100, nullable = false) private String updatedBy;
    @LastModifiedDate @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected GapPriorityLevel() { }

    public GapPriorityLevel(String agencyId, GapPriorityScheme scheme, String code, String label,
                            String description, int priorityRank, int displayOrder) {
        this.agencyId = requireText(agencyId, "agencyId");
        this.scheme = java.util.Objects.requireNonNull(scheme, "scheme");
        requireDraftScheme();
        if (!agencyId.equals(scheme.getAgencyId())) throw new IllegalArgumentException("Priority level agency does not match");
        apply(code, label, description, priorityRank, displayOrder);
        active = true;
    }

    public void updateDraft(String code, String label, String description, int priorityRank, int displayOrder) {
        requireDraftScheme();
        if (!active) throw new IllegalLifecycleTransitionException("An archived priority level cannot be changed");
        apply(code, label, description, priorityRank, displayOrder);
    }
    public void archiveDraft() { requireDraftScheme(); active = false; }
    public GapPriorityLevel copyTo(GapPriorityScheme successor) {
        if (!active) throw new IllegalLifecycleTransitionException("Archived priority levels cannot be copied");
        return new GapPriorityLevel(agencyId, successor, code, label, description, priorityRank, displayOrder);
    }

    private void apply(String code, String label, String description, int priorityRank, int displayOrder) {
        if (priorityRank < 1) throw new IllegalArgumentException("priorityRank must be at least 1");
        if (displayOrder < 0) throw new IllegalArgumentException("displayOrder cannot be negative");
        this.code = requireText(code, "code").toUpperCase(Locale.ROOT);
        this.label = requireText(label, "label");
        this.description = normalize(description);
        this.priorityRank = priorityRank;
        this.displayOrder = displayOrder;
    }
    private void requireDraftScheme() {
        if (scheme == null || !scheme.isDraft()) throw new IllegalLifecycleTransitionException(
                "Priority levels may be changed only on a DRAFT gap priority scheme");
    }
    @PrePersist void assignId() { if (id == null) id = UUID.randomUUID().toString(); }
    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public String getId() { return id; }
    public String getAgencyId() { return agencyId; }
    public GapPriorityScheme getScheme() { return scheme; }
    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
    public int getPriorityRank() { return priorityRank; }
    public boolean isActive() { return active; }
    public int getDisplayOrder() { return displayOrder; }
    public long getVersion() { return version; }
}
