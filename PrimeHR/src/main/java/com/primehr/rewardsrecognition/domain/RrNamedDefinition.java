package com.primehr.rewardsrecognition.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.util.Locale;

@MappedSuperclass
public abstract class RrNamedDefinition extends RspAuditedEntity {
    @Column(nullable = false, length = 80)
    private String code;

    @Column(name = "normalized_code", nullable = false, length = 80)
    private String normalizedCode;

    protected RrNamedDefinition() {
    }

    protected RrNamedDefinition(String agencyId, String code) {
        super(agencyId);
        this.code = requiredText(code, "code");
        this.normalizedCode = this.code.toUpperCase(Locale.ROOT);
    }

    public String getCode() { return code; }
    public String getNormalizedCode() { return normalizedCode; }
}
