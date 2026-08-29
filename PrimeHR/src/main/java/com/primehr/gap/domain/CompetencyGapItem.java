package com.primehr.gap.domain;

import com.primehr.assessment.domain.PersonCompetencyResult;
import com.primehr.competency.domain.Competency;
import com.primehr.competency.domain.ProficiencyLevel;
import com.primehr.positionprofile.domain.PositionProfileRequirement;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "prime_competency_gap_item", uniqueConstraints =
        @UniqueConstraint(name = "uk_prime_gap_item_competency", columnNames = {"analysis_id", "competency_id"}))
public class CompetencyGapItem {
    @Id @Column(length = 36, nullable = false, updatable = false) private String id;
    @Column(name = "agency_id", length = 64, nullable = false, updatable = false) private String agencyId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "analysis_id", nullable = false, updatable = false) private CompetencyGapAnalysis analysis;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "position_requirement_id", nullable = false, updatable = false) private PositionProfileRequirement positionRequirement;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "competency_id", nullable = false, updatable = false) private Competency competency;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "person_result_id", updatable = false) private PersonCompetencyResult personResult;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "required_proficiency_level_id", nullable = false, updatable = false) private ProficiencyLevel requiredLevel;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "attained_proficiency_level_id", updatable = false) private ProficiencyLevel attainedLevel;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "priority_level_id", updatable = false) private GapPriorityLevel priorityLevel;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "matched_rule_id", updatable = false) private GapPriorityRule matchedRule;
    @Column(name = "competency_code", length = 50, nullable = false, updatable = false) private String competencyCode;
    @Nationalized @Column(name = "competency_name", length = 200, nullable = false, updatable = false) private String competencyName;
    @Column(name = "competency_definition_version", nullable = false, updatable = false) private int competencyDefinitionVersion;
    @Column(name = "scale_id", length = 36, nullable = false, updatable = false) private String scaleId;
    @Column(name = "scale_definition_version", nullable = false, updatable = false) private int scaleDefinitionVersion;
    @Column(name = "required_level_code", length = 50, nullable = false, updatable = false) private String requiredLevelCode;
    @Nationalized @Column(name = "required_level_label", length = 150, nullable = false, updatable = false) private String requiredLevelLabel;
    @Column(name = "required_level_order", nullable = false, updatable = false) private int requiredLevelOrder;
    @Column(name = "attained_level_code", length = 50, updatable = false) private String attainedLevelCode;
    @Nationalized @Column(name = "attained_level_label", length = 150, updatable = false) private String attainedLevelLabel;
    @Column(name = "attained_level_order", updatable = false) private Integer attainedLevelOrder;
    @Column(name = "gap_value", updatable = false) private Integer gapValue;
    @Column(name = "gap_classification", length = 30, nullable = false, updatable = false) private String gapClassification;
    @Column(name = "not_assessed_reason", length = 40, updatable = false) private String notAssessedReason;
    @Column(name = "requirement_classification", length = 30, nullable = false, updatable = false) private String requirementClassification;
    @Column(name = "criticality_code", length = 50, updatable = false) private String criticalityCode;
    @Column(name = "priority_code", length = 50, updatable = false) private String priorityCode;
    @Nationalized @Column(name = "priority_label", length = 150, updatable = false) private String priorityLabel;
    @Column(name = "priority_rank", updatable = false) private Integer priorityRank;
    @Nationalized @Column(name = "priority_explanation", length = 1000, updatable = false) private String priorityExplanation;
    @Column(name = "display_order", nullable = false, updatable = false) private int displayOrder;
    @Version @Column(name = "record_version", nullable = false) private long version;
    @CreatedBy @Column(name = "created_by", length = 100, nullable = false, updatable = false) private String createdBy;
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @LastModifiedBy @Column(name = "updated_by", length = 100, nullable = false) private String updatedBy;
    @LastModifiedDate @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected CompetencyGapItem() { }

    public CompetencyGapItem(String agencyId, CompetencyGapAnalysis analysis,
                             PositionProfileRequirement requirement, PersonCompetencyResult result,
                             GapClassification classification, NotAssessedReason reason, Integer gap,
                             GapPriorityRule rule) {
        this.agencyId = requireText(agencyId, "agencyId");
        this.analysis = java.util.Objects.requireNonNull(analysis, "analysis");
        this.positionRequirement = java.util.Objects.requireNonNull(requirement, "requirement");
        this.competency = requirement.getCompetency();
        this.requiredLevel = requirement.getRequiredProficiencyLevel();
        this.personResult = result;
        this.attainedLevel = result == null ? null : result.getAttainedLevel();
        this.competencyCode = competency.getCode();
        this.competencyName = competency.getName();
        this.competencyDefinitionVersion = competency.getDefinitionVersion();
        this.scaleId = competency.getProficiencyScale().getId();
        this.scaleDefinitionVersion = competency.getProficiencyScale().getDefinitionVersion();
        this.requiredLevelCode = requiredLevel.getCode();
        this.requiredLevelLabel = requiredLevel.getLabel();
        this.requiredLevelOrder = requiredLevel.getLevelOrder();
        this.attainedLevelCode = attainedLevel == null ? null : attainedLevel.getCode();
        this.attainedLevelLabel = attainedLevel == null ? null : attainedLevel.getLabel();
        this.attainedLevelOrder = attainedLevel == null ? null : attainedLevel.getLevelOrder();
        this.gapValue = gap;
        this.gapClassification = java.util.Objects.requireNonNull(classification, "classification").name();
        this.notAssessedReason = reason == null ? null : reason.name();
        this.requirementClassification = requirement.getClassification().name();
        this.criticalityCode = normalize(requirement.getCriticalityCode());
        this.displayOrder = requirement.getDisplayOrder();
        if (classification == GapClassification.NOT_ASSESSED) {
            if (reason == null || gap != null || attainedLevel != null) throw new IllegalArgumentException("NOT_ASSESSED requires a reason and no numeric result");
        } else if (reason != null || gap == null || attainedLevel == null) {
            throw new IllegalArgumentException("Comparable gap results require an attained level and numeric gap");
        }
        if (classification == GapClassification.BELOW || classification == GapClassification.NOT_ASSESSED) {
            this.matchedRule = java.util.Objects.requireNonNull(rule, "priority rule");
            this.priorityLevel = rule.getPriorityLevel();
            this.priorityCode = priorityLevel.getCode();
            this.priorityLabel = priorityLevel.getLabel();
            this.priorityRank = priorityLevel.getPriorityRank();
            this.priorityExplanation = normalize(rule.getExplanation());
        } else if (rule != null) {
            throw new IllegalArgumentException("MEETS and EXCEEDS items cannot receive a development priority");
        }
    }

    @PrePersist void assignId() { if (id == null) id = UUID.randomUUID().toString(); }
    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public String getId() { return id; }
    public CompetencyGapAnalysis getAnalysis() { return analysis; }
    public PositionProfileRequirement getPositionRequirement() { return positionRequirement; }
    public Competency getCompetency() { return competency; }
    public PersonCompetencyResult getPersonResult() { return personResult; }
    public ProficiencyLevel getRequiredLevel() { return requiredLevel; }
    public ProficiencyLevel getAttainedLevel() { return attainedLevel; }
    public GapPriorityLevel getPriorityLevel() { return priorityLevel; }
    public GapPriorityRule getMatchedRule() { return matchedRule; }
    public String getCompetencyCode() { return competencyCode; }
    public String getCompetencyName() { return competencyName; }
    public int getCompetencyDefinitionVersion() { return competencyDefinitionVersion; }
    public String getScaleId() { return scaleId; }
    public int getScaleDefinitionVersion() { return scaleDefinitionVersion; }
    public String getRequiredLevelCode() { return requiredLevelCode; }
    public String getRequiredLevelLabel() { return requiredLevelLabel; }
    public int getRequiredLevelOrder() { return requiredLevelOrder; }
    public String getAttainedLevelCode() { return attainedLevelCode; }
    public String getAttainedLevelLabel() { return attainedLevelLabel; }
    public Integer getAttainedLevelOrder() { return attainedLevelOrder; }
    public Integer getGapValue() { return gapValue; }
    public GapClassification getGapClassification() { return GapClassification.valueOf(gapClassification); }
    public NotAssessedReason getNotAssessedReason() { return notAssessedReason == null ? null : NotAssessedReason.valueOf(notAssessedReason); }
    public String getRequirementClassification() { return requirementClassification; }
    public String getCriticalityCode() { return criticalityCode; }
    public String getPriorityCode() { return priorityCode; }
    public String getPriorityLabel() { return priorityLabel; }
    public Integer getPriorityRank() { return priorityRank; }
    public String getPriorityExplanation() { return priorityExplanation; }
    public int getDisplayOrder() { return displayOrder; }
}
