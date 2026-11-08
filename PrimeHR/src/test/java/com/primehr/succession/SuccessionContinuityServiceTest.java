package com.primehr.succession;

import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.OptimisticConflictException;
import com.primehr.succession.api.SuccessionContinuityDtos.*;
import com.primehr.succession.application.*;
import com.primehr.succession.domain.*;
import com.primehr.succession.infrastructure.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuccessionContinuityServiceTest {
 @Mock SuccessionContinuityPlanRepository plans;@Mock SuccessionBenchCoverageEntryRepository bench;@Mock SuccessionRspReferralPacketRepository referrals;@Mock SuccessionRspReferralEventRepository events;@Mock SuccessionContinuityRiskReviewRepository risks;@Mock SuccessionCycleRepository cycles;@Mock SuccessionCriticalPositionDesignationRepository designations;@Mock SuccessionCommitteeMemberRepository members;@Mock SuccessionCommitteeConflictRepository conflicts;@Mock SuccessionCandidateRepository candidates;@Mock SuccessionReadinessAssessmentRepository assessments;@Mock SuccessionPoolDecisionRepository decisions;@Mock SuccessionPoolMembershipEventRepository memberships;@Mock SuccessionDevelopmentPlanRepository developments;@Mock RspReferralSourcePort rsp;@Mock PrimeHrAuditService audit;@InjectMocks SuccessionContinuityServiceImpl service;

 @Test void staleCriticalPositionSourceBlocksContinuityPlanCreation(){var risk=mock(SuccessionContinuityRiskReview.class);when(risk.getVersion()).thenReturn(0L);when(risk.getCycleId()).thenReturn("cycle");when(risk.getDesignationId()).thenReturn("designation");when(risk.getTargetFingerprint()).thenReturn("old");var cycle=mock(SuccessionCycle.class);var designation=mock(SuccessionCriticalPositionDesignation.class);when(designation.getStatus()).thenReturn(SuccessionCriticalPositionDesignation.Status.APPROVED);when(designation.getSourceFingerprint()).thenReturn("new");when(risks.findByIdAndAgencyId("risk","A")).thenReturn(Optional.of(risk));when(cycles.findByIdAndAgencyId("cycle","A")).thenReturn(Optional.of(cycle));when(designations.findByIdAndAgencyId("designation","A")).thenReturn(Optional.of(designation));var input=new PlanInput(0L,"Functions","Interim","Actions","Triggers",1,LocalDate.now().plusMonths(1));assertThatThrownBy(()->service.createPlan("A","risk",input,"ACTOR",PermissionDataScope.AGENCY_WIDE,"c")).isInstanceOf(OptimisticConflictException.class).hasMessageContaining("exact source");verify(plans,never()).saveAndFlush(any());}

 @Test void mismatchedRspVacancyCannotBeLinkedOrCreateAnApplication(){var plan=mock(SuccessionContinuityPlan.class);when(plan.getVersion()).thenReturn(0L);when(plan.getStatus()).thenReturn(SuccessionContinuityPlan.Status.FINALIZED);when(plan.getCycleId()).thenReturn("cycle");when(plan.getId()).thenReturn("plan");when(plan.getJobPositionId()).thenReturn(10L);when(plan.getPositionName()).thenReturn("Chief");when(plan.getPlantillaId()).thenReturn(20L);var cycle=mock(SuccessionCycle.class);var b=mock(SuccessionBenchCoverageEntry.class);when(b.getId()).thenReturn("bench");when(b.getPoolDecisionId()).thenReturn("decision");when(b.getMembershipEventId()).thenReturn("membership");when(b.getEmployeeNo()).thenReturn("EMP-1");when(b.getEmployeeName()).thenReturn("Employee One");when(b.getReadinessLevelCode()).thenReturn("READY");when(b.getMembershipExpiresOn()).thenReturn(LocalDate.now().plusMonths(1));var current=mock(SuccessionPoolMembershipEvent.class);when(current.getId()).thenReturn("membership");when(current.getResultingStatus()).thenReturn(SuccessionPoolMembershipEvent.ResultingStatus.ACTIVE);when(current.getEffectiveOn()).thenReturn(LocalDate.now().minusDays(1));when(current.getExpiresOn()).thenReturn(LocalDate.now().plusMonths(1));when(plans.findByIdAndAgencyId("plan","A")).thenReturn(Optional.of(plan));when(cycles.findByIdAndAgencyId("cycle","A")).thenReturn(Optional.of(cycle));when(bench.findByIdAndAgencyIdAndContinuityPlanId("bench","A","plan")).thenReturn(Optional.of(b));when(memberships.findFirstByAgencyIdAndPoolDecisionIdOrderByEventSequenceDesc("A","decision")).thenReturn(Optional.of(current));when(rsp.findVacancy("A","publication")).thenReturn(Optional.of(new RspReferralSourcePort.VacancySnapshot("publication","vacancy","PUBLISHED",20L,"Item",99L,"Other",1L,"Office","profile",1,LocalDate.now(),LocalDate.now().plusDays(10),0,"fp",Instant.now())));var input=new ReferralCreate(0L,"bench","Reason","Notice","Restricted","publication");assertThatThrownBy(()->service.createReferral("A","plan",input,"PLANNER",PermissionDataScope.AGENCY_WIDE,"c")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("does not match");verify(referrals,never()).saveAndFlush(any());}

 @Test void referralReceiptRequiresAgencyWideAuthority(){var input=new ReferralTransition(0L,"Received","Restricted");assertThatThrownBy(()->service.receiveReferral("A","ref",input,"RSP",PermissionDataScope.ASSIGNED_RECORDS,"c")).isInstanceOf(AccessDeniedException.class).hasMessageContaining("Agency Wide");verifyNoInteractions(referrals);}
}
