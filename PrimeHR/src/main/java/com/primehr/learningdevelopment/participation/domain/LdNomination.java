package com.primehr.learningdevelopment.participation.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name="ld_nomination", uniqueConstraints=@UniqueConstraint(name="uk_ld_nomination_offering_employee", columnNames={"agency_id","offering_id","employee_id"}))
public class LdNomination extends RspAuditedEntity {
    public enum SourceType { IDP_ITEM, PLAN_SOURCE, JUSTIFIED }
    public enum InitiatorType { EMPLOYEE, SUPERVISOR, LD_STAFF }
    public enum Status { DRAFT, SUBMITTED, RECOMMENDED, RETURNED, APPROVED, CANCELLED }
    @Column(name="offering_id",nullable=false,length=36) private String offeringId;
    @Column(name="offering_version",nullable=false) private int offeringVersion;
    @Column(name="offering_fingerprint",nullable=false,length=64) private String offeringFingerprint;
    @Column(name="offering_title",nullable=false,length=200) private String offeringTitle;
    @Column(name="schedule_start",nullable=false) private OffsetDateTime scheduleStart;
    @Column(name="schedule_end",nullable=false) private OffsetDateTime scheduleEnd;
    @Enumerated(EnumType.STRING) @Column(name="source_type",nullable=false,length=20) private SourceType sourceType;
    @Column(name="source_id",length=36) private String sourceId;
    @Column(name="source_version") private Long sourceVersion;
    @Column(name="source_fingerprint",length=64) private String sourceFingerprint;
    @Column(name="justification",nullable=false,length=3000) private String justification;
    @Column(name="employee_id",nullable=false) private Long employeeId;
    @Column(name="employee_no",nullable=false,length=100) private String employeeNo;
    @Column(name="employee_name",nullable=false,length=300) private String employeeName;
    @Column(name="appointment_id",nullable=false) private Long appointmentId;
    @Column(name="employee_fingerprint",nullable=false,length=64) private String employeeFingerprint;
    @Column(name="business_unit_id",nullable=false) private Long businessUnitId;
    @Column(name="business_unit_name",nullable=false,length=200) private String businessUnitName;
    @Column(name="area_id") private Long areaId;
    @Column(name="area_name",length=200) private String areaName;
    @Column(name="organization_fingerprint",nullable=false,length=64) private String organizationFingerprint;
    @Column(name="estimated_cost",nullable=false,precision=19,scale=2) private BigDecimal estimatedCost;
    @Enumerated(EnumType.STRING) @Column(name="initiator_type",nullable=false,length=20) private InitiatorType initiatorType;
    @Column(name="initiated_by",nullable=false,length=100) private String initiatedBy;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Column(name="content_revision",nullable=false) private int contentRevision;
    @Column(name="submitted_by",length=100) private String submittedBy;
    @Column(name="submitted_at") private Instant submittedAt;
    @Column(name="approved_by",length=100) private String approvedBy;
    @Column(name="approved_at") private Instant approvedAt;
    @Column(name="return_reason",length=2000) private String returnReason;

    protected LdNomination() {}
    public LdNomination(String agency,String offeringId,int offeringVersion,String offeringFingerprint,String offeringTitle,
            OffsetDateTime scheduleStart,OffsetDateTime scheduleEnd,SourceType sourceType,String sourceId,Long sourceVersion,
            String sourceFingerprint,String justification,Long employeeId,String employeeNo,String employeeName,
            Long appointmentId,String employeeFingerprint,Long businessUnitId,String businessUnitName,Long areaId,
            String areaName,String organizationFingerprint,BigDecimal estimatedCost,InitiatorType initiatorType,String initiatedBy) {
        super(agency);this.offeringId=requiredText(offeringId,"offeringId");if(offeringVersion<1)throw new IllegalArgumentException("offeringVersion is required");this.offeringVersion=offeringVersion;
        this.offeringFingerprint=requiredText(offeringFingerprint,"offeringFingerprint");this.offeringTitle=requiredText(offeringTitle,"offeringTitle");if(scheduleStart==null||scheduleEnd==null||!scheduleEnd.isAfter(scheduleStart))throw new IllegalArgumentException("Valid offering schedule is required");this.scheduleStart=scheduleStart;this.scheduleEnd=scheduleEnd;
        this.sourceType=java.util.Objects.requireNonNull(sourceType);this.sourceId=optionalText(sourceId);this.sourceVersion=sourceVersion;this.sourceFingerprint=optionalText(sourceFingerprint);this.justification=requiredText(justification,"justification");if(sourceType!=SourceType.JUSTIFIED&&(this.sourceId==null||sourceVersion==null||this.sourceFingerprint==null))throw new IllegalArgumentException("Nomination source snapshot is incomplete");
        this.employeeId=positive(employeeId,"employeeId");this.employeeNo=requiredText(employeeNo,"employeeNo");this.employeeName=requiredText(employeeName,"employeeName");this.appointmentId=positive(appointmentId,"appointmentId");this.employeeFingerprint=requiredText(employeeFingerprint,"employeeFingerprint");this.businessUnitId=positive(businessUnitId,"businessUnitId");this.businessUnitName=requiredText(businessUnitName,"businessUnitName");this.areaId=areaId;this.areaName=optionalText(areaName);this.organizationFingerprint=requiredText(organizationFingerprint,"organizationFingerprint");
        if(estimatedCost==null||estimatedCost.signum()<0)throw new IllegalArgumentException("estimatedCost cannot be negative");this.estimatedCost=estimatedCost.setScale(2,java.math.RoundingMode.HALF_UP);this.initiatorType=java.util.Objects.requireNonNull(initiatorType);this.initiatedBy=requiredText(initiatedBy,"initiatedBy");status=Status.DRAFT;contentRevision=1;
    }
    public void update(String justification,BigDecimal cost){editable();this.justification=requiredText(justification,"justification");if(cost==null||cost.signum()<0)throw new IllegalArgumentException("estimatedCost cannot be negative");estimatedCost=cost.setScale(2,java.math.RoundingMode.HALF_UP);contentRevision++;}
    public void submit(String actor,Instant at){editable();status=Status.SUBMITTED;submittedBy=requiredText(actor,"actor");submittedAt=java.util.Objects.requireNonNull(at);returnReason=null;}
    public void recommend(){if(status!=Status.SUBMITTED&&status!=Status.RECOMMENDED)throw new IllegalStateException("Only a submitted nomination may be recommended");status=Status.RECOMMENDED;}
    public void returnForChanges(String reason){if(status!=Status.SUBMITTED&&status!=Status.RECOMMENDED)throw new IllegalStateException("Only an active nomination may be returned");status=Status.RETURNED;returnReason=requiredText(reason,"reason");}
    public void approve(String actor,Instant at){if(status!=Status.SUBMITTED&&status!=Status.RECOMMENDED)throw new IllegalStateException("Only an active nomination may be approved");status=Status.APPROVED;approvedBy=requiredText(actor,"actor");approvedAt=java.util.Objects.requireNonNull(at);}
    public void cancel(){if(status==Status.APPROVED||status==Status.CANCELLED)throw new IllegalStateException("Approved or cancelled nomination cannot be cancelled");status=Status.CANCELLED;}
    private void editable(){if(status!=Status.DRAFT&&status!=Status.RETURNED)throw new IllegalStateException("Only a draft or returned nomination may be changed");}
    private static Long positive(Long value,String name){if(value==null||value<1)throw new IllegalArgumentException(name+" is required");return value;}
    public String getOfferingId(){return offeringId;} public int getOfferingVersion(){return offeringVersion;} public String getOfferingFingerprint(){return offeringFingerprint;} public String getOfferingTitle(){return offeringTitle;} public OffsetDateTime getScheduleStart(){return scheduleStart;} public OffsetDateTime getScheduleEnd(){return scheduleEnd;} public SourceType getSourceType(){return sourceType;} public String getSourceId(){return sourceId;} public Long getSourceVersion(){return sourceVersion;} public String getSourceFingerprint(){return sourceFingerprint;} public String getJustification(){return justification;} public Long getEmployeeId(){return employeeId;} public String getEmployeeNo(){return employeeNo;} public String getEmployeeName(){return employeeName;} public Long getAppointmentId(){return appointmentId;} public String getEmployeeFingerprint(){return employeeFingerprint;} public Long getBusinessUnitId(){return businessUnitId;} public String getBusinessUnitName(){return businessUnitName;} public Long getAreaId(){return areaId;} public String getAreaName(){return areaName;} public String getOrganizationFingerprint(){return organizationFingerprint;} public BigDecimal getEstimatedCost(){return estimatedCost;} public InitiatorType getInitiatorType(){return initiatorType;} public String getInitiatedBy(){return initiatedBy;} public Status getStatus(){return status;} public int getContentRevision(){return contentRevision;} public String getSubmittedBy(){return submittedBy;} public Instant getSubmittedAt(){return submittedAt;} public String getApprovedBy(){return approvedBy;} public Instant getApprovedAt(){return approvedAt;} public String getReturnReason(){return returnReason;}
}
