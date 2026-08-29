package com.primehr.learning.referral.application;

import com.primehr.gap.domain.*;
import com.primehr.gap.infrastructure.*;
import com.primehr.learning.referral.api.LdReferralDtos.*;
import com.primehr.learning.referral.domain.*;
import com.primehr.learning.referral.infrastructure.*;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LdReferralServiceImplTest {
    @Mock LdReferralRepository referrals; @Mock LdReferralItemRepository referralItems;
    @Mock CompetencyGapAnalysisRepository analyses; @Mock CompetencyGapItemRepository gapItems;
    @Mock PrimeHrAuditService audit; @InjectMocks LdReferralServiceImpl service;

    @Test void staleUpdateIsRejectedBeforeMutation() {
        LdReferral referral=referral(); when(referrals.findByIdAndAgencyId("r1","AGENCY")).thenReturn(Optional.of(referral));
        assertThatThrownBy(()->service.update("AGENCY","r1",new UpdateRequest("Need","Action",null,null,null,4L),null))
                .isInstanceOf(OptimisticConflictException.class);
        verify(referrals,never()).saveAndFlush(any());
    }
    @Test void duplicateActiveGapClaimIsRejectedTransactionally() {
        LdReferral referral=referral(); CompetencyGapItem gap=gap(referral.getAnalysis(),GapClassification.BELOW);
        when(referrals.findByIdAndAgencyId("r1","AGENCY")).thenReturn(Optional.of(referral));
        when(analyses.findByIdAndAgencyIdForUpdate("analysis-1","AGENCY")).thenReturn(Optional.of(referral.getAnalysis()));
        when(gapItems.findById("gap-1")).thenReturn(Optional.of(gap));
        when(referralItems.findByReferralIdAndGapItemId("r1","gap-1")).thenReturn(Optional.empty());
        when(referralItems.existsActiveClaim("AGENCY","analysis-1","gap-1")).thenReturn(true);
        assertThatThrownBy(()->service.addItems("AGENCY","r1",new AddItemsRequest(List.of("gap-1"),0L),null))
                .isInstanceOf(OptimisticConflictException.class).hasMessageContaining("active referral");
        verify(referralItems,never()).save(any());
    }
    @Test void nonActionableGapCannotBeSelected() {
        LdReferral referral=referral(); CompetencyGapItem gap=gap(referral.getAnalysis(),GapClassification.MEETS);
        when(referrals.findByIdAndAgencyId("r1","AGENCY")).thenReturn(Optional.of(referral));
        when(analyses.findByIdAndAgencyIdForUpdate("analysis-1","AGENCY")).thenReturn(Optional.of(referral.getAnalysis()));
        when(gapItems.findById("gap-1")).thenReturn(Optional.of(gap));
        assertThatThrownBy(()->service.addItems("AGENCY","r1",new AddItemsRequest(List.of("gap-1"),0L),null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Only BELOW or NOT_ASSESSED");
    }
    @Test void submitRequiresAnActiveItemAndCreatesNoDownstreamSideEffect() {
        LdReferral referral=referral(); when(referrals.findByIdAndAgencyId("r1","AGENCY")).thenReturn(Optional.of(referral));
        when(analyses.findByIdAndAgencyIdForUpdate("analysis-1","AGENCY")).thenReturn(Optional.of(referral.getAnalysis()));
        when(referralItems.findByReferralIdOrderByDisplayOrderAscCompetencyCodeAsc("r1")).thenReturn(List.of());
        assertThatThrownBy(()->service.submit("AGENCY","r1",new TransitionRequest(0L,"manual"),null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("At least one");
        verify(referrals,never()).saveAndFlush(any());
    }
    @Test void submitIsAtomicAuditedAndReferredRecordIsImmutable() {
        LdReferral referral=referral(); CompetencyGapItem gap=gap(referral.getAnalysis(),GapClassification.NOT_ASSESSED);
        LdReferralItem selected=new LdReferralItem("AGENCY",referral,gap);
        when(referrals.findByIdAndAgencyId("r1","AGENCY")).thenReturn(Optional.of(referral));
        when(analyses.findByIdAndAgencyIdForUpdate("analysis-1","AGENCY")).thenReturn(Optional.of(referral.getAnalysis()));
        when(referralItems.findByReferralIdOrderByDisplayOrderAscCompetencyCodeAsc("r1")).thenReturn(List.of(selected));
        when(referralItems.countActiveClaims("AGENCY","analysis-1","gap-1")).thenReturn(1L);
        when(audit.currentActor()).thenReturn("hr-officer"); when(referrals.saveAndFlush(referral)).thenReturn(referral);
        Response result=service.submit("AGENCY","r1",new TransitionRequest(0L,"manual referral"),"corr-1");
        assertThat(result.status()).isEqualTo(LdReferralStatus.REFERRED);
        assertThatThrownBy(()->referral.update("Changed","Changed",null,null,null)).isInstanceOf(IllegalLifecycleTransitionException.class);
        verify(audit).record(eq("AGENCY"),eq("SUBMIT_REFERRAL"),eq("LD_REFERRAL"),eq("r1"),eq(1),anyLong(),any(),any(),eq("manual referral"),eq("corr-1"));
        verify(analyses).findByIdAndAgencyIdForUpdate("analysis-1","AGENCY");
        verifyNoInteractions(gapItems);
    }
    private static LdReferral referral(){
        LdReferral r=new LdReferral("AGENCY",analysis(),"Development need","Coaching",null,null,null);
        set(r,"id","r1"); return r;
    }
    private static CompetencyGapAnalysis analysis(){
        CompetencyGapAnalysis a=mock(CompetencyGapAnalysis.class); lenient().when(a.getId()).thenReturn("analysis-1"); when(a.getAgencyId()).thenReturn("AGENCY");
        when(a.getSubjectEmployeeId()).thenReturn(1L); when(a.getSubjectEmployeeNo()).thenReturn("001"); when(a.getSubjectDisplayName()).thenReturn("Ferdinand");
        lenient().when(a.getAnalysisDate()).thenReturn(LocalDate.of(2026,8,28)); lenient().when(a.getSourceJobPositionName()).thenReturn("Accountant"); return a;
    }
    private static CompetencyGapItem gap(CompetencyGapAnalysis a,GapClassification c){
        CompetencyGapItem i=mock(CompetencyGapItem.class); lenient().when(i.getId()).thenReturn("gap-1"); lenient().when(i.getAnalysis()).thenReturn(a);
        lenient().when(i.getGapClassification()).thenReturn(c); lenient().when(i.getCompetencyCode()).thenReturn("COMM"); lenient().when(i.getCompetencyName()).thenReturn("Communication");
        lenient().when(i.getNotAssessedReason()).thenReturn(c==GapClassification.NOT_ASSESSED?NotAssessedReason.NO_RESULT:null);
        lenient().when(i.getGapValue()).thenReturn(c==GapClassification.BELOW?1:null); return i;
    }
    private static void set(Object target,String field,Object value){try{var f=target.getClass().getDeclaredField(field);f.setAccessible(true);f.set(target,value);}catch(Exception e){throw new AssertionError(e);}}
}
