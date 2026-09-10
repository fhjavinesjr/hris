package com.primehr.performancemanagement.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="spms_commitment_route_step",uniqueConstraints=@UniqueConstraint(name="uk_spms_commitment_route_step",columnNames={"agency_id","route_id","route_level"}))
public class PerformanceCommitmentRouteStep extends RspAuditedEntity {
    public enum ActionType { RECOMMEND, APPROVE }
    public enum Status { PENDING, CURRENT, RECOMMENDED, RETURNED, APPROVED, REJECTED, SKIPPED }
    @Column(name="route_id",nullable=false,length=36) private String routeId;
    @Column(name="route_level",nullable=false) private int level;
    @Enumerated(EnumType.STRING) @Column(name="action_type",nullable=false,length=20) private ActionType actionType;
    @Column(name="employee_id",nullable=false) private Long employeeId;
    @Column(name="employee_no",nullable=false,length=100) private String employeeNo;
    @Column(name="employee_name",nullable=false,length=300) private String employeeName;
    @Column(name="appointment_id",nullable=false) private Long appointmentId;
    @Column(name="job_position_id",nullable=false) private Long jobPositionId;
    @Column(name="plantilla_id",nullable=false) private Long plantillaId;
    @Column(name="participant_fingerprint",nullable=false,length=64) private String participantFingerprint;
    @Column(name="organization_fingerprint",nullable=false,length=64) private String organizationFingerprint;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Column(name="decided_by",length=100) private String decidedBy; @Column(name="decided_at") private Instant decidedAt; @Column(name="decision_reason",length=2000) private String decisionReason;
    protected PerformanceCommitmentRouteStep() {}
    public PerformanceCommitmentRouteStep(String agency,String route,int level,ActionType action,Long employeeId,String employeeNo,String employeeName,Long appointmentId,Long jobPositionId,Long plantillaId,String participantFingerprint,String organizationFingerprint){super(agency);routeId=requiredText(route,"routeId");if(level<1)throw new IllegalArgumentException("route level must be positive");this.level=level;actionType=action;this.employeeId=employeeId;this.employeeNo=requiredText(employeeNo,"employeeNo");this.employeeName=requiredText(employeeName,"employeeName");this.appointmentId=appointmentId;this.jobPositionId=jobPositionId;this.plantillaId=plantillaId;this.participantFingerprint=requiredText(participantFingerprint,"participantFingerprint");this.organizationFingerprint=requiredText(organizationFingerprint,"organizationFingerprint");status=level==1?Status.CURRENT:Status.PENDING;}
    public void decide(Status result,String actor,String reason,Instant at){if(status!=Status.CURRENT)throw new IllegalStateException("Only the current route step may be decided");status=result;decidedBy=requiredText(actor,"decidedBy");decisionReason=optionalText(reason);decidedAt=at;}
    public void makeCurrent(){if(status!=Status.PENDING)throw new IllegalStateException("Only a pending route step may become current");status=Status.CURRENT;}
    public void skip(){if(status==Status.PENDING)status=Status.SKIPPED;}
    public void cancel(){if(status==Status.PENDING||status==Status.CURRENT)status=Status.SKIPPED;}
    public String getRouteId(){return routeId;} public int getLevel(){return level;} public ActionType getActionType(){return actionType;} public Long getEmployeeId(){return employeeId;} public String getEmployeeNo(){return employeeNo;} public String getEmployeeName(){return employeeName;} public Long getAppointmentId(){return appointmentId;} public Long getJobPositionId(){return jobPositionId;} public Long getPlantillaId(){return plantillaId;} public String getParticipantFingerprint(){return participantFingerprint;} public String getOrganizationFingerprint(){return organizationFingerprint;} public Status getStatus(){return status;} public String getDecidedBy(){return decidedBy;} public Instant getDecidedAt(){return decidedAt;} public String getDecisionReason(){return decisionReason;}
}
