package com.primehr.performancemanagement.domain;
import com.primehr.rsp.domain.RspAuditedEntity;import jakarta.persistence.*;import java.util.*;
@Entity@Table(name="spms_objective",uniqueConstraints=@UniqueConstraint(name="uk_spms_objective_code",columnNames={"agency_id","normalized_code"}))
public class PerformanceObjective extends RspAuditedEntity{@Column(nullable=false,length=80)private String code;@Column(name="normalized_code",nullable=false,length=80)private String normalizedCode;protected PerformanceObjective(){}public PerformanceObjective(String a,String c){super(a);code=requiredText(c,"code");normalizedCode=code.toUpperCase(Locale.ROOT);}public String getCode(){return code;}}
