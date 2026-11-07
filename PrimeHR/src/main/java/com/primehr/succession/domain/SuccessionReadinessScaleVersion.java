package com.primehr.succession.domain;
import jakarta.persistence.*;
@Entity @Table(name="succession_readiness_scale_version",uniqueConstraints=@UniqueConstraint(name="uk_succ_scale_version",columnNames={"agency_id","scale_id","definition_version"}))
public class SuccessionReadinessScaleVersion extends SuccessionVersionedDefinition {
 @Column(name="scale_id",nullable=false,length=36) private String scaleId;
 @Column(name="decision_guidance",nullable=false,length=4000) private String decisionGuidance;
 @Column(name="requires_narrative",nullable=false) private boolean requiresNarrative;
 @Column(name="levels_revision",nullable=false) private int levelsRevision;
 protected SuccessionReadinessScaleVersion(){}
 public SuccessionReadinessScaleVersion(String agencyId,String scaleId,int version,String supersedes,String title,String description,String guidance,boolean narrative){super(agencyId,version,supersedes,title,description);this.scaleId=requiredText(scaleId,"scaleId");apply(guidance,narrative);}
 public void update(String title,String description,String guidance,boolean narrative){changeDefinition(title,description);apply(guidance,narrative);} public void levelsChanged(){requireDraft();levelsRevision++;}
 private void apply(String guidance,boolean narrative){decisionGuidance=requiredText(guidance,"decisionGuidance");requiresNarrative=narrative;}
 public String getScaleId(){return scaleId;} public String getDecisionGuidance(){return decisionGuidance;} public boolean isRequiresNarrative(){return requiresNarrative;} public int getLevelsRevision(){return levelsRevision;}
}
