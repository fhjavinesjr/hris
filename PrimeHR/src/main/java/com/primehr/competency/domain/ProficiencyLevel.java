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
@Table(name = "prime_proficiency_level", uniqueConstraints = {
        @UniqueConstraint(name = "uk_prime_level_scale_code", columnNames = {"scale_id", "code"}),
        @UniqueConstraint(name = "uk_prime_level_scale_order", columnNames = {"scale_id", "level_order"})
})
public class ProficiencyLevel extends AgencyAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scale_id", nullable = false)
    private ProficiencyScale scale;

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "label", length = 150, nullable = false)
    @Nationalized
    private String label;

    @Column(name = "level_order", nullable = false)
    private int levelOrder;

    @Column(name = "description", length = 1000)
    @Nationalized
    private String description;

    protected ProficiencyLevel() {
    }

    public ProficiencyLevel(String agencyId, String code, String label, int levelOrder,
                            String description, boolean active, LocalDate effectiveFrom, LocalDate effectiveTo) {
        super(agencyId, active, levelOrder, effectiveFrom, effectiveTo);
        if (levelOrder < 1) {
            throw new IllegalArgumentException("levelOrder must be at least 1");
        }
        this.code = requireText(code, "code").toUpperCase(Locale.ROOT);
        this.label = requireText(label, "label");
        this.levelOrder = levelOrder;
        this.description = description == null ? null : description.trim();
    }

    void attachTo(ProficiencyScale scale) {
        this.scale = scale;
    }

    public ProficiencyScale getScale() { return scale; }
    public String getCode() { return code; }
    public String getLabel() { return label; }
    public int getLevelOrder() { return levelOrder; }
    public String getDescription() { return description; }
}
