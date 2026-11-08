package com.primehr.learningdevelopment.domain;
import jakarta.persistence.*;
@Entity @Table(name="ld_evaluation_framework",uniqueConstraints=@UniqueConstraint(name="uk_ld_evaluation_framework_code",columnNames={"agency_id","normalized_code"}))
public class LdEvaluationFramework extends LdNamedDefinition {protected LdEvaluationFramework(){}public LdEvaluationFramework(String agency,String code){super(agency,code);}}
