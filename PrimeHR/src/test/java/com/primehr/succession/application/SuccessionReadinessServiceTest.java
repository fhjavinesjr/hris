package com.primehr.succession.application;

import com.primehr.gap.application.TargetCompetencyGapService;
import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.integration.humanresource.HumanResourceAssessmentSubjectClient;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.succession.api.SuccessionReadinessDtos.*;
import com.primehr.succession.domain.*;
import com.primehr.succession.infrastructure.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuccessionReadinessServiceTest {
    @Mock SuccessionReadinessAssessmentRepository assessments; @Mock SuccessionReadinessGapItemRepository gaps;
    @Mock SuccessionReadinessPanelAssignmentRepository assignments; @Mock SuccessionReadinessCriterionFindingRepository findings;
    @Mock SuccessionPoolDecisionRepository decisions; @Mock SuccessionCandidateRepository candidates;
    @Mock SuccessionCycleRepository cycles; @Mock SuccessionGovernancePolicyVersionRepository policies;
    @Mock SuccessionCommitteeCharterVersionRepository charters; @Mock SuccessionCommitteeMemberRepository members;
    @Mock SuccessionCommitteeConflictRepository standingConflicts; @Mock SuccessionReadinessScaleVersionRepository scales;
    @Mock SuccessionReadinessLevelRepository levels; @Mock TargetCompetencyGapService targetGaps;
    @Mock HumanResourceAssessmentSubjectClient hr; @Mock PrimeHrAuditService audit;
    @InjectMocks SuccessionReadinessServiceImpl service;

    @Test
    void candidateWithAgencyWidePermissionStillReceivesOwnSafeResponse() {
        var assessment=mock(SuccessionReadinessAssessment.class);var candidate=mock(SuccessionCandidate.class);
        var finding=mock(SuccessionReadinessCriterionFinding.class);
        var decision=mock(SuccessionPoolDecision.class);
        when(assessments.findByIdAndAgencyId("review","A")).thenReturn(Optional.of(assessment));
        when(assessment.getId()).thenReturn("review");when(assessment.getCandidateId()).thenReturn("candidate");
        when(assessment.getCycleId()).thenReturn("cycle");when(assessment.getStatus()).thenReturn(SuccessionReadinessAssessment.Status.FINALIZED);
        when(assessment.getGapCalculatedAt()).thenReturn(Instant.now());
        when(candidates.findByIdAndAgencyId("candidate","A")).thenReturn(Optional.of(candidate));
        when(candidate.getEmployeeNo()).thenReturn("EMP-1");when(candidate.getEmployeeName()).thenReturn("Employee One");
        when(candidate.getTargetPositionName()).thenReturn("Target Position");
        when(findings.findByAgencyIdAndAssessmentIdOrderByDisplayOrder("A","review")).thenReturn(List.of(finding));
        when(finding.isEmployeeVisible()).thenReturn(false);
        when(decisions.findByAgencyIdAndAssessmentId("A","review")).thenReturn(Optional.of(decision));
        when(decision.getOutcome()).thenReturn(SuccessionPoolDecision.Outcome.NOT_INCLUDED);
        when(decision.getDecidedAt()).thenReturn(Instant.now());
        var response=service.get("A","review","EMP-1",PermissionDataScope.AGENCY_WIDE);
        assertThat(response.assignments()).isEmpty();assertThat(response.findings()).isEmpty();
        assertThat(response.decision().restrictedRationale()).isNull();assertThat(response.decision().decidedBy()).isNull();
    }

    @Test
    void candidateCannotRecordPanelFindingsEvenWithAgencyWidePermission() {
        var assessment=mock(SuccessionReadinessAssessment.class);var candidate=mock(SuccessionCandidate.class);
        when(assessments.findByIdAndAgencyId("review","A")).thenReturn(Optional.of(assessment));
        when(assessment.getCandidateId()).thenReturn("candidate");
        when(candidates.findByIdAndAgencyId("candidate","A")).thenReturn(Optional.of(candidate));
        when(candidate.getEmployeeNo()).thenReturn("EMP-1");
        var input=new FindingsInput(0L,List.of(new FindingInput("C1","Criterion",
                SuccessionReadinessCriterionFinding.Outcome.MET,"Rationale","Evidence",true,1)));
        assertThatThrownBy(()->service.findings("A","review",input,"EMP-1",PermissionDataScope.AGENCY_WIDE,null))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(findings);
    }
}
