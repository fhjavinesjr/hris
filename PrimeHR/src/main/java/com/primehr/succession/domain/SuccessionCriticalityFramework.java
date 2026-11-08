package com.primehr.succession.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "succession_criticality_framework", uniqueConstraints = @UniqueConstraint(name = "uk_succ_criticality_code", columnNames = {"agency_id", "normalized_code"}))
public class SuccessionCriticalityFramework extends SuccessionNamedDefinition {
    protected SuccessionCriticalityFramework() { }
    public SuccessionCriticalityFramework(String agencyId, String code) { super(agencyId, code); }
}
