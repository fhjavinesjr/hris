package com.primehr.learningdevelopment.plan.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ld_learning_need")
public class LdLearningNeed extends RspAuditedEntity {
    public enum Status { OPEN, LINKED, CANCELLED }

    @Column(name="employee_id",nullable=false) private Long employeeId;
    @Column(name="employee_no",nullable=false,length=100) private String employeeNo;
    @Column(name="employee_name",nullable=false,length=300) private String employeeName;
    @Column(name="appointment_id",nullable=false) private Long appointmentId;
    @Column(name="employee_fingerprint",nullable=false,length=64) private String employeeFingerprint;
    @Enumerated(EnumType.STRING) @Column(name="source_type",nullable=false,length=50) private LearningNeedSourceType sourceType;
    @Column(name="source_reference_id",length=100) private String sourceReferenceId;
    @Column(name="source_version") private Long sourceVersion;
    @Column(name="source_fingerprint",length=64) private String sourceFingerprint;
    @Column(name="target_competency_id",length=36) private String targetCompetencyId;
    @Column(name="target_competency_code",length=50) private String targetCompetencyCode;
    @Column(name="target_competency_name",length=200) private String targetCompetencyName;
    @Column(name="development_objective",nullable=false,length=3000) private String developmentObjective;
    @Column(name="current_state",nullable=false,length=2000) private String currentState;
    @Column(name="target_state",nullable=false,length=2000) private String targetState;
    @Column(name="success_measure",nullable=false,length=2000) private String successMeasure;
    @Column(name="target_date") private LocalDate targetDate;
    @Column(name="priority",nullable=false,length=20) private String priority;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;

    protected LdLearningNeed() {}
    public LdLearningNeed(String agency, Long employeeId, String employeeNo, String employeeName,
            Long appointmentId, String employeeFingerprint, LearningNeedSourceType sourceType,
            String sourceReferenceId, Long sourceVersion, String sourceFingerprint,
            String competencyId, String competencyCode, String competencyName,
            String objective, String currentState, String targetState, String successMeasure,
            LocalDate targetDate, String priority) {
        super(agency);
        if (employeeId == null || employeeId < 1 || appointmentId == null || appointmentId < 1)
            throw new IllegalArgumentException("An eligible employee appointment is required");
        this.employeeId=employeeId; this.employeeNo=requiredText(employeeNo,"employeeNo");
        this.employeeName=requiredText(employeeName,"employeeName"); this.appointmentId=appointmentId;
        this.employeeFingerprint=requiredText(employeeFingerprint,"employeeFingerprint");
        this.sourceType=java.util.Objects.requireNonNull(sourceType,"sourceType");
        this.sourceReferenceId=optionalText(sourceReferenceId); this.sourceVersion=sourceVersion;
        this.sourceFingerprint=optionalText(sourceFingerprint); this.targetCompetencyId=optionalText(competencyId);
        this.targetCompetencyCode=optionalText(competencyCode); this.targetCompetencyName=optionalText(competencyName);
        status=Status.OPEN; update(objective,currentState,targetState,successMeasure,targetDate,priority);
    }
    public void update(String objective,String current,String target,String measure,LocalDate date,String priority){
        if(status!=Status.OPEN)throw new IllegalStateException("Only an OPEN learning need may be edited");
        developmentObjective=requiredText(objective,"developmentObjective"); currentState=requiredText(current,"currentState");
        targetState=requiredText(target,"targetState"); successMeasure=requiredText(measure,"successMeasure");
        targetDate=date; this.priority=requiredText(priority,"priority").toUpperCase(java.util.Locale.ROOT);
        if(!java.util.Set.of("LOW","MEDIUM","HIGH","CRITICAL").contains(this.priority))throw new IllegalArgumentException("Unsupported priority");
    }
    public void link(){if(status==Status.CANCELLED)throw new IllegalStateException("A cancelled need cannot be linked");status=Status.LINKED;}
    public void reopen(){if(status==Status.LINKED)status=Status.OPEN;}
    public void cancel(){if(status!=Status.OPEN)throw new IllegalStateException("Only an OPEN learning need may be cancelled");status=Status.CANCELLED;}
    public Long getEmployeeId(){return employeeId;} public String getEmployeeNo(){return employeeNo;} public String getEmployeeName(){return employeeName;}
    public Long getAppointmentId(){return appointmentId;} public String getEmployeeFingerprint(){return employeeFingerprint;} public LearningNeedSourceType getSourceType(){return sourceType;}
    public String getSourceReferenceId(){return sourceReferenceId;} public Long getSourceVersion(){return sourceVersion;} public String getSourceFingerprint(){return sourceFingerprint;}
    public String getTargetCompetencyId(){return targetCompetencyId;} public String getTargetCompetencyCode(){return targetCompetencyCode;} public String getTargetCompetencyName(){return targetCompetencyName;}
    public String getDevelopmentObjective(){return developmentObjective;} public String getCurrentState(){return currentState;} public String getTargetState(){return targetState;}
    public String getSuccessMeasure(){return successMeasure;} public LocalDate getTargetDate(){return targetDate;} public String getPriority(){return priority;} public Status getStatus(){return status;}
}
