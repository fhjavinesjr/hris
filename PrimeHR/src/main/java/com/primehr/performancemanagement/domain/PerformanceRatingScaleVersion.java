package com.primehr.performancemanagement.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

@Entity
@Table(name = "spms_rating_scale_version", uniqueConstraints = @UniqueConstraint(
        name = "uk_spms_rating_scale_version", columnNames = {"agency_id", "rating_scale_id", "definition_version"}))
public class PerformanceRatingScaleVersion extends RspAuditedEntity {
    public enum Status { DRAFT, PUBLISHED, RETIRED }
    public enum MeasureType { COUNT, NUMBER, PERCENTAGE, CURRENCY, DURATION, DATE_MILESTONE, BOOLEAN, MANUAL_RUBRIC }
    public enum Direction { HIGHER_IS_BETTER, LOWER_IS_BETTER, EXACT_TARGET, WITHIN_RANGE, MANUAL_RUBRIC }
    public enum Aggregation { WEIGHTED_AVERAGE }
    public enum MissingValuePolicy { ERROR }
    public enum DimensionScoreSource { THRESHOLD_BAND, MANUAL_RUBRIC }

    @Column(name = "rating_scale_id", nullable = false, length = 36) private String ratingScaleId;
    @Column(name = "policy_version_id", nullable = false, length = 36) private String policyVersionId;
    @Column(name = "definition_version", nullable = false) private int definitionVersion;
    @Column(name = "supersedes_id", length = 36) private String supersedesId;
    @Column(nullable = false, length = 200) private String title;
    @Column(length = 2000) private String description;
    @Column(name = "legal_basis", nullable = false, length = 1000) private String legalBasis;
    @Column(name = "minimum_score", nullable = false, precision = 19, scale = 6) private BigDecimal minimumScore;
    @Column(name = "maximum_score", nullable = false, precision = 19, scale = 6) private BigDecimal maximumScore;
    @Column(name = "rounding_scale", nullable = false) private int roundingScale;
    @Enumerated(EnumType.STRING) @Column(name = "rounding_mode", nullable = false, length = 20) private RoundingMode roundingMode;
    @Enumerated(EnumType.STRING) @Column(name = "measure_type", nullable = false, length = 30) private MeasureType measureType;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private Direction direction;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private Aggregation aggregation;
    @Enumerated(EnumType.STRING) @Column(name = "missing_value_policy", nullable = false, length = 20) private MissingValuePolicy missingValuePolicy;
    @Enumerated(EnumType.STRING) @Column(name = "dimension_score_source", nullable = false, length = 30) private DimensionScoreSource dimensionScoreSource;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Status status;
    @Column(name = "band_revision", nullable = false) private int bandRevision;
    @Column(name = "effective_from") private LocalDate effectiveFrom;
    @Column(name = "effective_to") private LocalDate effectiveTo;
    @Column(name = "published_by", length = 100) private String publishedBy;
    @Column(name = "published_at") private Instant publishedAt;
    @Column(name = "retired_by", length = 100) private String retiredBy;
    @Column(name = "retired_at") private Instant retiredAt;
    @Column(name = "retirement_reason", length = 1000) private String retirementReason;

    protected PerformanceRatingScaleVersion() {}

    public PerformanceRatingScaleVersion(String agencyId, String ratingScaleId, String policyVersionId,
            int definitionVersion, String supersedesId, String title, String description, String legalBasis,
            BigDecimal minimumScore, BigDecimal maximumScore, int roundingScale, RoundingMode roundingMode,
            MeasureType measureType, Direction direction, DimensionScoreSource dimensionScoreSource) {
        super(agencyId);
        if (definitionVersion < 1) throw new IllegalArgumentException("definitionVersion must be positive");
        this.ratingScaleId = requiredText(ratingScaleId, "ratingScaleId");
        this.definitionVersion = definitionVersion;
        this.supersedesId = optionalText(supersedesId);
        this.status = Status.DRAFT;
        this.bandRevision = 0;
        this.aggregation = Aggregation.WEIGHTED_AVERAGE;
        this.missingValuePolicy = MissingValuePolicy.ERROR;
        apply(policyVersionId, title, description, legalBasis, minimumScore, maximumScore, roundingScale,
                roundingMode, measureType, direction, dimensionScoreSource);
    }

    public void update(String policyVersionId, String title, String description, String legalBasis,
            BigDecimal minimumScore, BigDecimal maximumScore, int roundingScale, RoundingMode roundingMode,
            MeasureType measureType, Direction direction, DimensionScoreSource dimensionScoreSource) {
        requireDraft();
        apply(policyVersionId, title, description, legalBasis, minimumScore, maximumScore, roundingScale,
                roundingMode, measureType, direction, dimensionScoreSource);
    }

    private void apply(String policyVersionId, String title, String description, String legalBasis,
            BigDecimal minimumScore, BigDecimal maximumScore, int roundingScale, RoundingMode roundingMode,
            MeasureType measureType, Direction direction, DimensionScoreSource dimensionScoreSource) {
        this.policyVersionId = requiredText(policyVersionId, "policyVersionId");
        this.title = requiredText(title, "title");
        this.description = optionalText(description);
        this.legalBasis = requiredText(legalBasis, "legalBasis");
        this.minimumScore = Objects.requireNonNull(minimumScore, "minimumScore");
        this.maximumScore = Objects.requireNonNull(maximumScore, "maximumScore");
        if (maximumScore.compareTo(minimumScore) < 0) throw new IllegalArgumentException("maximumScore cannot be less than minimumScore");
        if (roundingScale < 0 || roundingScale > 6) throw new IllegalArgumentException("roundingScale must be from 0 to 6");
        this.roundingScale = roundingScale;
        this.roundingMode = Objects.requireNonNull(roundingMode, "roundingMode");
        if (roundingMode != RoundingMode.HALF_UP && roundingMode != RoundingMode.HALF_EVEN && roundingMode != RoundingMode.DOWN)
            throw new IllegalArgumentException("roundingMode must be HALF_UP, HALF_EVEN, or DOWN");
        this.measureType = Objects.requireNonNull(measureType, "measureType");
        this.direction = Objects.requireNonNull(direction, "direction");
        this.dimensionScoreSource = Objects.requireNonNull(dimensionScoreSource, "dimensionScoreSource");
        if ((measureType == MeasureType.MANUAL_RUBRIC) != (direction == Direction.MANUAL_RUBRIC)
                || (direction == Direction.MANUAL_RUBRIC) != (dimensionScoreSource == DimensionScoreSource.MANUAL_RUBRIC))
            throw new IllegalArgumentException("Manual rubric measure, direction, and score source must be selected together");
    }

    public void publish(LocalDate from, LocalDate to, String actor, Instant at) {
        requireDraft();
        if (from == null) throw new IllegalArgumentException("effectiveFrom is required");
        if (to != null && to.isBefore(from)) throw new IllegalArgumentException("effectiveTo cannot precede effectiveFrom");
        effectiveFrom = from; effectiveTo = to; publishedBy = requiredText(actor, "actor");
        publishedAt = Objects.requireNonNull(at); status = Status.PUBLISHED;
    }

    public void bandsReplaced() { requireDraft(); bandRevision++; }

    public void supersede(LocalDate successorFrom, String actor, Instant at) {
        if (status != Status.PUBLISHED) throw new IllegalLifecycleTransitionException("Only a published rating scale may be superseded");
        if (successorFrom == null || !successorFrom.isAfter(effectiveFrom)) throw new IllegalArgumentException("A successor must become effective after its predecessor");
        effectiveTo = successorFrom.minusDays(1); retiredBy = requiredText(actor, "actor");
        retiredAt = Objects.requireNonNull(at); retirementReason = "Superseded by a published revision"; status = Status.RETIRED;
    }

    public void retire(String actor, String reason, Instant at) {
        if (status != Status.PUBLISHED) throw new IllegalLifecycleTransitionException("Only a published rating scale may be retired");
        retiredBy = requiredText(actor, "actor"); retirementReason = requiredText(reason, "reason");
        retiredAt = Objects.requireNonNull(at);
        LocalDate day = at.atZone(ZoneId.of("Asia/Manila")).toLocalDate();
        LocalDate end = day.isBefore(effectiveFrom) ? effectiveFrom : day;
        if (effectiveTo == null || effectiveTo.isAfter(end)) effectiveTo = end;
        status = Status.RETIRED;
    }

    private void requireDraft() {
        if (status != Status.DRAFT) throw new IllegalLifecycleTransitionException("Published rating scale versions are immutable; create a revision");
    }

    public String getRatingScaleId(){return ratingScaleId;} public String getPolicyVersionId(){return policyVersionId;}
    public int getDefinitionVersion(){return definitionVersion;} public String getSupersedesId(){return supersedesId;}
    public String getTitle(){return title;} public String getDescription(){return description;} public String getLegalBasis(){return legalBasis;}
    public BigDecimal getMinimumScore(){return minimumScore;} public BigDecimal getMaximumScore(){return maximumScore;}
    public int getRoundingScale(){return roundingScale;} public RoundingMode getRoundingMode(){return roundingMode;}
    public MeasureType getMeasureType(){return measureType;} public Direction getDirection(){return direction;}
    public Aggregation getAggregation(){return aggregation;} public MissingValuePolicy getMissingValuePolicy(){return missingValuePolicy;}
    public DimensionScoreSource getDimensionScoreSource(){return dimensionScoreSource;} public Status getStatus(){return status;}
    public LocalDate getEffectiveFrom(){return effectiveFrom;} public LocalDate getEffectiveTo(){return effectiveTo;}
    public String getPublishedBy(){return publishedBy;} public Instant getPublishedAt(){return publishedAt;}
    public String getRetiredBy(){return retiredBy;} public Instant getRetiredAt(){return retiredAt;} public String getRetirementReason(){return retirementReason;}
    public int getBandRevision(){return bandRevision;}
}
