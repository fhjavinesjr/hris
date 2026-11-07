package com.primehr.performancemanagement.report;

import com.primehr.performancemanagement.domain.*;
import com.primehr.performancemanagement.infrastructure.*;
import com.primehr.performancemanagement.report.application.PerformanceReportingData.*;
import com.primehr.performancemanagement.report.application.PerformanceReportingServiceImpl;
import com.primehr.shared.audit.PrimeHrAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PerformanceReportingServiceTest {
    private final PerformanceFinalRatingRepository ratings = mock(PerformanceFinalRatingRepository.class);
    private final PerformanceRatingCaseRepository cases = mock(PerformanceRatingCaseRepository.class);
    private final PerformanceCommitmentVersionRepository commitments = mock(PerformanceCommitmentVersionRepository.class);
    private final PerformanceRatingAcknowledgmentRepository acknowledgments = mock(PerformanceRatingAcknowledgmentRepository.class);
    private final PerformanceRatingAppealRepository appeals = mock(PerformanceRatingAppealRepository.class);
    private final PerformanceInterventionRepository interventions = mock(PerformanceInterventionRepository.class);
    private final PrimeHrAuditService audit = mock(PrimeHrAuditService.class);
    private PerformanceReportingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PerformanceReportingServiceImpl(ratings, cases, commitments, acknowledgments, appeals,
                interventions, audit, 2);
        when(acknowledgments.findByAgencyId("A")).thenReturn(List.of());
        when(appeals.findByAgencyId("A")).thenReturn(List.of());
        when(interventions.findByAgencyId("A")).thenReturn(List.of());
    }

    @Test
    void registerUsesCurrentRevisionByDefaultAndPaginates() {
        fixture(List.of(
                rating("R-OLD", "CASE-1", 1, null, "3.00", "Satisfactory", "2026-06-01T00:00:00Z"),
                rating("R-NEW", "CASE-1", 2, "R-OLD", "4.00", "Very Satisfactory", "2026-07-01T00:00:00Z"),
                rating("R-2", "CASE-2", 1, null, "5.00", "Outstanding", "2026-08-01T00:00:00Z")),
                List.of(ratingCase("CASE-1", "C-1", "EMP-1", "IPCR", 10L),
                        ratingCase("CASE-2", "C-2", "EMP-2", "DPCR", 20L)),
                List.of(commitment("C-1", "Office A"), commitment("C-2", "Office B")));

        RegisterPage first = service.register("A", query(false, 0, 1), "admin", "corr");
        RegisterPage second = service.register("A", query(false, 1, 1), "admin", "corr");
        RegisterPage history = service.register("A", query(true, 0, 10), "admin", "corr");

        assertThat(first.totalElements()).isEqualTo(2);
        assertThat(first.totalPages()).isEqualTo(2);
        assertThat(first.content()).extracting(RegisterRow::finalRatingId).containsExactly("R-2");
        assertThat(second.content()).extracting(RegisterRow::finalRatingId).containsExactly("R-NEW");
        assertThat(history.content()).extracting(RegisterRow::finalRatingId)
                .containsExactly("R-2", "R-NEW", "R-OLD");
        assertThat(history.content()).filteredOn(row -> row.finalRatingId().equals("R-OLD"))
                .allMatch(row -> !row.currentRevision());
    }

    @Test
    void analyticsSuppressesSmallCohortsAndBreakdownCellsIndependently() {
        fixture(List.of(
                rating("R-1", "CASE-1", 1, null, "4.00", "Very Satisfactory", "2026-07-01T00:00:00Z"),
                rating("R-2", "CASE-2", 1, null, "2.00", "Satisfactory", "2026-07-02T00:00:00Z"),
                rating("R-3", "CASE-3", 1, null, "3.00", "Very Satisfactory", "2026-07-03T00:00:00Z")),
                List.of(ratingCase("CASE-1", "C-1", "EMP-1", "IPCR", 10L),
                        ratingCase("CASE-2", "C-2", "EMP-2", "IPCR", 10L),
                        ratingCase("CASE-3", "C-3", "EMP-3", "DPCR", 20L)),
                List.of(commitment("C-1", "Office A"), commitment("C-2", "Office A"),
                        commitment("C-3", "Office B")));

        Analytics suppressed = service.analytics("A", new RegisterQuery(LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), false, 0, 100, null, null, null, "EMP-1", null),
                "admin", "corr");
        Analytics visible = service.analytics("A", query(false, 0, 100), "admin", "corr");

        assertThat(suppressed.suppressed()).isTrue();
        assertThat(suppressed.cohortSize()).isZero();
        assertThat(suppressed.metrics()).isEmpty();
        assertThat(suppressed.breakdowns()).isEmpty();
        assertThat(visible.suppressed()).isFalse();
        assertThat(visible.metrics()).filteredOn(metric -> metric.code().equals("AVERAGE_FINAL_SCORE"))
                .extracting(AggregateMetric::value).containsExactly(new BigDecimal("3.0000"));
        assertThat(visible.breakdowns()).extracting(AggregateBreakdown::dimension, AggregateBreakdown::key)
                .contains(tuple("FORM_TYPE", "IPCR"), tuple("BUSINESS_UNIT", "10"),
                        tuple("RATING_LABEL", "Very Satisfactory"))
                .doesNotContain(tuple("FORM_TYPE", "DPCR"), tuple("BUSINESS_UNIT", "20"),
                        tuple("RATING_LABEL", "Satisfactory"));
    }

    @Test
    void outcomeFeedReturnsOnlyCurrentRevisionInExclusiveInclusiveChangeWindow() {
        PerformanceFinalRating old = rating("R-OLD", "CASE-1", 1, null, "3.00", "Satisfactory",
                "2026-06-01T00:00:00Z");
        PerformanceFinalRating current = rating("R-NEW", "CASE-1", 2, "R-OLD", "4.00", "Very Satisfactory",
                "2026-07-01T00:00:00Z");
        PerformanceRatingAcknowledgment acknowledgment = mock(PerformanceRatingAcknowledgment.class);
        when(acknowledgment.getFinalRatingId()).thenReturn("R-NEW");
        when(acknowledgment.getStatus()).thenReturn(PerformanceRatingAcknowledgment.Status.ACKNOWLEDGED);
        when(acknowledgment.getUpdatedAt()).thenReturn(Instant.parse("2026-07-10T00:00:00Z"));
        when(acknowledgments.findByAgencyId("A")).thenReturn(List.of(acknowledgment));
        fixture(List.of(old, current), List.of(ratingCase("CASE-1", "C-1", "EMP-1", "IPCR", 10L)),
                List.of(commitment("C-1", "Office A")));

        OutcomePage excludedAtLowerBound = service.outcomes("A", new OutcomeQuery(
                Instant.parse("2026-07-10T00:00:00Z"), Instant.parse("2026-07-11T00:00:00Z"), 0, 100),
                "consumer", "corr");
        OutcomePage includedAtUpperBound = service.outcomes("A", new OutcomeQuery(
                Instant.parse("2026-07-09T00:00:00Z"), Instant.parse("2026-07-10T00:00:00Z"), 0, 100),
                "consumer", "corr");

        assertThat(excludedAtLowerBound.content()).isEmpty();
        assertThat(includedAtUpperBound.schemaVersion()).isEqualTo("1.0");
        assertThat(includedAtUpperBound.content()).singleElement().satisfies(outcome -> {
            assertThat(outcome.outcomeId()).isEqualTo("R-NEW");
            assertThat(outcome.revisionNo()).isEqualTo(2);
            assertThat(outcome.acknowledgmentStatus()).isEqualTo("ACKNOWLEDGED");
            assertThat(outcome.outcomeFingerprint()).hasSize(64);
        });
        assertThat(Outcome.class.getRecordComponents()).extracting(component -> component.getName().toLowerCase())
                .noneMatch(name -> name.contains("note") || name.contains("reason") || name.contains("narrative")
                        || name.contains("response") || name.contains("support") || name.contains("objective"));
    }

    @Test
    void validatesPrivacyThresholdAndQueryBounds() {
        assertThatThrownBy(() -> new PerformanceReportingServiceImpl(ratings, cases, commitments, acknowledgments,
                appeals, interventions, audit, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.register("A", new RegisterQuery(LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 1, 1), false, 0, 25, null, null, null, null, null), "a", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.outcomes("A", new OutcomeQuery(Instant.EPOCH, Instant.EPOCH, 0, 100),
                "a", null)).isInstanceOf(IllegalArgumentException.class);
    }

    private void fixture(List<PerformanceFinalRating> finalRatings, List<PerformanceRatingCase> ratingCases,
            List<PerformanceCommitmentVersion> versions) {
        when(ratings.findByAgencyIdOrderByFinalizedAtDesc("A")).thenReturn(finalRatings);
        when(cases.findByAgencyIdOrderByOpenedAtDesc("A")).thenReturn(ratingCases);
        when(commitments.findByAgencyIdOrderByCreatedAtDesc("A")).thenReturn(versions);
    }

    private static RegisterQuery query(boolean history, int page, int size) {
        return new RegisterQuery(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), history, page, size,
                null, null, null, null, null);
    }

    private static PerformanceFinalRating rating(String id, String caseId, int revision, String supersedes,
            String score, String label, String finalizedAt) {
        PerformanceFinalRating value = mock(PerformanceFinalRating.class);
        when(value.getId()).thenReturn(id);
        when(value.getRatingCaseId()).thenReturn(caseId);
        when(value.getRevisionNo()).thenReturn(revision);
        when(value.getSupersedesId()).thenReturn(supersedes);
        when(value.getRoundedScore()).thenReturn(new BigDecimal(score));
        when(value.getRatingLabel()).thenReturn(label);
        when(value.getFinalizedAt()).thenReturn(Instant.parse(finalizedAt));
        when(value.getCalculationFingerprint()).thenReturn("calculation-" + id);
        when(value.getVersion()).thenReturn((long) revision);
        return value;
    }

    private static PerformanceRatingCase ratingCase(String id, String commitmentId, String employeeNo,
            String formType, Long businessUnitId) {
        PerformanceRatingCase value = mock(PerformanceRatingCase.class);
        when(value.getId()).thenReturn(id);
        when(value.getCommitmentVersionId()).thenReturn(commitmentId);
        when(value.getOwnerEmployeeNo()).thenReturn(employeeNo);
        when(value.getOwnerName()).thenReturn("Employee " + employeeNo);
        when(value.getFormType()).thenReturn(formType);
        when(value.getCycleId()).thenReturn("CYCLE-2026");
        when(value.getBusinessUnitId()).thenReturn(businessUnitId);
        return value;
    }

    private static PerformanceCommitmentVersion commitment(String id, String businessUnit) {
        PerformanceCommitmentVersion value = mock(PerformanceCommitmentVersion.class);
        when(value.getId()).thenReturn(id);
        when(value.getBusinessUnitName()).thenReturn(businessUnit);
        when(value.getPeriodStart()).thenReturn(LocalDate.of(2026, 1, 1));
        when(value.getPeriodEnd()).thenReturn(LocalDate.of(2026, 12, 31));
        return value;
    }
}
