package com.primehr.succession.domain;

import jakarta.persistence.*;

@Entity @Table(name = "succession_governance_policy_version", uniqueConstraints = @UniqueConstraint(name = "uk_succ_policy_version", columnNames = {"agency_id", "policy_id", "definition_version"}))
public class SuccessionGovernancePolicyVersion extends SuccessionVersionedDefinition {
    @Column(name="policy_id",nullable=false,length=36) private String policyId;
    @Column(name="legal_basis",nullable=false,length=2000) private String legalBasis;
    @Column(name="policy_reference",nullable=false,length=250) private String policyReference;
    @Column(name="privacy_notice",nullable=false,length=4000) private String privacyNotice;
    @Column(name="eligibility_guidance",nullable=false,length=4000) private String eligibilityGuidance;
    @Column(name="evidence_requirements",nullable=false,length=4000) private String evidenceRequirements;
    @Column(name="review_interval_months",nullable=false) private int reviewIntervalMonths;
    @Column(name="membership_validity_months",nullable=false) private int membershipValidityMonths;
    @Column(name="minimum_aggregate_cohort",nullable=false) private int minimumAggregateCohort;
    @Column(name="requires_employee_notice",nullable=false) private boolean requiresEmployeeNotice;
    @Column(name="requires_employee_consent",nullable=false) private boolean requiresEmployeeConsent;
    @Column(name="allows_self_expression",nullable=false) private boolean allowsSelfExpression;
    @Column(name="requires_committee_review",nullable=false) private boolean requiresCommitteeReview;
    @Column(name="requires_agency_head_decision",nullable=false) private boolean requiresAgencyHeadDecision;
    protected SuccessionGovernancePolicyVersion() { }
    public SuccessionGovernancePolicyVersion(String agencyId,String policyId,int version,String supersedes,String title,String description,String legalBasis,String policyReference,String privacyNotice,String eligibilityGuidance,String evidenceRequirements,int reviewMonths,int validityMonths,int minCohort,boolean notice,boolean consent,boolean selfExpression,boolean committee,boolean agencyHead){super(agencyId,version,supersedes,title,description);this.policyId=requiredText(policyId,"policyId");apply(legalBasis,policyReference,privacyNotice,eligibilityGuidance,evidenceRequirements,reviewMonths,validityMonths,minCohort,notice,consent,selfExpression,committee,agencyHead);}
    public void update(String title,String description,String legalBasis,String policyReference,String privacyNotice,String eligibilityGuidance,String evidenceRequirements,int reviewMonths,int validityMonths,int minCohort,boolean notice,boolean consent,boolean selfExpression,boolean committee,boolean agencyHead){changeDefinition(title,description);apply(legalBasis,policyReference,privacyNotice,eligibilityGuidance,evidenceRequirements,reviewMonths,validityMonths,minCohort,notice,consent,selfExpression,committee,agencyHead);}
    private void apply(String legalBasis,String policyReference,String privacyNotice,String eligibilityGuidance,String evidenceRequirements,int reviewMonths,int validityMonths,int minCohort,boolean notice,boolean consent,boolean selfExpression,boolean committee,boolean agencyHead){if(reviewMonths<1||validityMonths<1)throw new IllegalArgumentException("Review and membership periods must be positive");if(minCohort<3)throw new IllegalArgumentException("minimumAggregateCohort must be at least 3");this.legalBasis=requiredText(legalBasis,"legalBasis");this.policyReference=requiredText(policyReference,"policyReference");this.privacyNotice=requiredText(privacyNotice,"privacyNotice");this.eligibilityGuidance=requiredText(eligibilityGuidance,"eligibilityGuidance");this.evidenceRequirements=requiredText(evidenceRequirements,"evidenceRequirements");this.reviewIntervalMonths=reviewMonths;this.membershipValidityMonths=validityMonths;this.minimumAggregateCohort=minCohort;this.requiresEmployeeNotice=notice;this.requiresEmployeeConsent=consent;this.allowsSelfExpression=selfExpression;this.requiresCommitteeReview=committee;this.requiresAgencyHeadDecision=agencyHead;}
    public String getPolicyId(){return policyId;} public String getLegalBasis(){return legalBasis;} public String getPolicyReference(){return policyReference;} public String getPrivacyNotice(){return privacyNotice;} public String getEligibilityGuidance(){return eligibilityGuidance;} public String getEvidenceRequirements(){return evidenceRequirements;} public int getReviewIntervalMonths(){return reviewIntervalMonths;} public int getMembershipValidityMonths(){return membershipValidityMonths;} public int getMinimumAggregateCohort(){return minimumAggregateCohort;} public boolean isRequiresEmployeeNotice(){return requiresEmployeeNotice;} public boolean isRequiresEmployeeConsent(){return requiresEmployeeConsent;} public boolean isAllowsSelfExpression(){return allowsSelfExpression;} public boolean isRequiresCommitteeReview(){return requiresCommitteeReview;} public boolean isRequiresAgencyHeadDecision(){return requiresAgencyHeadDecision;}
}
