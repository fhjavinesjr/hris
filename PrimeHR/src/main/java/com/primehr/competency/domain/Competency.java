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
import java.util.Locale;

@Entity
@Table(name = "prime_competency", uniqueConstraints =
        @UniqueConstraint(name = "uk_prime_competency_agency_code", columnNames = {"agency_id", "code"}))
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
        this.category = category;
        this.proficiencyScale = proficiencyScale;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDefinition() { return definition; }
    public String getStatus() { return status; }
    public CompetencyCategory getCategory() { return category; }
    public ProficiencyScale getProficiencyScale() { return proficiencyScale; }
}
