package com.primehr.rsp.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primehr.rsp.applicant.infrastructure.ApplicationDocumentSnapshotRepository;
import com.primehr.rsp.evaluation.domain.ComparativeEvaluation;
import com.primehr.rsp.evaluation.domain.EvaluationProceeding;
import com.primehr.rsp.evaluation.infrastructure.*;
import com.primehr.rsp.handoff.infrastructure.AppointmentHandoffRepository;
import com.primehr.rsp.screening.infrastructure.ScreeningEvidenceLinkRepository;
import com.primehr.rsp.selection.domain.SelectionCase;
import com.primehr.rsp.selection.infrastructure.*;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.ApplicationConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class RspFormalReportServiceImplTest {
    private EvaluationProceedingRepository proceedings;
    private ComparativeEvaluationRepository comparisons;
    private SelectionCaseRepository selections;
    private RspFormalReportService service;

    @BeforeEach
    void setUp() {
        proceedings = mock(EvaluationProceedingRepository.class);
        comparisons = mock(ComparativeEvaluationRepository.class);
        selections = mock(SelectionCaseRepository.class);
        service = new RspFormalReportServiceImpl(proceedings, comparisons,
                mock(ComparativeEvaluationItemRepository.class), mock(EvaluationCandidateRepository.class),
                mock(HrmpsbMeetingRepository.class), mock(HrmpsbResolutionRepository.class),
                mock(EvaluationEvidenceRepository.class), selections,
                mock(SelectionCandidateDecisionRepository.class), mock(OfferResponseRepository.class),
                mock(SelectionNoticeRepository.class), mock(AppointmentHandoffRepository.class),
                mock(ApplicationDocumentSnapshotRepository.class), mock(ScreeningEvidenceLinkRepository.class),
                mock(RspFormalReportRenderer.class), mock(PrimeHrAuditService.class), new ObjectMapper());
    }

    @Test
    void draftProceedingCannotProduceFormalComparativeReport() {
        EvaluationProceeding proceeding = mock(EvaluationProceeding.class);
        when(proceeding.getStatus()).thenReturn(EvaluationProceeding.Status.FOR_DELIBERATION);
        when(proceedings.findByIdAndAgencyId("proceeding-1", "agency-1"))
                .thenReturn(Optional.of(proceeding));

        assertThatThrownBy(() -> service.comparative("agency-1", "proceeding-1", "001", null))
                .isInstanceOf(ApplicationConflictException.class).hasMessageContaining("FINALIZED");
        verifyNoInteractions(comparisons);
    }

    @Test
    void mismatchedComparativeFingerprintFailsBeforeRendering() {
        EvaluationProceeding proceeding = mock(EvaluationProceeding.class);
        ComparativeEvaluation comparison = mock(ComparativeEvaluation.class);
        when(proceeding.getStatus()).thenReturn(EvaluationProceeding.Status.FINALIZED);
        when(proceeding.getId()).thenReturn("proceeding-1");
        when(proceeding.getPolicyFingerprint()).thenReturn("policy-fingerprint");
        when(comparison.getPolicyFingerprint()).thenReturn("policy-fingerprint");
        when(comparison.getCalculationSnapshot()).thenReturn("{}");
        when(comparison.getCalculationFingerprint()).thenReturn("not-the-snapshot-hash");
        when(proceedings.findByIdAndAgencyId("proceeding-1", "agency-1"))
                .thenReturn(Optional.of(proceeding));
        when(comparisons.findByAgencyIdAndProceedingIdAndStatus("agency-1", "proceeding-1",
                ComparativeEvaluation.Status.FINALIZED)).thenReturn(Optional.of(comparison));

        assertThatThrownBy(() -> service.comparative("agency-1", "proceeding-1", "001", null))
                .isInstanceOf(ApplicationConflictException.class).hasMessageContaining("fingerprint");
    }

    @Test
    void nonTerminalSelectionCannotProduceFormalReport() {
        SelectionCase selection = mock(SelectionCase.class);
        when(selection.getStatus()).thenReturn(SelectionCase.Status.APPROVED);
        when(selections.findByIdAndAgencyId("selection-1", "agency-1"))
                .thenReturn(Optional.of(selection));

        assertThatThrownBy(() -> service.selection("agency-1", "selection-1", "001", null))
                .isInstanceOf(ApplicationConflictException.class)
                .hasMessageContaining("FINALIZED or SUPERSEDED");
        verifyNoInteractions(proceedings);
    }

    @Test
    void staleFinalSelectionSourceCannotProduceFormalReport() {
        SelectionCase selection = mock(SelectionCase.class);
        EvaluationProceeding proceeding = mock(EvaluationProceeding.class);
        ComparativeEvaluation comparison = mock(ComparativeEvaluation.class);
        when(selection.getStatus()).thenReturn(SelectionCase.Status.SUPERSEDED);
        when(selection.getOutcome()).thenReturn(SelectionCase.Outcome.NO_SELECTION);
        when(selection.getProceedingId()).thenReturn("proceeding-1");
        when(selection.getComparativeEvaluationId()).thenReturn("comparison-1");
        when(selection.getComparativeFingerprint()).thenReturn("stored-comparison-fingerprint");
        when(selection.getSourceSnapshot()).thenReturn("{}");
        when(selection.getSourceFingerprint()).thenReturn("stale-source-fingerprint");
        when(proceeding.getStatus()).thenReturn(EvaluationProceeding.Status.FINALIZED);
        when(comparison.getProceedingId()).thenReturn("proceeding-1");
        when(comparison.getStatus()).thenReturn(ComparativeEvaluation.Status.SUPERSEDED);
        when(comparison.getCalculationFingerprint()).thenReturn("stored-comparison-fingerprint");
        when(selections.findByIdAndAgencyId("selection-1", "agency-1"))
                .thenReturn(Optional.of(selection));
        when(proceedings.findByIdAndAgencyId("proceeding-1", "agency-1"))
                .thenReturn(Optional.of(proceeding));
        when(comparisons.findByIdAndAgencyId("comparison-1", "agency-1"))
                .thenReturn(Optional.of(comparison));

        assertThatThrownBy(() -> service.selection("agency-1", "selection-1", "001", null))
                .isInstanceOf(ApplicationConflictException.class).hasMessageContaining("fingerprint");
    }
}
