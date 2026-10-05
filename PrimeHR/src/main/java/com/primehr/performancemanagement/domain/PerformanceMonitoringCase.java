package com.primehr.performancemanagement.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="spms_monitoring_case",uniqueConstraints=@UniqueConstraint(name="uk_spms_monitoring_commitment",columnNames={"agency_id","commitment_version_id"}))
public class PerformanceMonitoringCase extends RspAuditedEntity {
    public enum Status { OPEN, ACCOMPLISHMENT_SUBMITTED, RETURNED, READY_FOR_RATING, VOIDED }
    @Column(name="commitment_version_id",nullable=false,length=36) private String commitmentVersionId;
    @Column(name="cycle_id",nullable=false,length=36) private String cycleId;
    @Column(name="policy_version_id",nullable=false,length=36) private String policyVersionId;
    @Column(name="form_type",nullable=false,length=20) private String formType;
    @Column(name="owner_employee_id",nullable=false) private Long ownerEmployeeId;
    @Column(name="owner_employee_no",nullable=false,length=100) private String ownerEmployeeNo;
    @Column(name="owner_name",nullable=false,length=300) private String ownerName;
    @Column(name="business_unit_id",nullable=false) private Long businessUnitId;
    @Column(name="supervisor_employee_no",nullable=false,length=100) private String supervisorEmployeeNo;
    @Column(name="route_fingerprint",nullable=false,length=64) private String routeFingerprint;
    @Column(name="commitment_fingerprint",nullable=false,length=64) private String commitmentFingerprint;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=32) private Status status;
    @Column(name="accomplishment_revision",nullable=false) private int accomplishmentRevision;
    @Column(name="accomplishment_submitted_by",length=100) private String accomplishmentSubmittedBy;
    @Column(name="accomplishment_submitted_at") private Instant accomplishmentSubmittedAt;
    @Column(name="return_reason",length=2000) private String returnReason;
    protected PerformanceMonitoringCase() {}
    public PerformanceMonitoringCase(String agency,PerformanceCommitmentVersion commitment,String supervisor,String routeFingerprint){super(agency);commitmentVersionId=commitment.getId();cycleId=commitment.getCycleId();policyVersionId=commitment.getPolicyVersionId();formType=commitment.getFormType();ownerEmployeeId=commitment.getOwnerEmployeeId();ownerEmployeeNo=commitment.getOwnerEmployeeNo();ownerName=commitment.getOwnerName();businessUnitId=commitment.getBusinessUnitId();supervisorEmployeeNo=requiredText(supervisor,"supervisorEmployeeNo");this.routeFingerprint=requiredText(routeFingerprint,"routeFingerprint");commitmentFingerprint=requiredText(commitment.getContentFingerprint(),"commitmentFingerprint");status=Status.OPEN;}
    public void submitAccomplishment(String actor,Instant at){if(status!=Status.OPEN&&status!=Status.RETURNED)throw new IllegalStateException("Only an open or returned monitoring case may submit accomplishments");status=Status.ACCOMPLISHMENT_SUBMITTED;accomplishmentRevision++;accomplishmentSubmittedBy=requiredText(actor,"actor");accomplishmentSubmittedAt=at;returnReason=null;}
    public void returnAccomplishment(String reason){if(status!=Status.ACCOMPLISHMENT_SUBMITTED)throw new IllegalStateException("Only submitted accomplishments may be returned");status=Status.RETURNED;returnReason=requiredText(reason,"reason");}
    public void readyForRating(){if(status!=Status.ACCOMPLISHMENT_SUBMITTED)throw new IllegalStateException("Only submitted accomplishments may be accepted for rating");status=Status.READY_FOR_RATING;returnReason=null;}
    public String getCommitmentVersionId(){return commitmentVersionId;} public String getCycleId(){return cycleId;} public String getPolicyVersionId(){return policyVersionId;} public String getFormType(){return formType;} public Long getOwnerEmployeeId(){return ownerEmployeeId;} public String getOwnerEmployeeNo(){return ownerEmployeeNo;} public String getOwnerName(){return ownerName;} public Long getBusinessUnitId(){return businessUnitId;} public String getSupervisorEmployeeNo(){return supervisorEmployeeNo;} public String getRouteFingerprint(){return routeFingerprint;} public String getCommitmentFingerprint(){return commitmentFingerprint;} public Status getStatus(){return status;} public int getAccomplishmentRevision(){return accomplishmentRevision;} public String getAccomplishmentSubmittedBy(){return accomplishmentSubmittedBy;} public Instant getAccomplishmentSubmittedAt(){return accomplishmentSubmittedAt;} public String getReturnReason(){return returnReason;}
}
