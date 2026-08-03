package com.primehr.competency.domain;

import com.primehr.shared.audit.AgencyAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.util.Locale;

@Entity
@Table(name = "prime_competency_category", uniqueConstraints =
        @UniqueConstraint(name = "uk_prime_category_agency_code", columnNames = {"agency_id", "code"}))
public class CompetencyCategory extends AgencyAuditableEntity {

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
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
