package com.primehr.succession.report;

import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.succession.application.SuccessionContinuityService;
import com.primehr.succession.application.SuccessionDevelopmentService;
import com.primehr.succession.application.SuccessionReadinessService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SuccessionReportServiceImpl implements SuccessionReportService {
    private final SuccessionReadinessService readiness;
    private final SuccessionDevelopmentService development;
    private final SuccessionContinuityService continuity;
    private final SuccessionReportRenderer renderer;
    private final int minimumCohort;

    public SuccessionReportServiceImpl(SuccessionReadinessService readiness,
                                       SuccessionDevelopmentService development,
                                       SuccessionContinuityService continuity,
                                       SuccessionReportRenderer renderer,
                                       @Value("${primehr.reporting.minimum-aggregate-cohort:5}") int minimumCohort) {
        if (minimumCohort < 2 || minimumCohort > 100) {
            throw new IllegalArgumentException("minimum aggregate cohort must be between 2 and 100");
        }
        this.readiness = readiness;
        this.development = development;
        this.continuity = continuity;
        this.renderer = renderer;
        this.minimumCohort = minimumCohort;
    }

    @Override
    public byte[] readiness(String agency, String id, String actor, PermissionDataScope scope) {
        var value = readiness.get(agency, id, actor, scope);
        List<SuccessionReportData.Row> rows = new ArrayList<>();
        add(rows, "Assessment", "Employee", value.employeeName() + " (" + value.employeeNo() + ")");
        add(rows, "Assessment", "Target position", value.targetPositionName());
        add(rows, "Assessment", "Status", value.status());
        add(rows, "Competency comparison", "Below / Meets / Exceeds / Not assessed",
                value.belowCount() + " / " + value.meetsCount() + " / " + value.exceedsCount() + " / " + value.notAssessedCount());
        add(rows, "Human recommendation", "Readiness level", value.recommendedLevelLabel());
        if (scope != PermissionDataScope.OWN_RECORDS) {
            add(rows, "Human recommendation", "Narrative", value.recommendationNarrative());
        }
        if (value.decision() != null) {
            add(rows, "Authorized decision", "Outcome", value.decision().outcome());
            add(rows, "Authorized decision", "Employee-visible reason", value.decision().employeeVisibleReason());
        }
        value.findings().stream().filter(f -> scope != PermissionDataScope.OWN_RECORDS || f.employeeVisible())
                .forEach(f -> add(rows, "Criterion finding", f.criterionLabel(), f.outcome() + " - " + f.rationale()));
        return renderer.render("Candidate Readiness Assessment Summary",
                "Confidential decision-support record. It is not a rank or entitlement to appointment or promotion.", actor, rows);
    }

    @Override
    public byte[] development(String agency, String id, String actor, PermissionDataScope scope) {
        var value = development.get(agency, id, actor, scope);
        List<SuccessionReportData.Row> rows = new ArrayList<>();
        add(rows, "Development plan", "Employee", value.employeeName() + " (" + value.employeeNo() + ")");
        add(rows, "Development plan", "Target position", value.targetPositionName());
        add(rows, "Development plan", "Status", value.status());
        add(rows, "Development plan", "Overall goal", value.overallGoal());
        add(rows, "Development plan", "Coverage", value.startsOn() + " to " + value.targetCompletionDate());
        value.milestones().forEach(m -> add(rows, "Milestone", m.title(), m.status() + " - due " + m.dueDate() + " - " + m.measurableOutcome()));
        value.reviews().forEach(r -> add(rows, "Periodic review", String.valueOf(r.reviewDate()), r.recommendation() + " - " + r.employeeVisibleSummary()));
        value.membershipHistory().forEach(m -> add(rows, "Pool membership lineage", m.eventType(), m.resultingStatus() + " - " + m.employeeVisibleReason()));
        return renderer.render("Succession Development Plan and Review History",
                "Employee development record only; it creates no promotion, appointment, enrollment, or salary transaction.", actor, rows);
    }

    @Override
    public byte[] continuity(String agency, String id, String actor, PermissionDataScope scope) {
        requireConfidentialScope(scope);
        var value = continuity.getPlan(agency, id, actor, scope);
        List<SuccessionReportData.Row> rows = new ArrayList<>();
        add(rows, "Position", "Target", value.positionName());
        add(rows, "Position", "Risk", value.riskLevel() + " - " + value.riskReasonSnapshot());
        add(rows, "Continuity plan", "Status / coverage", value.status() + " / " + value.coverageStatus());
        add(rows, "Continuity plan", "Critical functions", value.criticalFunctions());
        add(rows, "Continuity plan", "Interim arrangement", value.interimArrangement());
        add(rows, "Continuity plan", "Actions", value.continuityActions());
        add(rows, "Continuity plan", "Activation triggers", value.activationTriggers());
        add(rows, "Continuity plan", "Recovery target / review", value.targetRecoveryDays() + " days / " + value.reviewOn());
        value.benchCoverage().forEach(b -> add(rows, "Confidential bench snapshot", b.employeeName() + " (" + b.employeeNo() + ")",
                b.readinessLevelCode() + " - membership through " + v(b.membershipExpiresOn())));
        return renderer.render("Critical Position and Continuity Plan",
                "Restricted, position-based continuity record. Bench entries are not ranked and do not confer appointment rights.", actor, rows);
    }

    @Override
    public byte[] referral(String agency, String id, String actor, PermissionDataScope scope) {
        var value = continuity.getReferral(agency, id, actor, scope);
        List<SuccessionReportData.Row> rows = new ArrayList<>();
        add(rows, "Referral packet", "Reference", value.referralCode());
        add(rows, "Referral packet", "Employee", value.employeeName() + " (" + value.employeeNo() + ")");
        add(rows, "Referral packet", "Target position", value.targetPositionName());
        add(rows, "Referral packet", "Status", value.status());
        add(rows, "Referral packet", "Employee-visible notice", value.employeeVisibleNotice());
        add(rows, "RSP linkage", "Vacancy publication", v(value.vacancyPublicationId()) + " / " + v(value.vacancyStatus()));
        value.events().forEach(e -> add(rows, "Referral lineage", e.eventType(), e.resultingStatus() + " - " + e.employeeVisibleReason()));
        return renderer.render("Authorized RSP Referral Packet",
                "Referral only. RSP remains authoritative for application, screening, comparative evaluation, selection, and appointment.", actor, rows);
    }

    @Override
    public byte[] coverageRegister(String agency, String actor, PermissionDataScope scope) {
        requireConfidentialScope(scope);
        var values = continuity.listPlans(agency, actor, scope);
        List<SuccessionReportData.Row> rows = new ArrayList<>();
        values.forEach(value -> add(rows, "Position coverage register", value.positionName(),
                value.status() + " - " + value.coverageStatus() + " - bench " + value.benchCount() + " - review " + value.reviewOn()));
        if (rows.isEmpty()) add(rows, "Position coverage register", "No records", "No continuity plans are visible in the authorized scope.");
        return renderer.render("Position-based Succession Coverage Register",
                "Restricted aggregate register. Named bench members are excluded from this register.", actor, rows);
    }

    @Override
    public SuccessionReportData.Analytics analytics(String agency, String actor) {
        var values = continuity.listPlans(agency, actor, PermissionDataScope.AGENCY_WIDE).stream()
                .filter(value -> "FINALIZED".equals(value.status())).toList();
        int cohort = values.size();
        if (cohort < minimumCohort) {
            return new SuccessionReportData.Analytics(minimumCohort, cohort, true,
                    "Results are suppressed because the finalized position cohort is below the configured minimum.", List.of());
        }
        var grouped = new LinkedHashMap<String, long[]>();
        values.forEach(value -> {
            var metric = grouped.computeIfAbsent(value.coverageStatus(), ignored -> new long[2]);
            metric[0]++;
            metric[1] += value.benchCount();
        });
        return new SuccessionReportData.Analytics(minimumCohort, cohort, false, null,
                grouped.entrySet().stream().map(entry -> new SuccessionReportData.CoverageMetric(
                        entry.getKey(), entry.getValue()[0], entry.getValue()[1])).toList());
    }

    private static void requireConfidentialScope(PermissionDataScope scope) {
        if (scope != PermissionDataScope.ASSIGNED_RECORDS && scope != PermissionDataScope.AGENCY_WIDE) {
            throw new org.springframework.security.access.AccessDeniedException("Confidential continuity reporting requires Assigned Records or Agency Wide scope");
        }
    }

    private static void add(List<SuccessionReportData.Row> rows, String section, String label, Object value) {
        rows.add(new SuccessionReportData.Row(section, label, v(value)));
    }

    private static String v(Object value) { return value == null ? "-" : String.valueOf(value); }
}
