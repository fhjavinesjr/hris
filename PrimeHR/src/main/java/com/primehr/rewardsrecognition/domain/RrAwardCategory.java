package com.primehr.rewardsrecognition.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "rr_award_category", uniqueConstraints = @UniqueConstraint(
        name = "uk_rr_category_code", columnNames = {"agency_id", "normalized_code"}))
public class RrAwardCategory extends RrNamedDefinition {
    protected RrAwardCategory() {
    }

    public RrAwardCategory(String agencyId, String code) {
        super(agencyId, code);
    }
}
