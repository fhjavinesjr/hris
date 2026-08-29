package com.primehr.learning.referral.domain;

import com.primehr.gap.domain.*;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LdReferralDomainTest {
    @Test void draftSnapshotsAnalysisAndBecomesImmutableAfterSubmission() {
        CompetencyGapAnalysis analysis=analysis();
        LdReferral referral=new LdReferral("AGENCY",analysis,"Improve communication","Coaching",
                LocalDate.of(2027,1,31),"Priority gap",null);
        assertThat(referral.getStatus()).isEqualTo(LdReferralStatus.DRAFT);
        assertThat(referral.getSubjectEmployeeNo()).isEqualTo("001");
        referral.submit("hr-officer",Instant.parse("2026-08-28T12:00:00Z"));
        assertThat(referral.getStatus()).isEqualTo(LdReferralStatus.REFERRED);
        assertThatThrownBy(()->referral.update("Changed","Training",null,null,null))
                .isInstanceOf(IllegalLifecycleTransitionException.class);
        assertThatThrownBy(referral::archiveDraft).isInstanceOf(IllegalLifecycleTransitionException.class);
    }
    @Test void referralItemAcceptsOnlyActionableGapResults() {
        LdReferral referral=new LdReferral("AGENCY",analysis(),"Need","Action",null,null,null);
        CompetencyGapItem below=gap(referral.getAnalysis(),GapClassification.BELOW);
        assertThat(new LdReferralItem("AGENCY",referral,below).getGapClassification()).isEqualTo(GapClassification.BELOW);
        assertThatThrownBy(()->new LdReferralItem("AGENCY",referral,gap(referral.getAnalysis(),GapClassification.MEETS)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Only BELOW or NOT_ASSESSED");
    }
    private static CompetencyGapAnalysis analysis(){
        CompetencyGapAnalysis a=mock(CompetencyGapAnalysis.class); when(a.getId()).thenReturn("analysis-1");
        when(a.getAgencyId()).thenReturn("AGENCY"); when(a.getSubjectEmployeeId()).thenReturn(1L);
        when(a.getSubjectEmployeeNo()).thenReturn("001"); when(a.getSubjectDisplayName()).thenReturn("Ferdinand Javines");
        when(a.getAnalysisDate()).thenReturn(LocalDate.of(2026,8,28)); when(a.getSourceJobPositionName()).thenReturn("Accountant"); return a;
    }
    private static CompetencyGapItem gap(CompetencyGapAnalysis a,GapClassification c){
        CompetencyGapItem i=mock(CompetencyGapItem.class); when(i.getAnalysis()).thenReturn(a); when(i.getGapClassification()).thenReturn(c);
        when(i.getCompetencyCode()).thenReturn("COMM"); when(i.getCompetencyName()).thenReturn("Communication");
        when(i.getGapValue()).thenReturn(c==GapClassification.BELOW?1:0); return i;
    }
}

