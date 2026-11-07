package com.primehr.rewardsrecognition.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Table(name = "rr_award_category_version", uniqueConstraints = @UniqueConstraint(
        name = "uk_rr_category_version", columnNames = {"agency_id", "category_id", "definition_version"}))
public class RrAwardCategoryVersion extends RrVersionedDefinition {
    public enum NomineeType { INDIVIDUAL, GROUP, ORGANIZATION_UNIT }
    public enum IncentiveType { MONETARY, NON_MONETARY, BOTH }

    @Column(name = "category_id", nullable = false, length = 36)
    private String categoryId;
    @Enumerated(EnumType.STRING)
    @Column(name = "nominee_type", nullable = false, length = 30)
    private NomineeType nomineeType;
    @Enumerated(EnumType.STRING)
    @Column(name = "incentive_type", nullable = false, length = 30)
    private IncentiveType incentiveType;
    @Column(name = "eligibility_guidance", nullable = false, length = 4000)
    private String eligibilityGuidance;
    @Column(name = "evidence_requirements", nullable = false, length = 4000)
    private String evidenceRequirements;
    @Column(name = "minimum_score", precision = 10, scale = 2)
    private BigDecimal minimumScore;
    @Column(name = "criteria_revision", nullable = false)
    private int criteriaRevision;

    protected RrAwardCategoryVersion() {
    }

    public RrAwardCategoryVersion(String agencyId, String categoryId, int definitionVersion,
                                  String supersedesId, String title, String description,
                                  NomineeType nomineeType, IncentiveType incentiveType,
                                  String eligibilityGuidance, String evidenceRequirements,
                                  BigDecimal minimumScore) {
        super(agencyId, definitionVersion, supersedesId, title, description);
        this.categoryId = requiredText(categoryId, "categoryId");
        apply(nomineeType, incentiveType, eligibilityGuidance, evidenceRequirements, minimumScore);
    }

    public void update(String title, String description, NomineeType nomineeType,
                       IncentiveType incentiveType, String eligibilityGuidance,
                       String evidenceRequirements, BigDecimal minimumScore) {
        changeDefinition(title, description);
        apply(nomineeType, incentiveType, eligibilityGuidance, evidenceRequirements, minimumScore);
    }

    public void criteriaChanged() {
        requireDraft();
        criteriaRevision++;
    }

    private void apply(NomineeType nomineeType, IncentiveType incentiveType,
                       String eligibilityGuidance, String evidenceRequirements,
                       BigDecimal minimumScore) {
        if (nomineeType == null) throw new IllegalArgumentException("nomineeType is required");
        if (incentiveType == null) throw new IllegalArgumentException("incentiveType is required");
        if (minimumScore != null && minimumScore.signum() < 0) {
            throw new IllegalArgumentException("minimumScore cannot be negative");
        }
        this.nomineeType = nomineeType;
        this.incentiveType = incentiveType;
        this.eligibilityGuidance = requiredText(eligibilityGuidance, "eligibilityGuidance");
        this.evidenceRequirements = requiredText(evidenceRequirements, "evidenceRequirements");
        this.minimumScore = minimumScore;
    }

    public String getCategoryId() { return categoryId; }
    public NomineeType getNomineeType() { return nomineeType; }
    public IncentiveType getIncentiveType() { return incentiveType; }
    public String getEligibilityGuidance() { return eligibilityGuidance; }
    public String getEvidenceRequirements() { return evidenceRequirements; }
    public BigDecimal getMinimumScore() { return minimumScore; }
    public int getCriteriaRevision() { return criteriaRevision; }
}
