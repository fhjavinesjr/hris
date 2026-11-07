package com.primehr.succession.domain; import jakarta.persistence.*;
@Entity @Table(name="succession_career_family",uniqueConstraints=@UniqueConstraint(name="uk_succ_family_code",columnNames={"agency_id","normalized_code"}))
public class SuccessionCareerFamily extends SuccessionNamedDefinition {protected SuccessionCareerFamily(){} public SuccessionCareerFamily(String agencyId,String code){super(agencyId,code);}}
