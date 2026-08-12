package com.primehr.competency.domain;

import com.primehr.shared.audit.AgencyAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.time.Instant;
import java.util.Locale;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;

@Entity
@Table(name = "prime_competency", uniqueConstraints =
        @UniqueConstraint(name = "uk_prime_competency_agency_code_version",
                columnNames = {"agency_id", "code", "definition_version"}))
public class Competency extends AgencyAuditableEntity {

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    @Nationalized
    private String name;

    @Column(name = "definition", length = 4000, nullable = false)
    @Nationalized
    private String definition;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CompetencyCategory category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proficiency_scale_id", nullable = false)
    private ProficiencyScale proficiencyScale;

    protected Competency() {
    }

    public Competency(String agencyId, String code, String name, String definition, String status,
                      CompetencyCategory category, ProficiencyScale proficiencyScale,
                      boolean active, int displayOrder, LocalDate effectiveFrom, LocalDate effectiveTo) {
        super(agencyId, active, displayOrder, effectiveFrom, effectiveTo);
        if (!agencyId.equals(category.getAgencyId()) || !agencyId.equals(proficiencyScale.getAgencyId())) {
            throw new IllegalArgumentException("Competency, category, and scale must use the same agency");
        }
        this.code = requireText(code, "code").toUpperCase(Locale.ROOT);
        this.name = requireText(name, "name");
        this.definition = requireText(definition, "definition");
        this.status = requireText(status, "status").toUpperCase(Locale.ROOT);
        DefinitionStatus.valueOf(this.status);
        this.definitionVersion = 1;
        this.category = category;
        this.proficiencyScale = proficiencyScale;
    }

    public static Competency draft(String agencyId, String code, String name, String definition,
                                   CompetencyCategory category, ProficiencyScale scale, int displayOrder,
                                   LocalDate effectiveFrom, LocalDate effectiveTo) {
        return new Competency(agencyId, code, name, definition, DefinitionStatus.DRAFT.name(), category, scale,
                false, displayOrder, effectiveFrom, effectiveTo);
    }

    public Competency successorDraft() {
        requireStatus(DefinitionStatus.ACTIVE);
        Competency successor = draft(getAgencyId(), code, name, definition, category, proficiencyScale,
                getDisplayOrder(), getEffectiveFrom(), getEffectiveTo());
        successor.definitionVersion = definitionVersion + 1;
        successor.supersedesId = getId();
        return successor;
    }

    public void updateDraft(String code, String name, String definition, CompetencyCategory category,
                            ProficiencyScale scale, int displayOrder, LocalDate effectiveFrom,
                            LocalDate effectiveTo) {
        requireStatus(DefinitionStatus.DRAFT);
        if (!getAgencyId().equals(category.getAgencyId()) || !getAgencyId().equals(scale.getAgencyId())) {
            throw new IllegalArgumentException("Competency, category, and scale must use the same agency");
        }
        this.code = requireText(code, "code").toUpperCase(Locale.ROOT);
        this.name = requireText(name, "name");
        this.definition = requireText(definition, "definition");
        this.category = category;
        this.proficiencyScale = scale;
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
            throw new IllegalLifecycleTransitionException("Only " + expected + " competencies may be changed");
        }
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDefinition() { return definition; }
    public String getStatus() { return status; }
    public DefinitionStatus getDefinitionStatus() { return DefinitionStatus.valueOf(status); }
    public int getDefinitionVersion() { return definitionVersion; }
    public String getSupersedesId() { return supersedesId; }
    public Instant getPublishedAt() { return publishedAt; }
    public String getPublishedBy() { return publishedBy; }
    public CompetencyCategory getCategory() { return category; }
    public ProficiencyScale getProficiencyScale() { return proficiencyScale; }
}
