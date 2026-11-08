package com.primehr.learningdevelopment.domain;
import jakarta.persistence.*;
@Entity @Table(name="ld_evaluation_framework_version",uniqueConstraints=@UniqueConstraint(name="uk_ld_evaluation_framework_version",columnNames={"agency_id","framework_id","definition_version"}))
public class LdEvaluationFrameworkVersion extends LdVersionedDefinition {
 @Column(name="framework_id",nullable=false,length=36)private String frameworkId;@Column(name="legal_basis",nullable=false,length=1000)private String legalBasis;@Column(name="level_revision",nullable=false)private int levelRevision;
 protected LdEvaluationFrameworkVersion(){}public LdEvaluationFrameworkVersion(String agency,String framework,int version,String prior,String title,String description,String basis){super(agency,version,prior,title,description);frameworkId=requiredText(framework,"frameworkId");legalBasis=requiredText(basis,"legalBasis");}
 public void update(String title,String description,String basis){changeDefinition(title,description);legalBasis=requiredText(basis,"legalBasis");}public void levelsChanged(){requireDraft();levelRevision++;}
 public String getFrameworkId(){return frameworkId;}public String getLegalBasis(){return legalBasis;}public int getLevelRevision(){return levelRevision;}
}
