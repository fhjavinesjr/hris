package com.primehr.rewardsrecognition.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "rr_committee_charter", uniqueConstraints = @UniqueConstraint(
        name = "uk_rr_charter_code", columnNames = {"agency_id", "normalized_code"}))
public class RrCommitteeCharter extends RrNamedDefinition {
    protected RrCommitteeCharter() {
    }

    public RrCommitteeCharter(String agencyId, String code) {
        super(agencyId, code);
    }
}
