package com.primehr.performancemanagement.report;

import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.performancemanagement.domain.*;
import com.primehr.performancemanagement.infrastructure.*;
import com.primehr.performancemanagement.report.application.*;
import com.primehr.performancemanagement.report.domain.PerformanceReportMetadataSnapshot;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.ApplicationConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static com.primehr.performancemanagement.report.domain.PerformanceReportMetadataSnapshot.ReportKind.*;
import static com.primehr.performancemanagement.report.domain.PerformanceReportMetadataSnapshot.SourceType.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceFormalReportServiceTest {
    @Mock PerformanceCommitmentVersionRepository versions; @Mock PerformanceCommitmentSectionRepository sections;
    @Mock PerformanceCommitmentItemRepository items; @Mock PerformanceMonitoringCaseRepository monitoringCases;
    @Mock PerformanceMonitoringUpdateRepository updates; @Mock PerformanceRatingCaseRepository ratingCases;
    @Mock PerformanceFinalRatingRepository finalRatings; @Mock PerformanceRatingAssessmentRepository assessments;
    @Mock PerformanceRatingSourceItemRepository ratingSources; @Mock PerformanceRatingItemResultRepository itemResults;
    @Mock PerformanceRatingDimensionResultRepository dimensionResults; @Mock PerformanceCoachingSessionRepository coachingSessions;
    @Mock PerformanceCoachingActionItemRepository coachingActions; @Mock PerformanceInterventionRepository interventions;
    @Mock PerformanceInterventionCheckpointRepository checkpoints; @Mock PerformanceReportMetadataPreparationService metadata;
    @Mock PerformanceFormalReportRenderer renderer; @Mock PrimeHrAuditService audit;
    PerformanceFormalReportServiceImpl service;

    @BeforeEach void setup() {
        service = new PerformanceFormalReportServiceImpl(versions, sections, items, monitoringCases, updates,
                ratingCases, finalRatings, assessments, ratingSources, itemResults, dimensionResults,
                coachingSessions, coachingActions, interventions, checkpoints, metadata, renderer, audit);
    }

    @Test void approvedPlanningFormUsesPreparedSnapshotAndOwnRecordScope() {
        PerformanceCommitmentVersion version = commitment("commitment-1", 2);
        when(versions.findByIdAndAgencyId("commitment-1", "A")).thenReturn(Optional.of(version));
        PerformanceReportMetadataSnapshot prepared = snapshot(IPCR, COMMITMENT_VERSION, "E-1");
        when(metadata.commitment("A", IPCR, version, "Bearer token", "E-1", PermissionDataScope.OWN_RECORDS)).thenReturn(prepared);
        when(sections.findByAgencyIdAndCommitmentVersionIdOrderByDisplayOrder("A", "commitment-1")).thenReturn(List.of());
        when(items.findByAgencyIdAndCommitmentVersionIdOrderByCommitmentSectionIdAscDisplayOrderAsc("A", "commitment-1")).thenReturn(List.of());
        when(renderer.form(any())).thenReturn("%PDF-test".getBytes());
        assertThat(service.planningForm("A", "commitment-1", "Bearer token", "E-1", PermissionDataScope.OWN_RECORDS, "c"))
                .startsWith("%PDF".getBytes());
    }

    @Test void reportFailsClosedForMetadataFailureAndWrongOwner() {
        PerformanceCommitmentVersion version = commitment("commitment-1", 1);
        when(versions.findByIdAndAgencyId("commitment-1", "A")).thenReturn(Optional.of(version));
        when(metadata.commitment("A", IPCR, version, "Bearer token", "E-1", PermissionDataScope.OWN_RECORDS))
                .thenThrow(new ApplicationConflictException("Report metadata source is unavailable"));
        assertThatThrownBy(() -> service.planningForm("A", "commitment-1", "Bearer token", "E-1", PermissionDataScope.OWN_RECORDS, null))
                .isInstanceOf(ApplicationConflictException.class).hasMessageContaining("metadata");
        PerformanceReportMetadataSnapshot otherOwner = snapshot(IPCR, COMMITMENT_VERSION, "E-2");
        doReturn(otherOwner).when(metadata).commitment("A", IPCR, version, "Bearer token", "E-1", PermissionDataScope.OWN_RECORDS);
        assertThatThrownBy(() -> service.planningForm("A", "commitment-1", "Bearer token", "E-1", PermissionDataScope.OWN_RECORDS, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test void ratedFormUsesFrozenRatingSourceAccomplishment() {
        PerformanceFinalRating rating = mock(PerformanceFinalRating.class);
        when(rating.getId()).thenReturn("final-1"); when(rating.getRatingCaseId()).thenReturn("case-1");
        when(rating.getRevisionNo()).thenReturn(1); when(rating.getSourceAssessmentId()).thenReturn("assessment-1");
        when(rating.getRoundedScore()).thenReturn(new BigDecimal("4.75")); when(rating.getRatingLabel()).thenReturn("Outstanding");
        when(finalRatings.findByIdAndAgencyId("final-1", "A")).thenReturn(Optional.of(rating));
        PerformanceRatingCase ratingCase = mock(PerformanceRatingCase.class);
        when(ratingCase.getId()).thenReturn("case-1"); when(ratingCase.getStatus()).thenReturn(PerformanceRatingCase.Status.FINALIZED);
        when(ratingCase.getCommitmentVersionId()).thenReturn("commitment-1");
        when(ratingCases.findByIdAndAgencyId("case-1", "A")).thenReturn(Optional.of(ratingCase));
        PerformanceCommitmentVersion version = commitment("commitment-1", 1);
        when(versions.findByIdAndAgencyId("commitment-1", "A")).thenReturn(Optional.of(version));
        PerformanceReportMetadataSnapshot prepared = snapshot(IPCR, FINAL_RATING, "E-1");
        when(metadata.finalRating("A", IPCR, rating, version, "Bearer token", "E-1", PermissionDataScope.OWN_RECORDS)).thenReturn(prepared);
        PerformanceRatingAssessment assessment = mock(PerformanceRatingAssessment.class);
        when(assessment.getId()).thenReturn("assessment-1");
        when(assessments.findByIdAndAgencyId("assessment-1", "A")).thenReturn(Optional.of(assessment));
        PerformanceRatingSourceItem source = mock(PerformanceRatingSourceItem.class);
        when(source.getId()).thenReturn("source-1"); when(source.getCommitmentItemId()).thenReturn("item-1");
        when(source.getAccomplishmentNarrative()).thenReturn("Frozen final accomplishment");
        when(ratingSources.findByAgencyIdAndRatingCaseIdOrderByCommitmentSectionIdAscDisplayOrderAsc("A", "case-1")).thenReturn(List.of(source));
        PerformanceRatingItemResult score = mock(PerformanceRatingItemResult.class);
        when(score.getSourceItemId()).thenReturn("source-1"); when(score.getId()).thenReturn("result-1");
        when(score.getRoundedScore()).thenReturn(new BigDecimal("4.75"));
        when(itemResults.findByAgencyIdAndAssessmentId("A", "assessment-1")).thenReturn(List.of(score));
        when(dimensionResults.findByAgencyIdAndAssessmentId("A", "assessment-1")).thenReturn(List.of());
        PerformanceCommitmentItem item = mock(PerformanceCommitmentItem.class);
        when(item.getId()).thenReturn("item-1"); when(item.getOutputDescription()).thenReturn("Output");
        when(item.getPerformanceIndicator()).thenReturn("Indicator"); when(item.getSuccessStatement()).thenReturn("Success");
        when(items.findByAgencyIdAndCommitmentVersionIdOrderByCommitmentSectionIdAscDisplayOrderAsc("A", "commitment-1")).thenReturn(List.of(item));
        when(sections.findByAgencyIdAndCommitmentVersionIdOrderByDisplayOrder("A", "commitment-1")).thenReturn(List.of());
        when(renderer.form(any())).thenReturn("%PDF-test".getBytes());
        service.ratedForm("A", "final-1", "Bearer token", "E-1", PermissionDataScope.OWN_RECORDS, null);
        ArgumentCaptor<PerformanceFormalReportData.Form> captured = ArgumentCaptor.forClass(PerformanceFormalReportData.Form.class);
        verify(renderer).form(captured.capture());
        assertThat(captured.getValue().rows()).singleElement().extracting(PerformanceFormalReportData.FormRow::accomplishment)
                .isEqualTo("Frozen final accomplishment");
        verifyNoInteractions(updates);
    }

    @Test void coachingOutputNeverCarriesPrivateNotes() {
        PerformanceCoachingSession session = mock(PerformanceCoachingSession.class);
        when(session.getId()).thenReturn("coach-1"); when(session.getMonitoringCaseId()).thenReturn("monitor-1");
        when(session.getRevisionNo()).thenReturn(1); when(session.getVersion()).thenReturn(3L);
        when(session.getStatus()).thenReturn(PerformanceCoachingSession.Status.ISSUED);
        when(session.getAgenda()).thenReturn("Agenda"); when(session.getGoal()).thenReturn("Goal");
        when(session.getObservedIssue()).thenReturn("Issue"); when(session.getAgreedAction()).thenReturn("Action");
        when(coachingSessions.findByIdAndAgencyId("coach-1", "A")).thenReturn(Optional.of(session));
        PerformanceMonitoringCase monitoring = mock(PerformanceMonitoringCase.class);
        when(monitoring.getCommitmentVersionId()).thenReturn("commitment-1");
        when(monitoringCases.findByIdAndAgencyId("monitor-1", "A")).thenReturn(Optional.of(monitoring));
        PerformanceCommitmentVersion version = commitment("commitment-1", 1);
        when(versions.findByIdAndAgencyId("commitment-1", "A")).thenReturn(Optional.of(version));
        PerformanceReportMetadataSnapshot prepared = snapshot(COACHING, COACHING_SESSION, "E-1");
        when(metadata.coaching("A", session, version, "Bearer token", "E-1", PermissionDataScope.OWN_RECORDS)).thenReturn(prepared);
        when(coachingActions.findByAgencyIdAndCoachingSessionIdOrderByDueDateAsc("A", "coach-1")).thenReturn(List.of());
        when(renderer.coaching(any())).thenReturn("%PDF-test".getBytes());
        service.coaching("A", "coach-1", "Bearer token", "E-1", PermissionDataScope.OWN_RECORDS, null);
        verify(session, never()).getPrivateNotes();
    }

    private static PerformanceCommitmentVersion commitment(String id, int revision) {
        PerformanceCommitmentVersion value = mock(PerformanceCommitmentVersion.class);
        lenient().when(value.getId()).thenReturn(id); lenient().when(value.getFormType()).thenReturn("IPCR");
        lenient().when(value.getFormLabel()).thenReturn("IPCR");
        lenient().when(value.getStatus()).thenReturn(PerformanceCommitmentVersion.Status.APPROVED);
        lenient().when(value.getDefinitionVersion()).thenReturn(revision); lenient().when(value.getVersion()).thenReturn(4L);
        lenient().when(value.getContentFingerprint()).thenReturn("a".repeat(64)); return value;
    }

    private static PerformanceReportMetadataSnapshot snapshot(PerformanceReportMetadataSnapshot.ReportKind kind,
            PerformanceReportMetadataSnapshot.SourceType type, String employee) {
        PerformanceReportMetadataSnapshot value = mock(PerformanceReportMetadataSnapshot.class);
        lenient().when(value.getReportKind()).thenReturn(kind); lenient().when(value.getSourceType()).thenReturn(type);
        lenient().when(value.getEmployeeNo()).thenReturn(employee); lenient().when(value.getEmployeeName()).thenReturn("Employee One");
        lenient().when(value.getPositionName()).thenReturn("Officer V"); lenient().when(value.getBusinessUnitName()).thenReturn("HRM");
        lenient().when(value.getPeriodStart()).thenReturn(LocalDate.of(2026, 1, 1));
        lenient().when(value.getPeriodEnd()).thenReturn(LocalDate.of(2026, 12, 31));
        lenient().when(value.getAgencyName()).thenReturn("ISOFT TEST AGENCY");
        lenient().when(value.getMetadataFingerprint()).thenReturn("b".repeat(64));
        lenient().when(value.getCapturedBy()).thenReturn("tester");
        lenient().when(value.getCapturedAt()).thenReturn(Instant.parse("2026-10-23T00:00:00Z")); return value;
    }
}
