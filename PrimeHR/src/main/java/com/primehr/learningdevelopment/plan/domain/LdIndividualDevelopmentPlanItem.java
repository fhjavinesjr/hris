package com.primehr.learningdevelopment.plan.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="ld_individual_plan_item",uniqueConstraints=@UniqueConstraint(name="uk_ld_idp_item_need",columnNames={"agency_id","plan_id","learning_need_id"}))
public class LdIndividualDevelopmentPlanItem extends RspAuditedEntity {
    @Column(name="plan_id",nullable=false,length=36) private String planId;
    @Column(name="learning_need_id",nullable=false,length=36) private String learningNeedId;
    @Column(name="source_type",nullable=false,length=50) private String sourceType;
    @Column(name="source_reference_id",length=100) private String sourceReferenceId;
    @Column(name="source_version") private Long sourceVersion;
    @Column(name="source_fingerprint",length=64) private String sourceFingerprint;
    @Column(name="target_competency_id",length=36) private String targetCompetencyId;
    @Column(name="target_competency_code",length=50) private String targetCompetencyCode;
    @Column(name="target_competency_name",length=200) private String targetCompetencyName;
    @Column(name="development_objective",nullable=false,length=3000) private String developmentObjective;
    @Column(name="current_state",nullable=false,length=2000) private String currentState;
    @Column(name="target_state",nullable=false,length=2000) private String targetState;
    @Column(name="intervention_category",nullable=false,length=50) private String interventionCategory;
    @Column(name="success_measure",nullable=false,length=2000) private String successMeasure;
    @Column(name="target_date",nullable=false) private LocalDate targetDate;
    @Column(name="employee_commitment",nullable=false,length=2000) private String employeeCommitment;
    @Column(name="supervisor_commitment",nullable=false,length=2000) private String supervisorCommitment;
    @Column(name="priority",nullable=false,length=20) private String priority;
    @Column(name="display_order",nullable=false) private int displayOrder;
    protected LdIndividualDevelopmentPlanItem(){}
    public LdIndividualDevelopmentPlanItem(String agency,String planId,LdLearningNeed need,String interventionCategory,String employeeCommitment,String supervisorCommitment,int order){super(agency);this.planId=requiredText(planId,"planId");learningNeedId=need.getId();sourceType=need.getSourceType().name();sourceReferenceId=need.getSourceReferenceId();sourceVersion=need.getSourceVersion();sourceFingerprint=need.getSourceFingerprint();targetCompetencyId=need.getTargetCompetencyId();targetCompetencyCode=need.getTargetCompetencyCode();targetCompetencyName=need.getTargetCompetencyName();developmentObjective=need.getDevelopmentObjective();currentState=need.getCurrentState();targetState=need.getTargetState();successMeasure=need.getSuccessMeasure();if(need.getTargetDate()==null)throw new IllegalArgumentException("Learning need targetDate is required before adding it to an IDP");targetDate=need.getTargetDate();priority=need.getPriority();update(interventionCategory,employeeCommitment,supervisorCommitment,order);}
    public void update(String category,String employee,String supervisor,int order){interventionCategory=requiredText(category,"interventionCategory");employeeCommitment=requiredText(employee,"employeeCommitment");supervisorCommitment=requiredText(supervisor,"supervisorCommitment");if(order<1)throw new IllegalArgumentException("displayOrder must be positive");displayOrder=order;}
    public String getPlanId(){return planId;}public String getLearningNeedId(){return learningNeedId;}public String getSourceType(){return sourceType;}public String getSourceReferenceId(){return sourceReferenceId;}public Long getSourceVersion(){return sourceVersion;}public String getSourceFingerprint(){return sourceFingerprint;}public String getTargetCompetencyId(){return targetCompetencyId;}public String getTargetCompetencyCode(){return targetCompetencyCode;}public String getTargetCompetencyName(){return targetCompetencyName;}public String getDevelopmentObjective(){return developmentObjective;}public String getCurrentState(){return currentState;}public String getTargetState(){return targetState;}public String getInterventionCategory(){return interventionCategory;}public String getSuccessMeasure(){return successMeasure;}public LocalDate getTargetDate(){return targetDate;}public String getEmployeeCommitment(){return employeeCommitment;}public String getSupervisorCommitment(){return supervisorCommitment;}public String getPriority(){return priority;}public int getDisplayOrder(){return displayOrder;}
}
