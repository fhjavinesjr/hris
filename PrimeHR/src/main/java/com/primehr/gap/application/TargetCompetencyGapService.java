package com.primehr.gap.application;

import com.primehr.gap.domain.GapClassification;
import com.primehr.gap.domain.NotAssessedReason;
import com.primehr.positionprofile.domain.PositionTargetType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Read-only application boundary for comparing a person profile with an exact target position. */
public interface TargetCompetencyGapService {
    TargetGapResult calculate(String agencyId, PositionTargetType targetType, Long jobPositionId,
                              Long plantillaId, String employeeNo, LocalDate asOf);

    record TargetGapResult(String positionProfileId, int positionProfileVersion,
                           long positionProfileRevision, String positionProfileFingerprint,
                           String personProfileId, int personProfileVersion,
                           LocalDate personProfileValidFrom, LocalDate personProfileValidTo,
                           Instant calculatedAt, List<TargetGapItem> items) { }

    record TargetGapItem(String positionRequirementId, String competencyVersionId,
                         String competencyCode, String competencyName, int competencyDefinitionVersion,
                         String scaleVersionId, int scaleDefinitionVersion,
                         String requiredLevelId, String requiredLevelCode, String requiredLevelLabel,
                         int requiredLevelOrder, String attainedLevelId, String attainedLevelCode,
                         String attainedLevelLabel, Integer attainedLevelOrder, Integer gap,
                         GapClassification classification, NotAssessedReason notAssessedReason,
                         String requirementClassification, String criticalityCode, int displayOrder) { }
}
