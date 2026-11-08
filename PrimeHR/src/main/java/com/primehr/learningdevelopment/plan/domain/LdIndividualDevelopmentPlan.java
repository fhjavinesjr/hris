package com.primehr.learningdevelopment.plan.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="ld_individual_plan",uniqueConstraints=@UniqueConstraint(name="uk_ld_idp_series_version",columnNames={"agency_id","series_id","plan_version"}))
public class LdIndividualDevelopmentPlan extends RspAuditedEntity {
    public enum Status { DRAFT, SUBMITTED, RECOMMENDED, RETURNED, APPROVED, CANCELLED }
    @Column(name="series_id",nullable=false,length=36) private String seriesId;
    @Column(name="plan_version",nullable=false) private int planVersion;
    @Column(name="supersedes_id",length=36) private String supersedesId;
    @Column(name="employee_id",nullable=false) private Long employeeId;
    @Column(name="employee_no",nullable=false,length=100) private String employeeNo;
    @Column(name="employee_name",nullable=false,length=300) private String employeeName;
    @Column(name="appointment_id",nullable=false) private Long appointmentId;
    @Column(name="job_position_id",nullable=false) private Long jobPositionId;
    @Column(name="plantilla_id",nullable=false) private Long plantillaId;
    @Column(name="employee_fingerprint",nullable=false,length=64) private String employeeFingerprint;
    @Column(name="business_unit_id",nullable=false) private Long businessUnitId;
    @Column(name="business_unit_name",nullable=false,length=200) private String businessUnitName;
    @Column(name="area_id",nullable=false) private Long areaId;
    @Column(name="area_name",nullable=false,length=200) private String areaName;
    @Column(name="organization_fingerprint",nullable=false,length=64) private String organizationFingerprint;
    @Column(name="planning_year",nullable=false) private int planningYear;
    @Column(name="overall_goal",nullable=false,length=3000) private String overallGoal;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Column(name="content_revision",nullable=false) private int contentRevision;
    @Column(name="submitted_by",length=100) private String submittedBy; @Column(name="submitted_at") private Instant submittedAt;
    @Column(name="approved_by",length=100) private String approvedBy; @Column(name="approved_at") private Instant approvedAt;
    @Column(name="return_reason",length=2000) private String returnReason;
    protected LdIndividualDevelopmentPlan(){}
    public LdIndividualDevelopmentPlan(String agency,Long employeeId,String employeeNo,String employeeName,Long appointmentId,Long jobPositionId,Long plantillaId,String employeeFingerprint,Long unit,String unitName,Long area,String areaName,String organizationFingerprint,int year,String goal){super(agency);
        seriesId=java.util.UUID.randomUUID().toString();planVersion=1;this.employeeId=employeeId;this.employeeNo=requiredText(employeeNo,"employeeNo");this.employeeName=requiredText(employeeName,"employeeName");this.appointmentId=appointmentId;this.jobPositionId=jobPositionId;this.plantillaId=plantillaId;this.employeeFingerprint=requiredText(employeeFingerprint,"employeeFingerprint");businessUnitId=unit;businessUnitName=requiredText(unitName,"businessUnitName");areaId=area;this.areaName=requiredText(areaName,"areaName");this.organizationFingerprint=requiredText(organizationFingerprint,"organizationFingerprint");if(year<2000||year>2200)throw new IllegalArgumentException("planningYear is invalid");planningYear=year;overallGoal=requiredText(goal,"overallGoal");status=Status.DRAFT;contentRevision=1;}
    public LdIndividualDevelopmentPlan successor(String goal){if(status!=Status.APPROVED)throw new IllegalStateException("Only an APPROVED IDP may be amended");LdIndividualDevelopmentPlan n=new LdIndividualDevelopmentPlan(getAgencyId(),employeeId,employeeNo,employeeName,appointmentId,jobPositionId,plantillaId,employeeFingerprint,businessUnitId,businessUnitName,areaId,areaName,organizationFingerprint,planningYear,goal);n.seriesId=seriesId;n.planVersion=planVersion+1;n.supersedesId=getId();return n;}
    public void updateGoal(String goal){editable();overallGoal=requiredText(goal,"overallGoal");contentRevision++;}
    public void touchContent(){editable();contentRevision++;}
    public void submit(String actor,Instant at){editable();status=Status.SUBMITTED;submittedBy=requiredText(actor,"actor");submittedAt=at;returnReason=null;}
    public void recommend(){if(status!=Status.SUBMITTED&&status!=Status.RECOMMENDED)throw new IllegalStateException("Only an in-review IDP may be recommended");status=Status.RECOMMENDED;}
    public void returnForChanges(String reason){if(status!=Status.SUBMITTED&&status!=Status.RECOMMENDED)throw new IllegalStateException("Only an in-review IDP may be returned");status=Status.RETURNED;returnReason=requiredText(reason,"reason");}
    public void approve(String actor,Instant at){if(status!=Status.SUBMITTED&&status!=Status.RECOMMENDED)throw new IllegalStateException("Only an in-review IDP may be approved");status=Status.APPROVED;approvedBy=requiredText(actor,"actor");approvedAt=at;}
    public void cancel(){editable();status=Status.CANCELLED;}
    private void editable(){if(status!=Status.DRAFT&&status!=Status.RETURNED)throw new IllegalStateException("Only a DRAFT or RETURNED IDP may be changed");}
    public String getSeriesId(){return seriesId;}public int getPlanVersion(){return planVersion;}public String getSupersedesId(){return supersedesId;}public Long getEmployeeId(){return employeeId;}public String getEmployeeNo(){return employeeNo;}public String getEmployeeName(){return employeeName;}public Long getAppointmentId(){return appointmentId;}public Long getJobPositionId(){return jobPositionId;}public Long getPlantillaId(){return plantillaId;}public String getEmployeeFingerprint(){return employeeFingerprint;}public Long getBusinessUnitId(){return businessUnitId;}public String getBusinessUnitName(){return businessUnitName;}public Long getAreaId(){return areaId;}public String getAreaName(){return areaName;}public String getOrganizationFingerprint(){return organizationFingerprint;}public int getPlanningYear(){return planningYear;}public String getOverallGoal(){return overallGoal;}public Status getStatus(){return status;}public int getContentRevision(){return contentRevision;}public String getSubmittedBy(){return submittedBy;}public Instant getSubmittedAt(){return submittedAt;}public String getApprovedBy(){return approvedBy;}public Instant getApprovedAt(){return approvedAt;}public String getReturnReason(){return returnReason;}
}
