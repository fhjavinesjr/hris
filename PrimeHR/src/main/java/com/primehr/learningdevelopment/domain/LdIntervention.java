package com.primehr.learningdevelopment.domain;
import jakarta.persistence.*;
@Entity @Table(name="ld_intervention",uniqueConstraints=@UniqueConstraint(name="uk_ld_intervention_code",columnNames={"agency_id","normalized_code"}))
public class LdIntervention extends LdNamedDefinition {protected LdIntervention(){}public LdIntervention(String agency,String code){super(agency,code);}}
