package com.primehr.performancemanagement.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.util.Locale;

@Entity @Table(name="spms_policy",uniqueConstraints=@UniqueConstraint(name="uk_spms_policy_code",columnNames={"agency_id","normalized_code"}))
public class PerformancePolicy extends RspAuditedEntity {
 @Column(nullable=false,length=80)private String code;@Column(name="normalized_code",nullable=false,length=80)private String normalizedCode;
 protected PerformancePolicy(){} public PerformancePolicy(String agency,String code){super(agency);changeCode(code);}
 public void changeCode(String value){code=requiredText(value,"code");normalizedCode=code.toUpperCase(Locale.ROOT);}
 public String getCode(){return code;}public String getNormalizedCode(){return normalizedCode;}
}
