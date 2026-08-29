package com.primehr.gap.domain;

import com.primehr.shared.audit.AgencyAuditableEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "prime_gap_priority_scheme", uniqueConstraints =
        @UniqueConstraint(name = "uk_prime_gap_scheme_version",
                columnNames = {"agency_id", "code", "definition_version"}))
public class GapPriorityScheme extends AgencyAuditableEntity {
    @Column(name = "code", length = 50, nullable = false)
    private String code;
    @Nationalized @Column(name = "name", length = 200, nullable = false) private String name;
    @Nationalized @Column(name = "description", length = 2000) private String description;
    @Column(name = "status", length = 30, nullable = false) private String status;
    @Column(name = "definition_version", nullable = false) private int definitionVersion;
    @Column(name = "supersedes_id", length = 36) private String supersedesId;
    @Column(name = "published_by", length = 100) private String publishedBy;
    @Column(name = "published_at") private Instant publishedAt;

    protected GapPriorityScheme() { }

    private GapPriorityScheme(String agencyId, String code, String name, String description,
                              int displayOrder, LocalDate effectiveFrom, LocalDate effectiveTo,
                              int definitionVersion, String supersedesId) {
        super(agencyId, false, displayOrder, effectiveFrom, effectiveTo);
        this.code = requireText(code, "code").toUpperCase(Locale.ROOT);
        this.name = requireText(name, "name");
        this.description = normalize(description);
        this.status = GapPrioritySchemeStatus.DRAFT.name();
        this.definitionVersion = definitionVersion;
        this.supersedesId = supersedesId;
    }

    public static GapPriorityScheme draft(String agencyId, String code, String name, String description,
                                          int displayOrder, LocalDate effectiveFrom, LocalDate effectiveTo) {
        return new GapPriorityScheme(agencyId, code, name, description, displayOrder,
                effectiveFrom, effectiveTo, 1, null);
    }

    public GapPriorityScheme successorDraft() {
        requireStatus(GapPrioritySchemeStatus.ACTIVE);
        return new GapPriorityScheme(getAgencyId(), code, name, description, getDisplayOrder(),
                getEffectiveFrom(), getEffectiveTo(), definitionVersion + 1, getId());
    }

    public void updateDraft(String code, String name, String description, int displayOrder,
                            LocalDate effectiveFrom, LocalDate effectiveTo) {
        requireStatus(GapPrioritySchemeStatus.DRAFT);
        this.code = requireText(code, "code").toUpperCase(Locale.ROOT);
        this.name = requireText(name, "name");
        this.description = normalize(description);
        updateDefinitionFields(displayOrder, effectiveFrom, effectiveTo);
    }

    public void archiveDraft() {
        requireStatus(GapPrioritySchemeStatus.DRAFT);
        status = GapPrioritySchemeStatus.ARCHIVED.name();
        setDefinitionActive(false);
    }

    public void publish(String actor, Instant at) {
        requireStatus(GapPrioritySchemeStatus.DRAFT);
        if (getEffectiveFrom() == null) throw new IllegalArgumentException("effectiveFrom is required before publication");
        publishedBy = requireText(actor, "publisher");
        publishedAt = Objects.requireNonNull(at, "publishedAt");
        status = GapPrioritySchemeStatus.ACTIVE.name();
        setDefinitionActive(true);
    }

    public void closeEffectivePeriodBefore(LocalDate successorFrom) {
        requireStatus(GapPrioritySchemeStatus.ACTIVE);
        LocalDate end = Objects.requireNonNull(successorFrom, "successorFrom").minusDays(1);
        if (getEffectiveTo() == null || !getEffectiveTo().isBefore(successorFrom)) {
            updateDefinitionFields(getDisplayOrder(), getEffectiveFrom(), end);
        }
    }

    public boolean isDraft() { return getStatus() == GapPrioritySchemeStatus.DRAFT; }

    private void requireStatus(GapPrioritySchemeStatus expected) {
        if (getStatus() != expected) throw new IllegalLifecycleTransitionException(
                "Only " + expected + " gap priority schemes may be changed");
    }
    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public GapPrioritySchemeStatus getStatus() { return GapPrioritySchemeStatus.valueOf(status); }
    public int getDefinitionVersion() { return definitionVersion; }
    public String getSupersedesId() { return supersedesId; }
    public String getPublishedBy() { return publishedBy; }
    public Instant getPublishedAt() { return publishedAt; }
}
