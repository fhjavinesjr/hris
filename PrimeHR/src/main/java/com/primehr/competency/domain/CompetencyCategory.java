package com.primehr.competency.domain;

import com.primehr.shared.audit.AgencyAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.util.Locale;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;

@Entity
@Table(name = "prime_competency_category", uniqueConstraints =
        @UniqueConstraint(name = "uk_prime_category_agency_code_version",
                columnNames = {"agency_id", "code", "definition_version"}))
public class CompetencyCategory extends AgencyAuditableEntity {

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "definition_version", nullable = false)
    private int definitionVersion;

    @Column(name = "supersedes_id", length = 36)
    private String supersedesId;

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "name", length = 150, nullable = false)
    @Nationalized
    private String name;

    @Column(name = "description", length = 1000)
    @Nationalized
    private String description;

    protected CompetencyCategory() {
    }

    public CompetencyCategory(String agencyId, String code, String name, String description,
                              boolean active, int displayOrder, LocalDate effectiveFrom, LocalDate effectiveTo) {
        super(agencyId, active, displayOrder, effectiveFrom, effectiveTo);
        this.code = requireText(code, "code").toUpperCase(Locale.ROOT);
        this.name = requireText(name, "name");
        this.description = description == null ? null : description.trim();
        this.status = active ? DefinitionStatus.ACTIVE.name() : DefinitionStatus.DRAFT.name();
        this.definitionVersion = 1;
    }

    public static CompetencyCategory draft(String agencyId, String code, String name, String description,
                                            int displayOrder, LocalDate effectiveFrom, LocalDate effectiveTo) {
        return new CompetencyCategory(agencyId, code, name, description, false, displayOrder,
                effectiveFrom, effectiveTo);
    }

    public CompetencyCategory successorDraft() {
        requireStatus(DefinitionStatus.ACTIVE);
        CompetencyCategory successor = draft(getAgencyId(), code, name, description, getDisplayOrder(),
                getEffectiveFrom(), getEffectiveTo());
        successor.definitionVersion = definitionVersion + 1;
        successor.supersedesId = getId();
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

    private void requireStatus(DefinitionStatus expected) {
        if (!expected.name().equals(status)) {
            throw new IllegalLifecycleTransitionException(
                    "Only " + expected + " competency categories may be changed");
        }
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public DefinitionStatus getStatus() { return DefinitionStatus.valueOf(status); }
    public int getDefinitionVersion() { return definitionVersion; }
    public String getSupersedesId() { return supersedesId; }
}
