package com.primehr.succession.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.util.Locale;

@MappedSuperclass
public abstract class SuccessionNamedDefinition extends RspAuditedEntity {
    @Column(nullable = false, length = 80) private String code;
    @Column(name = "normalized_code", nullable = false, length = 80) private String normalizedCode;

    protected SuccessionNamedDefinition() { }
    protected SuccessionNamedDefinition(String agencyId, String code) {
        super(agencyId);
        this.code = requiredText(code, "code");
        this.normalizedCode = this.code.toUpperCase(Locale.ROOT);
    }
    public String getCode() { return code; }
    public String getNormalizedCode() { return normalizedCode; }
}
