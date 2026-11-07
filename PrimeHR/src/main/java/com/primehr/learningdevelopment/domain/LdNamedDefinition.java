package com.primehr.learningdevelopment.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.util.Locale;

@MappedSuperclass
public abstract class LdNamedDefinition extends RspAuditedEntity {
    @Column(nullable=false,length=80) private String code;
    @Column(name="normalized_code",nullable=false,length=80) private String normalizedCode;
    protected LdNamedDefinition(){}
    protected LdNamedDefinition(String agency,String code){super(agency);changeCode(code);}
    protected final void changeCode(String value){code=requiredText(value,"code");normalizedCode=code.toUpperCase(Locale.ROOT);}
    public String getCode(){return code;} public String getNormalizedCode(){return normalizedCode;}
}
