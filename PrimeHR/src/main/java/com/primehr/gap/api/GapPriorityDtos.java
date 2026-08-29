package com.primehr.gap.api;

import com.primehr.gap.domain.GapClassification;
import com.primehr.gap.domain.GapPrioritySchemeStatus;
import com.primehr.positionprofile.domain.RequirementClassification;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class GapPriorityDtos {
    private GapPriorityDtos() { }

    public record CreateSchemeRequest(@NotBlank @Size(max = 50) String code,
            @NotBlank @Size(max = 200) String name, @Size(max = 2000) String description,
            @PositiveOrZero int displayOrder, LocalDate effectiveFrom, LocalDate effectiveTo) { }

    public record UpdateSchemeRequest(@NotBlank @Size(max = 50) String code,
            @NotBlank @Size(max = 200) String name, @Size(max = 2000) String description,
            @PositiveOrZero int displayOrder, LocalDate effectiveFrom, LocalDate effectiveTo,
            @NotNull @PositiveOrZero Long recordVersion) { }

    public record TransitionRequest(@NotNull @PositiveOrZero Long recordVersion,
            @Size(max = 1000) String reason) { }

    public record PublishRequest(@NotNull @PositiveOrZero Long recordVersion,
            @NotBlank @Size(max = 1000) String reason) { }

    public record CreateLevelRequest(@NotBlank @Size(max = 50) String code,
            @NotBlank @Size(max = 150) String label, @Size(max = 1000) String description,
            @Positive int priorityRank, @PositiveOrZero int displayOrder) { }

    public record UpdateLevelRequest(@NotBlank @Size(max = 50) String code,
            @NotBlank @Size(max = 150) String label, @Size(max = 1000) String description,
            @Positive int priorityRank, @PositiveOrZero int displayOrder,
            @NotNull @PositiveOrZero Long recordVersion) { }

    public record CreateRuleRequest(@NotNull GapClassification gapClassification,
            @Positive Integer minimumGap, @Positive Integer maximumGap,
            RequirementClassification requirementClassification, @Size(max = 50) String criticalityCode,
            @NotBlank String priorityLevelId, @Size(max = 1000) String explanation,
            @PositiveOrZero int displayOrder) { }

    public record UpdateRuleRequest(@NotNull GapClassification gapClassification,
            @Positive Integer minimumGap, @Positive Integer maximumGap,
            RequirementClassification requirementClassification, @Size(max = 50) String criticalityCode,
            @NotBlank String priorityLevelId, @Size(max = 1000) String explanation,
            @PositiveOrZero int displayOrder, @NotNull @PositiveOrZero Long recordVersion) { }

    public record LevelResponse(String id, String code, String label, String description,
            int priorityRank, boolean active, int displayOrder, long recordVersion) { }

    public record RuleResponse(String id, GapClassification gapClassification, Integer minimumGap,
            Integer maximumGap, RequirementClassification requirementClassification, String criticalityCode,
            String priorityLevelId, String priorityCode, String priorityLabel, int priorityRank,
            String explanation, boolean active, int displayOrder, long recordVersion) { }

    public record SchemeSummaryResponse(String id, String code, String name, GapPrioritySchemeStatus status,
            int definitionVersion, String supersedesId, LocalDate effectiveFrom, LocalDate effectiveTo,
            long recordVersion) { }

    public record SchemeResponse(String id, String code, String name, String description,
            GapPrioritySchemeStatus status, int definitionVersion, String supersedesId,
            int displayOrder, LocalDate effectiveFrom, LocalDate effectiveTo, long recordVersion,
            String publishedBy, Instant publishedAt, List<LevelResponse> levels, List<RuleResponse> rules) { }
}
