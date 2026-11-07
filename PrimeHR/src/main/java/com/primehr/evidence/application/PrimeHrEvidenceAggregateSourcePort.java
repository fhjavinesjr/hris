package com.primehr.evidence.application;

import com.primehr.evidence.application.EvidenceReadinessData.*;
import com.primehr.learningdevelopment.evaluation.application.LdEvaluationService;
import com.primehr.performancemanagement.report.application.PerformanceReportingData.RegisterQuery;
import com.primehr.performancemanagement.report.application.PerformanceReportingService;
import com.primehr.rewardsrecognition.report.RrReportService;
import com.primehr.security.*;
import com.primehr.succession.report.SuccessionReportService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class PrimeHrEvidenceAggregateSourcePort implements EvidenceAggregateSourcePort {
    private final PerformanceReportingService performance;
    private final LdEvaluationService learning;
    private final RrReportService rewards;
    private final SuccessionReportService succession;
    private final PerformanceReportingPermissionGuard performancePermission;
    private final LdReportingPermissionGuard learningPermission;
    private final RrAwardPermissionGuard rewardsPermission;
    private final SuccessionPlanningPermissionGuard successionPermission;

    public PrimeHrEvidenceAggregateSourcePort(PerformanceReportingService performance,
            LdEvaluationService learning, RrReportService rewards, SuccessionReportService succession,
            PerformanceReportingPermissionGuard performancePermission,
            LdReportingPermissionGuard learningPermission, RrAwardPermissionGuard rewardsPermission,
            SuccessionPlanningPermissionGuard successionPermission) {
        this.performance = performance;
        this.learning = learning;
        this.rewards = rewards;
        this.succession = succession;
        this.performancePermission = performancePermission;
        this.learningPermission = learningPermission;
        this.rewardsPermission = rewardsPermission;
        this.successionPermission = successionPermission;
    }

    @Override
    public List<ModuleAggregate> load(String agencyId, LocalDate from, LocalDate to, String actor,
            String authorization, String correlationId) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new IllegalArgumentException("A valid analytics date range is required");
        }
        performancePermission.require(PerformanceReportingPermissionGuard.ANALYTICS, authorization);
        learningPermission.requireAnalytics(authorization);
        rewardsPermission.requireAgency(RrAwardPermissionGuard.ANALYTICS, PrimeHrAction.ACCESS, authorization);
        successionPermission.require(SuccessionPlanningPermissionGuard.ANALYTICS, PrimeHrAction.ACCESS, authorization);

        List<ModuleAggregate> result = new ArrayList<>();
        var performanceData = performance.analytics(agencyId,
                new RegisterQuery(from, to, false, 0, 100, null, null, null, null, null),
                actor, correlationId);
        result.add(new ModuleAggregate("PERFORMANCE_MANAGEMENT", performanceData.minimumCohort(),
                performanceData.cohortSize(), performanceData.suppressed(), performanceData.suppressionReason(),
                performanceData.suppressed() ? List.of()
                        : performanceData.metrics().stream().map(value -> new ModuleMetric(value.code(), value.label(),
                                value.value() == null ? null : value.value().toPlainString(), value.unit())).toList()));

        var learningData = learning.effectiveness(agencyId, actor, correlationId);
        boolean learningSuppressed = learningData.rows().isEmpty() && learningData.suppressedGroups() > 0;
        result.add(new ModuleAggregate("LEARNING_AND_DEVELOPMENT", learningData.minimumCohort(), 0,
                learningSuppressed,
                learningSuppressed ? "All L&D aggregate groups are below the configured minimum cohort." : null,
                learningData.rows().stream().map(value -> new ModuleMetric(
                        value.dimension() + ":" + value.sourceId(), value.label(),
                        value.verifiedResults() + "/" + value.passedResults()
                                + (value.averageScore() == null ? "" : "/" + value.averageScore().toPlainString()),
                        "verified/passed/average-score")).toList()));

        var rewardsData = rewards.analytics(agencyId, to.getYear());
        result.add(new ModuleAggregate("REWARDS_AND_RECOGNITION", rewardsData.minimumCohort(),
                rewardsData.cohortSize(), rewardsData.suppressed(), rewardsData.suppressionReason(),
                rewardsData.suppressed() ? List.of()
                        : rewardsData.metrics().stream().map(value -> new ModuleMetric(value.category(), value.category(),
                                value.awards() + "/" + value.revoked(), "awards/revoked")).toList()));

        var successionData = succession.analytics(agencyId, actor);
        result.add(new ModuleAggregate("CAREER_AND_SUCCESSION", successionData.minimumCohort(),
                successionData.cohortSize(), successionData.suppressed(), successionData.suppressionReason(),
                successionData.suppressed() ? List.of()
                        : successionData.metrics().stream().map(value -> new ModuleMetric(value.coverageStatus(),
                                value.coverageStatus(), value.positions() + "/" + value.benchMembers(),
                                "positions/bench-members")).toList()));
        return List.copyOf(result);
    }
}
