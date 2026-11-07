package com.primehr.succession.domain;
import jakarta.persistence.*;
@Entity @Table(name="succession_readiness_scale",uniqueConstraints=@UniqueConstraint(name="uk_succ_scale_code",columnNames={"agency_id","normalized_code"}))
public class SuccessionReadinessScale extends SuccessionNamedDefinition { protected SuccessionReadinessScale(){} public SuccessionReadinessScale(String agencyId,String code){super(agencyId,code);} }
