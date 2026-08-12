package com.primehr.competency.domain;

import com.primehr.shared.audit.AgencyAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static jakarta.persistence.CascadeType.ALL;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;

@Entity
@Table(name = "prime_proficiency_scale", uniqueConstraints =
        @UniqueConstraint(name = "uk_prime_scale_agency_code_version",
                columnNames = {"agency_id", "code", "definition_version"}))
public class ProficiencyScale extends AgencyAuditableEntity {

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "definition_version", nullable = false)
    private int definitionVersion;

    @Column(name = "supersedes_id", length = 36)
    private String supersedesId;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "published_by", length = 100)
    private String publishedBy;

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "name", length = 150, nullable = false)
    @Nationalized
    private String name;

    @Column(name = "description", length = 1000)
    @Nationalized
    private String description;

    @OneToMany(mappedBy = "scale", cascade = ALL, orphanRemoval = true)
    @OrderBy("levelOrder ASC, code ASC")
    private List<ProficiencyLevel> levels = new ArrayList<>();

    protected ProficiencyScale() {
    }

    public ProficiencyScale(String agencyId, String code, String name, String description,
                            boolean active, int displayOrder, LocalDate effectiveFrom, LocalDate effectiveTo) {
        super(agencyId, active, displayOrder, effectiveFrom, effectiveTo);
        this.code = requireText(code, "code").toUpperCase(Locale.ROOT);
        this.name = requireText(name, "name");
        this.description = description == null ? null : description.trim();
        this.status = active ? DefinitionStatus.ACTIVE.name() : DefinitionStatus.DRAFT.name();
        this.definitionVersion = 1;
    }

    public static ProficiencyScale draft(String agencyId, String code, String name, String description,
                                          int displayOrder, LocalDate effectiveFrom, LocalDate effectiveTo) {
        return new ProficiencyScale(agencyId, code, name, description, false, displayOrder,
                effectiveFrom, effectiveTo);
    }

    public ProficiencyScale successorDraft() {
        requireStatus(DefinitionStatus.ACTIVE);
        ProficiencyScale successor = draft(getAgencyId(), code, name, description, getDisplayOrder(),
                getEffectiveFrom(), getEffectiveTo());
        successor.definitionVersion = definitionVersion + 1;
        successor.supersedesId = getId();
        for (ProficiencyLevel level : levels) {
            if (level.isActive()) {
                successor.addLevel(level.copyForDraft());
            }
        }
        return successor;
    }

    public void updateDraft(String code, String name, String description, int displayOrder,
                            LocalDate effectiveFrom, LocalDate effectiveTo) {
        requireStatus(DefinitionStatus.DRAFT);
        this.code = requireText(code, "code").toUpperCase(Locale.ROOT);
        this.name = requireText(name, "name");
        this.description = description == null ? null : description.trim();
        updateDefinitionFields(displayOrder, effectiveFrom, effectiveTo);
    }

    public void archiveDraft() {
        requireStatus(DefinitionStatus.DRAFT);
        status = DefinitionStatus.ARCHIVED.name();
        setDefinitionActive(false);
    }

    public void publish(String actor, Instant publishedAt) {
        requireStatus(DefinitionStatus.DRAFT);
        if (getEffectiveFrom() == null) {
            throw new IllegalArgumentException("effectiveFrom is required before publication");
        }
        this.publishedBy = requireText(actor, "publisher");
        this.publishedAt = java.util.Objects.requireNonNull(publishedAt, "publishedAt");
        this.status = DefinitionStatus.ACTIVE.name();
        setDefinitionActive(true);
    }

    public void closeEffectivePeriodBefore(LocalDate successorFrom) {
        requireStatus(DefinitionStatus.ACTIVE);
        LocalDate newEnd = java.util.Objects.requireNonNull(successorFrom, "successorFrom").minusDays(1);
        if (getEffectiveTo() == null || !getEffectiveTo().isBefore(successorFrom)) {
            updateDefinitionFields(getDisplayOrder(), getEffectiveFrom(), newEnd);
        }
    }

    public boolean isDraft() { return DefinitionStatus.DRAFT.name().equals(status); }

    private void requireStatus(DefinitionStatus expected) {
        if (!expected.name().equals(status)) {
            throw new IllegalLifecycleTransitionException(
                    "Only " + expected + " proficiency scales may be changed");
        }
    }

    public void addLevel(ProficiencyLevel level) {
        if (!getAgencyId().equals(level.getAgencyId())) {
            throw new IllegalArgumentException("A proficiency level must use the scale agency");
        }
        level.attachTo(this);
        levels.add(level);
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<ProficiencyLevel> getLevels() { return Collections.unmodifiableList(levels); }
    public DefinitionStatus getStatus() { return DefinitionStatus.valueOf(status); }
    public int getDefinitionVersion() { return definitionVersion; }
    public String getSupersedesId() { return supersedesId; }
    public Instant getPublishedAt() { return publishedAt; }
    public String getPublishedBy() { return publishedBy; }
}
