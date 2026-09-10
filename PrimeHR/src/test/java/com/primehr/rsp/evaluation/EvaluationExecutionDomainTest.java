package com.primehr.rsp.evaluation;

import com.primehr.rsp.evaluation.domain.*;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import org.junit.jupiter.api.Test;

import java.math.*;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class EvaluationExecutionDomainTest {
    private static final String AGENCY="AGENCY";
    private static final Instant NOW=Instant.parse("2026-09-01T00:00:00Z");

    @Test void stageResultRequiresIndependentValidationAndPreservesReturnedCorrectionTrail(){
        StageResult result=new StageResult(AGENCY,"p","s","c","stage",1,null,true,"examiner");
        result.recordScore(new BigDecimal("42"),new BigDecimal("50"),2,RoundingMode.HALF_UP,"initial");
        assertThat(result.getNormalizedScore()).isEqualByComparingTo("84.00");
        result.submit("examiner",NOW);
        assertThatThrownBy(()->result.validate("examiner",NOW)).isInstanceOf(IllegalLifecycleTransitionException.class);
        result.returnForCorrection("validator","Recheck item 3",NOW);
        assertThat(result.getStatus()).isEqualTo(StageResult.Status.RETURNED);
        assertThat(result.getReturnReason()).isEqualTo("Recheck item 3");
        result.recordScore(new BigDecimal("45"),new BigDecimal("50"),2,RoundingMode.HALF_UP,"corrected");
        result.submit("examiner",NOW);
        result.validate("validator",NOW);
        assertThat(result.getStatus()).isEqualTo(StageResult.Status.VALIDATED);
    }

    @Test void panelRatingIsMemberOwnedAndCanOnlyBeReturnedByAnotherActor(){
        PanelRating rating=new PanelRating(AGENCY,"p","c","stage","member-1",1,null);
        rating.calculate(new BigDecimal("87.50"),"independent rating");
        assertThatThrownBy(()->rating.submit("member-2",NOW)).isInstanceOf(IllegalLifecycleTransitionException.class);
        rating.submit("member-1",NOW);
        assertThatThrownBy(()->rating.returnForCorrection("member-1","self return",NOW)).isInstanceOf(IllegalLifecycleTransitionException.class);
        rating.returnForCorrection("chair","Clarify behavioral evidence",NOW);
        assertThat(rating.getStatus()).isEqualTo(PanelRating.Status.RETURNED);
    }

    @Test void unresolvedOrRecusedConflictBlocksCandidateActionAndCannotBeSelfResolved(){
        ConflictDeclaration conflict=new ConflictDeclaration(AGENCY,"p","c","member-1",ConflictDeclaration.Outcome.POTENTIAL_CONFLICT_REVIEW_REQUIRED,"Prior reporting relationship",NOW);
        assertThat(conflict.blocksAction()).isTrue();
        assertThatThrownBy(()->conflict.resolve(ConflictDeclaration.Resolution.CLEARED,"member-1","self",NOW)).isInstanceOf(IllegalArgumentException.class);
        conflict.resolve(ConflictDeclaration.Resolution.CLEARED,"chair","No current supervisory relationship",NOW);
        assertThat(conflict.blocksAction()).isFalse();
    }

    @Test void referenceCheckSeparatesApplicantSafeTextAndConfidentialNotesWithValidatorSod(){
        ReferenceCheck check=new ReferenceCheck(AGENCY,"p","c","stage","EMPLOYMENT","FORMER_SUPERVISOR","Consent-17","checker",NOW);
        check.complete(ReferenceCheck.Outcome.SATISFACTORY,"Reference check completed","Restricted source notes","checker",NOW);
        assertThatThrownBy(()->check.validate("checker",NOW)).isInstanceOf(IllegalLifecycleTransitionException.class);
        check.validate("validator",NOW);
        assertThat(check.getApplicantSafeText()).isEqualTo("Reference check completed");
        assertThat(check.getConfidentialNotes()).isEqualTo("Restricted source notes");
        assertThat(check.getStatus()).isEqualTo(ReferenceCheck.Status.VALIDATED);
    }

    @Test void sessionTracksNoShowAndRequiresReasonForCancellation(){
        EvaluationSession session=new EvaluationSession(AGENCY,"p","stage",1,NOW,NOW.plusSeconds(3600),"Asia/Manila",EvaluationSession.DeliveryMode.ONSITE,"Room 1","Bring ID",10,"secretariat");
        session.schedule("secretariat",NOW);
        EvaluationSessionCandidate candidate=new EvaluationSessionCandidate(AGENCY,"session","candidate");
        candidate.invite(NOW);candidate.attendance(EvaluationSessionCandidate.Attendance.NO_SHOW,"secretariat","Did not appear",NOW);
        assertThat(candidate.getAttendanceStatus()).isEqualTo(EvaluationSessionCandidate.Attendance.NO_SHOW);
        assertThatThrownBy(()->session.cancel("secretariat"," ",NOW)).isInstanceOf(IllegalArgumentException.class);
        session.cancel("secretariat","Session invalidated by venue closure",NOW);
        assertThat(session.getStatus()).isEqualTo(EvaluationSession.Status.CANCELLED);
    }

    @Test void meetingFinalizationRequiresMinutesAndResolutionVotesAreNonNegative(){
        HrmpsbMeeting meeting=new HrmpsbMeeting(AGENCY,"p",1,NOW,"Asia/Manila","Board room","Deliberation",3);
        meeting.schedule();meeting.begin();
        assertThatThrownBy(()->meeting.finalizeMeeting("chair","Complete",NOW)).isInstanceOf(IllegalLifecycleTransitionException.class);
        meeting.update(NOW,"Asia/Manila","Board room","Deliberation",3,"Quorum confirmed; evidence deliberated.");
        meeting.finalizeMeeting("chair","Approved minutes",NOW);
        assertThat(meeting.getStatus()).isEqualTo(HrmpsbMeeting.Status.FINALIZED);
        assertThatThrownBy(()->new HrmpsbResolution(AGENCY,"m","c",HrmpsbResolution.Recommendation.NOT_ENDORSED,"Evidence did not meet policy",HrmpsbResolution.Method.VOTE,-1,2,0,"chair",NOW)).isInstanceOf(IllegalArgumentException.class);
    }
}
