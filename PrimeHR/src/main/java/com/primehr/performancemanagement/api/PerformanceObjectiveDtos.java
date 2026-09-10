package com.primehr.performancemanagement.api;

import com.primehr.performancemanagement.domain.PerformanceObjectiveVersion;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class PerformanceObjectiveDtos {
    private PerformanceObjectiveDtos() {}
    public record ObjectiveInput(@NotBlank @Size(max=80) String code,@NotBlank @Size(max=200) String title,
        @NotBlank @Size(max=2000) String objectiveStatement,@NotBlank @Size(max=2000) String expectedOutcome,
        @NotBlank @Size(max=1000) String strategyReference,@NotNull PerformanceObjectiveVersion.Level level,
        @Positive Long organizationId,@Size(max=36) String parentObjectiveVersionId,
        @NotBlank @Size(max=36) String policyVersionId,@Size(max=36) String indicatorVersionId,
        BigDecimal targetValue,BigDecimal targetFrom,BigDecimal targetTo,@Size(max=100) String unitOfMeasure,
        Long recordVersion) {}
    public record Transition(@NotNull Long recordVersion,@NotBlank @Size(max=1000) String reason,
        LocalDate effectiveFrom,LocalDate effectiveTo) {}
    public record ObjectiveResponse(String id,String objectiveId,String code,int definitionVersion,String supersedesId,
        String title,String objectiveStatement,String expectedOutcome,String strategyReference,String level,
        Long organizationId,String organizationCode,String organizationName,Long parentAreaId,
        String parentObjectiveVersionId,String policyVersionId,String indicatorVersionId,BigDecimal targetValue,
        BigDecimal targetFrom,BigDecimal targetTo,String unitOfMeasure,String sourceFingerprint,String status,
        LocalDate effectiveFrom,LocalDate effectiveTo,long recordVersion) {}
    public record ReadinessResponse(boolean ready,List<String> errors) {}
}
