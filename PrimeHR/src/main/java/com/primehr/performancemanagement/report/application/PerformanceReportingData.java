package com.primehr.performancemanagement.report.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class PerformanceReportingData {
    private PerformanceReportingData() { }

    public record RegisterQuery(LocalDate from, LocalDate to, boolean includeHistory, int page, int size,
                                String cycleId, String formType, Long businessUnitId, String employeeNo,
                                String ratingLabel) { }

    public record RegisterPage(LocalDate from, LocalDate to, boolean includeHistory, int page, int size,
                               long totalElements, int totalPages, List<RegisterRow> content) { }

    public record RegisterRow(String finalRatingId, String ratingCaseId, int ratingRevision,
                              String supersedesFinalRatingId, boolean currentRevision, String employeeNo,
                              String employeeName, String formType, String cycleId, Long businessUnitId,
                              String businessUnitName, LocalDate periodStart, LocalDate periodEnd,
                              BigDecimal finalScore, String ratingLabel, Instant finalizedAt,
                              String acknowledgmentStatus, String appealStatus, String interventionAction,
                              String interventionStatus, String calculationFingerprint) { }

    public record AggregateMetric(String code, String label, long numerator, Long denominator,
                                  BigDecimal value, String unit, String definition) { }

    public record AggregateBreakdown(String dimension, String key, String label, long count,
                                     BigDecimal averageScore) { }

    public record Analytics(LocalDate from, LocalDate to, int minimumCohort, int cohortSize,
                            boolean suppressed, String suppressionReason, List<AggregateMetric> metrics,
                            List<AggregateBreakdown> breakdowns) { }

    public record OutcomeQuery(Instant changedAfter, Instant changedThrough, int page, int size) { }

    public record OutcomePage(String schemaVersion, Instant changedAfter, Instant changedThrough,
                              Instant generatedAt, int page, int size, long totalElements, int totalPages,
                              List<Outcome> content) { }

    public record Outcome(String outcomeId, String ratingCaseId, int revisionNo,
                          String supersedesOutcomeId, String employeeNo, String cycleId, String formType,
                          Long businessUnitId, LocalDate periodStart, LocalDate periodEnd,
                          BigDecimal finalScore, String ratingLabel, Instant finalizedAt,
                          String acknowledgmentStatus, String appealStatus, String successorOutcomeId,
                          String interventionAction, String interventionStatus, String calculationFingerprint,
                          long recordVersion, Instant changedAt, String outcomeFingerprint) { }
}
