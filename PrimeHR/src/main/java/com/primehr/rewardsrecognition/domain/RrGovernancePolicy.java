package com.primehr.rewardsrecognition.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "rr_governance_policy", uniqueConstraints = @UniqueConstraint(
        name = "uk_rr_policy_code", columnNames = {"agency_id", "normalized_code"}))
public class RrGovernancePolicy extends RrNamedDefinition {
    protected RrGovernancePolicy() {
    }

    public RrGovernancePolicy(String agencyId, String code) {
        super(agencyId, code);
    }
}
