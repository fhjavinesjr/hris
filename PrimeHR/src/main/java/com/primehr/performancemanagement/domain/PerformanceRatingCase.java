package com.primehr.performancemanagement.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="spms_rating_case",uniqueConstraints=@UniqueConstraint(name="uk_spms_rating_case_revision",columnNames={"agency_id","monitoring_case_id","accomplishment_revision"}))
public class PerformanceRatingCase extends RspAuditedEntity {
    public enum Status{OPEN,SUPERVISOR_SUBMITTED,CALIBRATION_OPEN,READY_FOR_FINALIZATION,FINALIZED,VOIDED}
    @Column(name="monitoring_case_id",nullable=false,length=36)private String monitoringCaseId;
    @Column(name="commitment_version_id",nullable=false,length=36)private String commitmentVersionId;
    @Column(name="cycle_id",nullable=false,length=36)private String cycleId;
    @Column(name="policy_version_id",nullable=false,length=36)private String policyVersionId;
    @Column(name="template_version_id",nullable=false,length=36)private String templateVersionId;
    @Column(name="rating_scale_version_id",nullable=false,length=36)private String ratingScaleVersionId;
    @Column(name="form_type",nullable=false,length=20)private String formType;
    @Column(name="owner_employee_id",nullable=false)private Long ownerEmployeeId;
    @Column(name="owner_employee_no",nullable=false,length=100)private String ownerEmployeeNo;
    @Column(name="owner_name",nullable=false,length=300)private String ownerName;
    @Column(name="business_unit_id",nullable=false)private Long businessUnitId;
    @Column(name="supervisor_employee_no",nullable=false,length=100)private String supervisorEmployeeNo;
    @Column(name="accomplishment_revision",nullable=false)private int accomplishmentRevision;
    @Column(name="requires_self_assessment",nullable=false)private boolean requiresSelfAssessment;
    @Column(name="route_fingerprint",nullable=false,length=64)private String routeFingerprint;
    @Column(name="commitment_fingerprint",nullable=false,length=64)private String commitmentFingerprint;
    @Column(name="source_fingerprint",nullable=false,length=64)private String sourceFingerprint;
    @Enumerated(EnumType.STRING)@Column(nullable=false,length=30)private Status status;
    @Column(name="opened_by",nullable=false,length=100)private String openedBy;
    @Column(name="opened_at",nullable=false)private Instant openedAt;
    protected PerformanceRatingCase(){}
    public PerformanceRatingCase(String agency,PerformanceMonitoringCase monitoring,PerformanceCommitmentVersion commitment,boolean self,String source,String actor,Instant at){super(agency);monitoringCaseId=monitoring.getId();commitmentVersionId=commitment.getId();cycleId=commitment.getCycleId();policyVersionId=commitment.getPolicyVersionId();templateVersionId=commitment.getTemplateVersionId();ratingScaleVersionId=commitment.getRatingScaleVersionId();formType=commitment.getFormType();ownerEmployeeId=commitment.getOwnerEmployeeId();ownerEmployeeNo=commitment.getOwnerEmployeeNo();ownerName=commitment.getOwnerName();businessUnitId=commitment.getBusinessUnitId();supervisorEmployeeNo=monitoring.getSupervisorEmployeeNo();accomplishmentRevision=monitoring.getAccomplishmentRevision();requiresSelfAssessment=self;routeFingerprint=monitoring.getRouteFingerprint();commitmentFingerprint=monitoring.getCommitmentFingerprint();sourceFingerprint=requiredText(source,"sourceFingerprint");status=Status.OPEN;openedBy=requiredText(actor,"actor");openedAt=at;}
    public void supervisorSubmitted(){if(status!=Status.OPEN)throw new IllegalStateException("Only an open rating case may receive the supervisor assessment");status=Status.SUPERVISOR_SUBMITTED;}
    public void calibrationOpened(){if(status!=Status.SUPERVISOR_SUBMITTED)throw new IllegalStateException("Calibration requires a submitted supervisor assessment");status=Status.CALIBRATION_OPEN;}
    public void readyForFinalization(){if(status!=Status.SUPERVISOR_SUBMITTED&&status!=Status.CALIBRATION_OPEN)throw new IllegalStateException("Rating case is not ready for finalization");status=Status.READY_FOR_FINALIZATION;}
    public void finalized(){if(status!=Status.READY_FOR_FINALIZATION)throw new IllegalStateException("Only a ready rating case may be finalized");status=Status.FINALIZED;}
    public String getMonitoringCaseId(){return monitoringCaseId;}public String getCommitmentVersionId(){return commitmentVersionId;}public String getCycleId(){return cycleId;}public String getPolicyVersionId(){return policyVersionId;}public String getTemplateVersionId(){return templateVersionId;}public String getRatingScaleVersionId(){return ratingScaleVersionId;}public String getFormType(){return formType;}public Long getOwnerEmployeeId(){return ownerEmployeeId;}public String getOwnerEmployeeNo(){return ownerEmployeeNo;}public String getOwnerName(){return ownerName;}public Long getBusinessUnitId(){return businessUnitId;}public String getSupervisorEmployeeNo(){return supervisorEmployeeNo;}public int getAccomplishmentRevision(){return accomplishmentRevision;}public boolean isRequiresSelfAssessment(){return requiresSelfAssessment;}public String getRouteFingerprint(){return routeFingerprint;}public String getCommitmentFingerprint(){return commitmentFingerprint;}public String getSourceFingerprint(){return sourceFingerprint;}public Status getStatus(){return status;}public String getOpenedBy(){return openedBy;}public Instant getOpenedAt(){return openedAt;}
}
