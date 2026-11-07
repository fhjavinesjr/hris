package com.primehr.rewardsrecognition.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "rr_governance_policy_version", uniqueConstraints = @UniqueConstraint(
        name = "uk_rr_policy_version", columnNames = {"agency_id", "policy_id", "definition_version"}))
public class RrGovernancePolicyVersion extends RrVersionedDefinition {
    @Column(name = "policy_id", nullable = false, length = 36)
    private String policyId;
    @Column(name = "legal_basis", nullable = false, length = 2000)
    private String legalBasis;
    @Column(name = "policy_reference", nullable = false, length = 250)
    private String policyReference;
    @Column(name = "committee_required", nullable = false)
    private boolean committeeRequired;
    @Column(name = "allows_individual_awards", nullable = false)
    private boolean allowsIndividualAwards;
    @Column(name = "allows_group_awards", nullable = false)
    private boolean allowsGroupAwards;
    @Column(name = "allows_organization_awards", nullable = false)
    private boolean allowsOrganizationAwards;
    @Column(name = "allows_monetary_incentives", nullable = false)
    private boolean allowsMonetaryIncentives;
    @Column(name = "allows_nonmonetary_incentives", nullable = false)
    private boolean allowsNonmonetaryIncentives;
    @Column(name = "requires_agency_head_approval", nullable = false)
    private boolean requiresAgencyHeadApproval;
    @Column(name = "annual_report_required", nullable = false)
    private boolean annualReportRequired;

    protected RrGovernancePolicyVersion() {
    }

    public RrGovernancePolicyVersion(String agencyId, String policyId, int definitionVersion,
                                     String supersedesId, String title, String description,
                                     String legalBasis, String policyReference,
                                     boolean committeeRequired, boolean allowsIndividualAwards,
                                     boolean allowsGroupAwards, boolean allowsOrganizationAwards,
                                     boolean allowsMonetaryIncentives, boolean allowsNonmonetaryIncentives,
                                     boolean requiresAgencyHeadApproval, boolean annualReportRequired) {
        super(agencyId, definitionVersion, supersedesId, title, description);
        this.policyId = requiredText(policyId, "policyId");
        apply(legalBasis, policyReference, committeeRequired, allowsIndividualAwards, allowsGroupAwards,
                allowsOrganizationAwards, allowsMonetaryIncentives, allowsNonmonetaryIncentives,
                requiresAgencyHeadApproval, annualReportRequired);
    }

    public void update(String title, String description, String legalBasis, String policyReference,
                       boolean committeeRequired, boolean allowsIndividualAwards,
                       boolean allowsGroupAwards, boolean allowsOrganizationAwards,
                       boolean allowsMonetaryIncentives, boolean allowsNonmonetaryIncentives,
                       boolean requiresAgencyHeadApproval, boolean annualReportRequired) {
        changeDefinition(title, description);
        apply(legalBasis, policyReference, committeeRequired, allowsIndividualAwards, allowsGroupAwards,
                allowsOrganizationAwards, allowsMonetaryIncentives, allowsNonmonetaryIncentives,
                requiresAgencyHeadApproval, annualReportRequired);
    }

    private void apply(String legalBasis, String policyReference, boolean committeeRequired,
                       boolean allowsIndividualAwards, boolean allowsGroupAwards,
                       boolean allowsOrganizationAwards, boolean allowsMonetaryIncentives,
                       boolean allowsNonmonetaryIncentives, boolean requiresAgencyHeadApproval,
                       boolean annualReportRequired) {
        if (!allowsIndividualAwards && !allowsGroupAwards && !allowsOrganizationAwards) {
            throw new IllegalArgumentException("At least one award nominee type must be allowed");
        }
        if (!allowsMonetaryIncentives && !allowsNonmonetaryIncentives) {
            throw new IllegalArgumentException("At least one incentive type must be allowed");
        }
        this.legalBasis = requiredText(legalBasis, "legalBasis");
        this.policyReference = requiredText(policyReference, "policyReference");
        this.committeeRequired = committeeRequired;
        this.allowsIndividualAwards = allowsIndividualAwards;
        this.allowsGroupAwards = allowsGroupAwards;
        this.allowsOrganizationAwards = allowsOrganizationAwards;
        this.allowsMonetaryIncentives = allowsMonetaryIncentives;
        this.allowsNonmonetaryIncentives = allowsNonmonetaryIncentives;
        this.requiresAgencyHeadApproval = requiresAgencyHeadApproval;
        this.annualReportRequired = annualReportRequired;
    }

    public String getPolicyId() { return policyId; }
    public String getLegalBasis() { return legalBasis; }
    public String getPolicyReference() { return policyReference; }
    public boolean isCommitteeRequired() { return committeeRequired; }
    public boolean isAllowsIndividualAwards() { return allowsIndividualAwards; }
    public boolean isAllowsGroupAwards() { return allowsGroupAwards; }
    public boolean isAllowsOrganizationAwards() { return allowsOrganizationAwards; }
    public boolean isAllowsMonetaryIncentives() { return allowsMonetaryIncentives; }
    public boolean isAllowsNonmonetaryIncentives() { return allowsNonmonetaryIncentives; }
    public boolean isRequiresAgencyHeadApproval() { return requiresAgencyHeadApproval; }
    public boolean isAnnualReportRequired() { return annualReportRequired; }
}
