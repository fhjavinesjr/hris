package com.primehr.evidence;

import com.primehr.evidence.application.PrimeHrEvidenceAggregateSourcePort;
import com.primehr.learningdevelopment.evaluation.api.LdEvaluationDtos.EffectivenessResponse;
import com.primehr.learningdevelopment.evaluation.application.LdEvaluationService;
import com.primehr.performancemanagement.report.application.PerformanceReportingData.AggregateMetric;
import com.primehr.performancemanagement.report.application.PerformanceReportingData.Analytics;
import com.primehr.performancemanagement.report.application.PerformanceReportingService;
import com.primehr.rewardsrecognition.report.RrReportData;
import com.primehr.rewardsrecognition.report.RrReportService;
import com.primehr.security.LdReportingPermissionGuard;
import com.primehr.security.PerformanceReportingPermissionGuard;
import com.primehr.security.PrimeHrAction;
import com.primehr.security.RrAwardPermissionGuard;
import com.primehr.security.SuccessionPlanningPermissionGuard;
import com.primehr.succession.report.SuccessionReportData;
import com.primehr.succession.report.SuccessionReportService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrimeHrEvidenceAggregateSourcePortTest {
    private final PerformanceReportingService performance = mock(PerformanceReportingService.class);
    private final LdEvaluationService learning = mock(LdEvaluationService.class);
    private final RrReportService rewards = mock(RrReportService.class);
    private final SuccessionReportService succession = mock(SuccessionReportService.class);
    private final PerformanceReportingPermissionGuard performancePermission =
            mock(PerformanceReportingPermissionGuard.class);
    private final LdReportingPermissionGuard learningPermission = mock(LdReportingPermissionGuard.class);
    private final RrAwardPermissionGuard rewardsPermission = mock(RrAwardPermissionGuard.class);
    private final SuccessionPlanningPermissionGuard successionPermission =
            mock(SuccessionPlanningPermissionGuard.class);
    private final PrimeHrEvidenceAggregateSourcePort source = new PrimeHrEvidenceAggregateSourcePort(
            performance, learning, rewards, succession, performancePermission, learningPermission,
            rewardsPermission, successionPermission);

    @Test
    void preservesSourceSuppressionAndNeverEmitsSuppressedMetrics() {
        var leakedPerformanceMetric = new AggregateMetric("LEAK", "Leak", 1, 1L, BigDecimal.ONE,
                "count", "must not be exposed");
        when(performance.analytics(anyString(), any(), anyString(), anyString())).thenReturn(
                new Analytics(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 5, 2,
                        true, "Below cohort", List.of(leakedPerformanceMetric), List.of()));
        when(learning.effectiveness(anyString(), anyString(), anyString())).thenReturn(
                new EffectivenessResponse(Instant.parse("2026-11-05T00:00:00Z"), 5, 1, List.of()));
        when(rewards.analytics(anyString(), anyInt())).thenReturn(new RrReportData.Analytics(
                2026, 5, 2, true, "Below cohort", List.of(new RrReportData.Metric("LEAK", 1, 0))));
        when(succession.analytics(anyString(), anyString())).thenReturn(new SuccessionReportData.Analytics(
                5, 1, true, "Below cohort",
                List.of(new SuccessionReportData.CoverageMetric("LEAK", 1, 1))));

        var result = source.load("AGENCY", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                "EMP-1", "Bearer token", "correlation");

        assertThat(result).hasSize(4).allSatisfy(module -> {
            assertThat(module.suppressed()).isTrue();
            assertThat(module.metrics()).isEmpty();
        });
        verify(performancePermission).require(PerformanceReportingPermissionGuard.ANALYTICS, "Bearer token");
        verify(learningPermission).requireAnalytics("Bearer token");
        verify(rewardsPermission).requireAgency(RrAwardPermissionGuard.ANALYTICS, PrimeHrAction.ACCESS,
                "Bearer token");
        verify(successionPermission).require(SuccessionPlanningPermissionGuard.ANALYTICS,
                PrimeHrAction.ACCESS, "Bearer token");
    }

    @Test
    void checksEverySourcePermissionBeforeReadingAnyAggregate() {
        doThrow(new AccessDeniedException("denied")).when(learningPermission).requireAnalytics("Bearer token");

        assertThatThrownBy(() -> source.load("AGENCY", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), "EMP-1", "Bearer token", "correlation"))
                .isInstanceOf(AccessDeniedException.class);

        verify(performance, never()).analytics(anyString(), any(), anyString(), anyString());
        verify(learning, never()).effectiveness(anyString(), anyString(), anyString());
        verify(rewards, never()).analytics(anyString(), anyInt());
        verify(succession, never()).analytics(anyString(), anyString());
    }
}
