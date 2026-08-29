package com.primehr.gap.api;

import com.primehr.gap.domain.GapClassification;
import com.primehr.gap.domain.NotAssessedReason;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class CompetencyGapDtos {
    private CompetencyGapDtos() { }

    public record GenerateRequest(@NotNull @Positive Long employeeId,
            @NotBlank @Size(max = 128) String expectedHrmSourceFingerprint,
            @NotBlank @Size(max = 100) String requestKey) { }

    public record AnalysisSummaryResponse(String id, Long employeeId, String employeeNo,
            String employeeName, LocalDate analysisDate, String positionName, String plantillaName,
            int positionProfileVersion, int personProfileVersion, String prioritySchemeCode,
            int prioritySchemeVersion, long belowCount, long meetsCount, long exceedsCount,
            long notAssessedCount, Instant generatedAt) { }

    public record GapItemResponse(String id, String positionRequirementId, String competencyVersionId,
            String competencyCode, String competencyName, int competencyDefinitionVersion,
            String scaleVersionId, int scaleDefinitionVersion,
            String requiredLevelId, String requiredLevelCode, String requiredLevelLabel, int requiredLevelOrder,
            String attainedLevelId, String attainedLevelCode, String attainedLevelLabel, Integer attainedLevelOrder,
            Integer gap, GapClassification classification, NotAssessedReason notAssessedReason,
            String requirementClassification, String criticalityCode,
            String priorityLevelId, String priorityCode, String priorityLabel, Integer priorityRank,
            String matchedRuleId, String priorityExplanation, int displayOrder) { }

    public record AnalysisResponse(String id, Long employeeId, String employeeNo, String employeeName,
            Long appointmentId, Long jobPositionId, Long plantillaId, String hrmSourceFingerprint,
            String positionName, String plantillaName, Long salaryGrade, Long salaryStep,
            String positionProfileId, int positionProfileVersion, long positionProfileRevision,
            String personProfileId, int personProfileVersion, LocalDate personProfileValidFrom,
            LocalDate personProfileValidTo, String prioritySchemeId, String prioritySchemeCode,
            int prioritySchemeVersion, LocalDate analysisDate, String requestKey,
            String generatedBy, Instant generatedAt, List<GapItemResponse> items) { }
}
