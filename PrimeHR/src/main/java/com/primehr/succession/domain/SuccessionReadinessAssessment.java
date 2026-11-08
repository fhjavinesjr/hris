package com.primehr.succession.domain;

import com.primehr.gap.application.TargetCompetencyGapService.TargetGapResult;
import com.primehr.rsp.domain.RspAuditedEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "succession_readiness_assessment", uniqueConstraints =
        @UniqueConstraint(name = "uk_succ_readiness_candidate", columnNames = {"agency_id", "candidate_id"}))
public class SuccessionReadinessAssessment extends RspAuditedEntity {
    public enum Status { PANEL_REVIEW, RECOMMENDED, FINALIZED }

    @Column(name="candidate_id",nullable=false,length=36) private String candidateId;
    @Column(name="cycle_id",nullable=false,length=36) private String cycleId;
    @Column(name="charter_version_id",nullable=false,length=36) private String charterVersionId;
    @Column(name="readiness_scale_version_id",nullable=false,length=36) private String readinessScaleVersionId;
    @Column(name="readiness_scale_definition_version",nullable=false) private int readinessScaleDefinitionVersion;
    @Column(name="position_profile_id",nullable=false,length=36) private String positionProfileId;
    @Column(name="position_profile_definition_version",nullable=false) private int positionProfileDefinitionVersion;
    @Column(name="position_profile_content_revision",nullable=false) private long positionProfileContentRevision;
    @Column(name="position_profile_fingerprint",nullable=false,length=120) private String positionProfileFingerprint;
    @Column(name="person_profile_id",nullable=false,length=36) private String personProfileId;
    @Column(name="person_profile_version",nullable=false) private int personProfileVersion;
    @Column(name="gap_calculated_at",nullable=false) private Instant gapCalculatedAt;
    @Column(name="below_count",nullable=false) private int belowCount;
    @Column(name="meets_count",nullable=false) private int meetsCount;
    @Column(name="exceeds_count",nullable=false) private int exceedsCount;
    @Column(name="not_assessed_count",nullable=false) private int notAssessedCount;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Column(name="recommended_level_id",length=36) private String recommendedLevelId;
    @Column(name="recommended_level_code",length=80) private String recommendedLevelCode;
    @Column(name="recommended_level_label",length=200) private String recommendedLevelLabel;
    @Column(name="pool_recommended") private Boolean poolRecommended;
    @Column(name="recommendation_narrative",length=4000) private String recommendationNarrative;
    @Column(name="recommended_by",length=100) private String recommendedBy;
    @Column(name="recommended_at") private Instant recommendedAt;
    @Column(name="finalized_by",length=100) private String finalizedBy;
    @Column(name="finalized_at") private Instant finalizedAt;

    protected SuccessionReadinessAssessment() { }

    public SuccessionReadinessAssessment(String agencyId, SuccessionCandidate candidate,
                                         String charterVersionId, SuccessionReadinessScaleVersion scale,
                                         TargetGapResult gap) {
        super(agencyId);
        candidateId = requiredText(candidate.getId(), "candidateId");
        cycleId = requiredText(candidate.getCycleId(), "cycleId");
        this.charterVersionId = requiredText(charterVersionId, "charterVersionId");
        readinessScaleVersionId = requiredText(scale.getId(), "readinessScaleVersionId");
        readinessScaleDefinitionVersion = scale.getDefinitionVersion();
        positionProfileId = requiredText(gap.positionProfileId(), "positionProfileId");
        positionProfileDefinitionVersion = gap.positionProfileVersion();
        positionProfileContentRevision = gap.positionProfileRevision();
        positionProfileFingerprint = requiredText(gap.positionProfileFingerprint(), "positionProfileFingerprint");
        personProfileId = requiredText(gap.personProfileId(), "personProfileId");
        personProfileVersion = gap.personProfileVersion();
        gapCalculatedAt = gap.calculatedAt();
        belowCount = (int) gap.items().stream().filter(i -> i.classification().name().equals("BELOW")).count();
        meetsCount = (int) gap.items().stream().filter(i -> i.classification().name().equals("MEETS")).count();
        exceedsCount = (int) gap.items().stream().filter(i -> i.classification().name().equals("EXCEEDS")).count();
        notAssessedCount = (int) gap.items().stream().filter(i -> i.classification().name().equals("NOT_ASSESSED")).count();
        status = Status.PANEL_REVIEW;
    }

    public void recommend(SuccessionReadinessLevel level, boolean include, String narrative,
                          String actor, Instant at) {
        if (status != Status.PANEL_REVIEW) throw new IllegalLifecycleTransitionException(
                "Only an assessment in panel review may receive a recommendation");
        if (!readinessScaleVersionId.equals(level.getScaleVersionId())) throw new IllegalArgumentException(
                "Readiness level does not belong to the frozen scale version");
        if (include && !level.isAllowsPoolMembership()) throw new IllegalArgumentException(
                "The selected readiness level does not allow pool membership");
        recommendedLevelId = level.getId(); recommendedLevelCode = level.getCode();
        recommendedLevelLabel = level.getLabel(); poolRecommended = include;
        recommendationNarrative = requiredText(narrative, "recommendationNarrative");
        recommendedBy = requiredText(actor, "actor"); recommendedAt = at; status = Status.RECOMMENDED;
    }

    public void finalizeDecision(String actor, Instant at) {
        if (status != Status.RECOMMENDED) throw new IllegalLifecycleTransitionException(
                "A panel recommendation is required before the final decision");
        finalizedBy = requiredText(actor, "actor"); finalizedAt = at; status = Status.FINALIZED;
    }

    public String getCandidateId(){return candidateId;} public String getCycleId(){return cycleId;}
    public String getCharterVersionId(){return charterVersionId;} public String getReadinessScaleVersionId(){return readinessScaleVersionId;}
    public int getReadinessScaleDefinitionVersion(){return readinessScaleDefinitionVersion;} public String getPositionProfileId(){return positionProfileId;}
    public int getPositionProfileDefinitionVersion(){return positionProfileDefinitionVersion;} public long getPositionProfileContentRevision(){return positionProfileContentRevision;}
    public String getPositionProfileFingerprint(){return positionProfileFingerprint;} public String getPersonProfileId(){return personProfileId;}
    public int getPersonProfileVersion(){return personProfileVersion;} public Instant getGapCalculatedAt(){return gapCalculatedAt;}
    public int getBelowCount(){return belowCount;} public int getMeetsCount(){return meetsCount;} public int getExceedsCount(){return exceedsCount;}
    public int getNotAssessedCount(){return notAssessedCount;} public Status getStatus(){return status;}
    public String getRecommendedLevelId(){return recommendedLevelId;} public String getRecommendedLevelCode(){return recommendedLevelCode;}
    public String getRecommendedLevelLabel(){return recommendedLevelLabel;} public Boolean getPoolRecommended(){return poolRecommended;}
    public String getRecommendationNarrative(){return recommendationNarrative;} public String getRecommendedBy(){return recommendedBy;}
    public Instant getRecommendedAt(){return recommendedAt;} public String getFinalizedBy(){return finalizedBy;} public Instant getFinalizedAt(){return finalizedAt;}
}
