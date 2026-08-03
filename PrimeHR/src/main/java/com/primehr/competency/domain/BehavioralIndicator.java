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
import com.primehr.shared.exception.IllegalLifecycleTransitionException;

@Entity
@Table(name = "prime_behavioral_indicator", uniqueConstraints =
        @UniqueConstraint(name = "uk_prime_indicator_order",
                columnNames = {"competency_id", "proficiency_level_id", "display_order"}))
public class BehavioralIndicator extends AgencyAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competency_id", nullable = false)
    private Competency competency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proficiency_level_id", nullable = false)
    private ProficiencyLevel proficiencyLevel;

    @Column(name = "behavior_description", length = 2000, nullable = false)
    @Nationalized
    private String behaviorDescription;

    @Column(name = "evidence_guidance", length = 2000)
    @Nationalized
    private String evidenceGuidance;

    protected BehavioralIndicator() {
    }

    public BehavioralIndicator(String agencyId, Competency competency, ProficiencyLevel proficiencyLevel,
                               String behaviorDescription, String evidenceGuidance, boolean active,
                               int displayOrder, LocalDate effectiveFrom, LocalDate effectiveTo) {
        super(agencyId, active, displayOrder, effectiveFrom, effectiveTo);
        if (!agencyId.equals(competency.getAgencyId()) || !agencyId.equals(proficiencyLevel.getAgencyId())) {
            throw new IllegalArgumentException("Indicator and related records must use the same agency");
        }
        if (!sameScale(proficiencyLevel.getScale(), competency.getProficiencyScale())) {
            throw new IllegalArgumentException("Indicator level must belong to the competency scale");
        }
        this.competency = competency;
        this.proficiencyLevel = proficiencyLevel;
        this.behaviorDescription = requireText(behaviorDescription, "behaviorDescription");
        this.evidenceGuidance = evidenceGuidance == null ? null : evidenceGuidance.trim();
    }

    public Competency getCompetency() { return competency; }
    public ProficiencyLevel getProficiencyLevel() { return proficiencyLevel; }
    public String getBehaviorDescription() { return behaviorDescription; }
    public String getEvidenceGuidance() { return evidenceGuidance; }

    public void updateDraft(ProficiencyLevel proficiencyLevel, String behaviorDescription,
                            String evidenceGuidance, int displayOrder, LocalDate effectiveFrom,
                            LocalDate effectiveTo) {
        requireDraftCompetency();
        if (!getAgencyId().equals(proficiencyLevel.getAgencyId())
                || !sameScale(proficiencyLevel.getScale(), competency.getProficiencyScale())) {
            throw new IllegalArgumentException("Indicator level must belong to the competency scale and agency");
        }
        this.proficiencyLevel = proficiencyLevel;
        this.behaviorDescription = requireText(behaviorDescription, "behaviorDescription");
        this.evidenceGuidance = evidenceGuidance == null ? null : evidenceGuidance.trim();
        updateDefinitionFields(displayOrder, effectiveFrom, effectiveTo);
    }

    public void archiveDraft() {
        requireDraftCompetency();
        setDefinitionActive(false);
    }

    public BehavioralIndicator copyForDraft(Competency successor) {
        return new BehavioralIndicator(getAgencyId(), successor, proficiencyLevel, behaviorDescription,
                evidenceGuidance, isActive(), getDisplayOrder(), getEffectiveFrom(), getEffectiveTo());
    }

    private void requireDraftCompetency() {
        if (!competency.isDraft()) {
            throw new IllegalLifecycleTransitionException(
                    "Behavioral indicators may be changed only on a draft competency");
        }
    }

    private static boolean sameScale(ProficiencyScale first, ProficiencyScale second) {
        if (first == second) return true;
        return first != null && second != null && first.getId() != null && first.getId().equals(second.getId());
    }
}
