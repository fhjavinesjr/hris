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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@Table(name = "prime_proficiency_scale", uniqueConstraints =
        @UniqueConstraint(name = "uk_prime_scale_agency_code", columnNames = {"agency_id", "code"}))
public class ProficiencyScale extends AgencyAuditableEntity {

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
}
