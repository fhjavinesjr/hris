package com.primehr.learningdevelopment.domain;
import jakarta.persistence.*;
@Entity @Table(name="ld_governance_policy",uniqueConstraints=@UniqueConstraint(name="uk_ld_policy_code",columnNames={"agency_id","normalized_code"}))
public class LdGovernancePolicy extends LdNamedDefinition {protected LdGovernancePolicy(){} public LdGovernancePolicy(String agency,String code){super(agency,code);}}
