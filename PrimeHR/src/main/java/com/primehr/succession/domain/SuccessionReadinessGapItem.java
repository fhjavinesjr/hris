package com.primehr.succession.domain;

import com.primehr.gap.application.TargetCompetencyGapService.TargetGapItem;
import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;

@Entity
@Table(name="succession_readiness_gap_item",uniqueConstraints=@UniqueConstraint(name="uk_succ_readiness_gap_competency",columnNames={"agency_id","assessment_id","competency_version_id"}))
public class SuccessionReadinessGapItem extends RspAuditedEntity {
    @Column(name="assessment_id",nullable=false,length=36) private String assessmentId;
    @Column(name="position_requirement_id",nullable=false,length=36) private String positionRequirementId;
    @Column(name="competency_version_id",nullable=false,length=36) private String competencyVersionId;
    @Column(name="competency_code",nullable=false,length=80) private String competencyCode;
    @Column(name="competency_name",nullable=false,length=200) private String competencyName;
    @Column(name="competency_definition_version",nullable=false) private int competencyDefinitionVersion;
    @Column(name="scale_version_id",nullable=false,length=36) private String scaleVersionId;
    @Column(name="scale_definition_version",nullable=false) private int scaleDefinitionVersion;
    @Column(name="required_level_id",nullable=false,length=36) private String requiredLevelId;
    @Column(name="required_level_code",nullable=false,length=80) private String requiredLevelCode;
    @Column(name="required_level_label",nullable=false,length=200) private String requiredLevelLabel;
    @Column(name="required_level_order",nullable=false) private int requiredLevelOrder;
    @Column(name="attained_level_id",length=36) private String attainedLevelId;
    @Column(name="attained_level_code",length=80) private String attainedLevelCode;
    @Column(name="attained_level_label",length=200) private String attainedLevelLabel;
    @Column(name="attained_level_order") private Integer attainedLevelOrder;
    @Column(name="gap_value") private Integer gapValue;
    @Column(name="gap_classification",nullable=false,length=30) private String gapClassification;
    @Column(name="not_assessed_reason",length=40) private String notAssessedReason;
    @Column(name="requirement_classification",nullable=false,length=30) private String requirementClassification;
    @Column(name="criticality_code",length=80) private String criticalityCode;
    @Column(name="display_order",nullable=false) private int displayOrder;
    protected SuccessionReadinessGapItem() { }
    public SuccessionReadinessGapItem(String agency,String assessment,TargetGapItem item){super(agency);assessmentId=requiredText(assessment,"assessmentId");positionRequirementId=requiredText(item.positionRequirementId(),"positionRequirementId");competencyVersionId=requiredText(item.competencyVersionId(),"competencyVersionId");competencyCode=requiredText(item.competencyCode(),"competencyCode");competencyName=requiredText(item.competencyName(),"competencyName");competencyDefinitionVersion=item.competencyDefinitionVersion();scaleVersionId=requiredText(item.scaleVersionId(),"scaleVersionId");scaleDefinitionVersion=item.scaleDefinitionVersion();requiredLevelId=requiredText(item.requiredLevelId(),"requiredLevelId");requiredLevelCode=requiredText(item.requiredLevelCode(),"requiredLevelCode");requiredLevelLabel=requiredText(item.requiredLevelLabel(),"requiredLevelLabel");requiredLevelOrder=item.requiredLevelOrder();attainedLevelId=item.attainedLevelId();attainedLevelCode=item.attainedLevelCode();attainedLevelLabel=item.attainedLevelLabel();attainedLevelOrder=item.attainedLevelOrder();gapValue=item.gap();gapClassification=item.classification().name();notAssessedReason=item.notAssessedReason()==null?null:item.notAssessedReason().name();requirementClassification=requiredText(item.requirementClassification(),"requirementClassification");criticalityCode=optionalText(item.criticalityCode());displayOrder=item.displayOrder();}
    public String getAssessmentId(){return assessmentId;} public String getPositionRequirementId(){return positionRequirementId;} public String getCompetencyVersionId(){return competencyVersionId;} public String getCompetencyCode(){return competencyCode;} public String getCompetencyName(){return competencyName;} public int getCompetencyDefinitionVersion(){return competencyDefinitionVersion;} public String getScaleVersionId(){return scaleVersionId;} public int getScaleDefinitionVersion(){return scaleDefinitionVersion;} public String getRequiredLevelId(){return requiredLevelId;} public String getRequiredLevelCode(){return requiredLevelCode;} public String getRequiredLevelLabel(){return requiredLevelLabel;} public int getRequiredLevelOrder(){return requiredLevelOrder;} public String getAttainedLevelId(){return attainedLevelId;} public String getAttainedLevelCode(){return attainedLevelCode;} public String getAttainedLevelLabel(){return attainedLevelLabel;} public Integer getAttainedLevelOrder(){return attainedLevelOrder;} public Integer getGapValue(){return gapValue;} public String getGapClassification(){return gapClassification;} public String getNotAssessedReason(){return notAssessedReason;} public String getRequirementClassification(){return requirementClassification;} public String getCriticalityCode(){return criticalityCode;} public int getDisplayOrder(){return displayOrder;}
}
