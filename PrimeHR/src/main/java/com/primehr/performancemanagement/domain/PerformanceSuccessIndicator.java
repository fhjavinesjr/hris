package com.primehr.performancemanagement.domain;
import com.primehr.rsp.domain.RspAuditedEntity;import jakarta.persistence.*;import java.util.Locale;
@Entity @Table(name="spms_success_indicator",uniqueConstraints=@UniqueConstraint(name="uk_spms_success_indicator_code",columnNames={"agency_id","normalized_code"}))
public class PerformanceSuccessIndicator extends RspAuditedEntity{@Column(nullable=false,length=80)private String code;@Column(name="normalized_code",nullable=false,length=80)private String normalizedCode;protected PerformanceSuccessIndicator(){}public PerformanceSuccessIndicator(String a,String c){super(a);code=requiredText(c,"code");normalizedCode=code.toUpperCase(Locale.ROOT);}public String getCode(){return code;}}
