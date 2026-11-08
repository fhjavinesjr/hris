package com.primehr.rewardsrecognition.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "rr_evaluation_case", uniqueConstraints = @UniqueConstraint(name = "uk_rr_evaluation_nomination", columnNames = {"agency_id", "nomination_id"}))
public class RrEvaluationCase extends RspAuditedEntity {
    public enum Status { ELIGIBILITY_REVIEW, ELIGIBLE, DISQUALIFIED, RETURNED, EVALUATION, DELIBERATION, RECOMMENDED, NOT_RECOMMENDED, FINAL_APPROVED, FINAL_DISAPPROVED, RECONSIDERATION_PENDING }
    @Column(name="nomination_id",nullable=false,length=36) private String nominationId;
    @Column(name="cycle_id",nullable=false,length=36) private String cycleId;
    @Column(name="category_version_id",nullable=false,length=36) private String categoryVersionId;
    @Column(name="charter_version_id",nullable=false,length=36) private String charterVersionId;
    @Column(name="charter_definition_version",nullable=false) private int charterDefinitionVersion;
    @Column(name="charter_fingerprint",nullable=false,length=64) private String charterFingerprint;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=40) private Status status;
    @Column(name="eligibility_summary",length=4000) private String eligibilitySummary;
    @Column(name="eligibility_decided_by",length=100) private String eligibilityDecidedBy;
    @Column(name="eligibility_decided_at") private Instant eligibilityDecidedAt;
    @Column(name="calculated_score",precision=10,scale=4) private BigDecimal calculatedScore;
    @Column(name="eligible_voters") private Integer eligibleVoters;
    @Column(name="attending_voters") private Integer attendingVoters;
    @Column(name="quorum_met") private Boolean quorumMet;
    protected RrEvaluationCase() {}
    public RrEvaluationCase(String agency,String nomination,String cycle,String category,String charter,int charterVersion,String fingerprint){super(agency);nominationId=requiredText(nomination,"nominationId");cycleId=requiredText(cycle,"cycleId");categoryVersionId=requiredText(category,"categoryVersionId");charterVersionId=requiredText(charter,"charterVersionId");if(charterVersion<1)throw new IllegalArgumentException("charterDefinitionVersion must be positive");charterDefinitionVersion=charterVersion;charterFingerprint=requiredText(fingerprint,"charterFingerprint");status=Status.ELIGIBILITY_REVIEW;}
    public void decideEligibility(Status decision,String summary,String actor,Instant at){if(status!=Status.ELIGIBILITY_REVIEW&&status!=Status.RETURNED)throw new IllegalLifecycleTransitionException("Eligibility may only be decided during eligibility review");if(decision!=Status.ELIGIBLE&&decision!=Status.DISQUALIFIED&&decision!=Status.RETURNED)throw new IllegalArgumentException("Eligibility decision must be ELIGIBLE, DISQUALIFIED, or RETURNED");status=decision;eligibilitySummary=requiredText(summary,"summary");eligibilityDecidedBy=requiredText(actor,"actor");eligibilityDecidedAt=at;}
    public void beginEvaluation(){if(status!=Status.ELIGIBLE)throw new IllegalLifecycleTransitionException("Only an eligible nomination may enter evaluation");status=Status.EVALUATION;}
    public void beginDeliberation(BigDecimal score){if(status!=Status.EVALUATION)throw new IllegalLifecycleTransitionException("Only an evaluated nomination may enter deliberation");calculatedScore=score;status=Status.DELIBERATION;}
    public void recommend(boolean recommended,int eligible,int attending,boolean quorum){if(status!=Status.DELIBERATION)throw new IllegalLifecycleTransitionException("Recommendation requires deliberation");eligibleVoters=eligible;attendingVoters=attending;quorumMet=quorum;if(!quorum)throw new IllegalStateException("Committee quorum is not met");status=recommended?Status.RECOMMENDED:Status.NOT_RECOMMENDED;}
    public void closeDeliberation(Status outcome,int eligible,int attending,boolean quorum){if(status!=Status.DELIBERATION)throw new IllegalLifecycleTransitionException("Deliberation is not active");if(outcome!=Status.RETURNED&&outcome!=Status.DISQUALIFIED)throw new IllegalArgumentException("Outcome must be RETURNED or DISQUALIFIED");eligibleVoters=eligible;attendingVoters=attending;quorumMet=quorum;if(!quorum)throw new IllegalStateException("Committee quorum is not met");status=outcome;}
    public void finalDecision(boolean approved){if(status!=Status.RECOMMENDED&&status!=Status.NOT_RECOMMENDED)throw new IllegalLifecycleTransitionException("Final decision requires a committee recommendation");status=approved?Status.FINAL_APPROVED:Status.FINAL_DISAPPROVED;}
    public void requestReconsideration(){if(status!=Status.FINAL_APPROVED&&status!=Status.FINAL_DISAPPROVED)throw new IllegalLifecycleTransitionException("Only a final decision may be reconsidered");status=Status.RECONSIDERATION_PENDING;}
    public void resolveReconsideration(boolean approved){if(status!=Status.RECONSIDERATION_PENDING)throw new IllegalLifecycleTransitionException("No reconsideration is pending");status=approved?Status.FINAL_APPROVED:Status.FINAL_DISAPPROVED;}
    public String getNominationId(){return nominationId;} public String getCycleId(){return cycleId;} public String getCategoryVersionId(){return categoryVersionId;} public String getCharterVersionId(){return charterVersionId;} public int getCharterDefinitionVersion(){return charterDefinitionVersion;} public String getCharterFingerprint(){return charterFingerprint;} public Status getStatus(){return status;} public String getEligibilitySummary(){return eligibilitySummary;} public String getEligibilityDecidedBy(){return eligibilityDecidedBy;} public Instant getEligibilityDecidedAt(){return eligibilityDecidedAt;} public BigDecimal getCalculatedScore(){return calculatedScore;} public Integer getEligibleVoters(){return eligibleVoters;} public Integer getAttendingVoters(){return attendingVoters;} public Boolean getQuorumMet(){return quorumMet;}
}
