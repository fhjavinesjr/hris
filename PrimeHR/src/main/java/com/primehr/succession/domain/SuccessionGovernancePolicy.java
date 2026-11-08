package com.primehr.succession.domain;

import jakarta.persistence.*;

@Entity @Table(name = "succession_governance_policy", uniqueConstraints = @UniqueConstraint(name = "uk_succ_policy_code", columnNames = {"agency_id", "normalized_code"}))
public class SuccessionGovernancePolicy extends SuccessionNamedDefinition {
    protected SuccessionGovernancePolicy() { }
    public SuccessionGovernancePolicy(String agencyId, String code) { super(agencyId, code); }
}
