package com.primehr.evidence.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.util.Locale;

@Entity
@Table(name="evidence_framework", uniqueConstraints=@UniqueConstraint(name="uk_evidence_framework_code", columnNames={"agency_id","normalized_code"}))
public class EvidenceFramework extends RspAuditedEntity {
    @Column(nullable=false,length=80) private String code;
    @Column(name="normalized_code",nullable=false,length=80) private String normalizedCode;
    protected EvidenceFramework() { }
    public EvidenceFramework(String agencyId,String code){super(agencyId);this.code=requiredText(code,"code");normalizedCode=this.code.toUpperCase(Locale.ROOT);}
    public String getCode(){return code;} public String getNormalizedCode(){return normalizedCode;}
}
