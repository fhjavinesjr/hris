package com.primehr.learningdevelopment.evaluation.domain;

import com.primehr.learningdevelopment.domain.LdEvaluationLevel;
import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name="ld_evaluation_task",uniqueConstraints=@UniqueConstraint(name="uk_ld_evaluation_task_level",columnNames={"agency_id","completion_case_id","level_type"}))
public class LdEvaluationTask extends RspAuditedEntity {
    public enum EvaluatorRole { PARTICIPANT, SUPERVISOR, LD_STAFF }
    public enum Status { OPEN, SATISFIED }
    @Column(name="completion_case_id",nullable=false,length=36) private String completionCaseId;
    @Column(name="offering_id",nullable=false,length=36) private String offeringId;
    @Column(name="employee_no",nullable=false,length=100) private String employeeNo;
    @Column(name="framework_version_id",nullable=false,length=36) private String frameworkVersionId;
    @Column(name="framework_fingerprint",nullable=false,length=64) private String frameworkFingerprint;
    @Enumerated(EnumType.STRING) @Column(name="level_type",nullable=false,length=30) private LdEvaluationLevel.Type levelType;
    @Column(nullable=false) private boolean required;
    @Enumerated(EnumType.STRING) @Column(name="score_method",nullable=false,length=20) private LdEvaluationLevel.ScoreMethod scoreMethod;
    @Column(name="passing_score",precision=19,scale=6) private BigDecimal passingScore;
    @Column(name="evidence_requirements",nullable=false,length=3000) private String evidenceRequirements;
    @Column(name="instrument_guidance",nullable=false,length=3000) private String instrumentGuidance;
    @Enumerated(EnumType.STRING) @Column(name="evaluator_role",nullable=false,length=20) private EvaluatorRole evaluatorRole;
    @Column(name="assigned_evaluator_no",nullable=false,length=100) private String assignedEvaluatorNo;
    @Column(name="due_date",nullable=false) private LocalDate dueDate;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Column(name="current_result_id",length=36) private String currentResultId;
    protected LdEvaluationTask() {}
    public LdEvaluationTask(String agency,String completion,String offering,String employee,String framework,String fingerprint,LdEvaluationLevel level,EvaluatorRole role,String evaluator,LocalDate completionDate){
        super(agency);completionCaseId=requiredText(completion,"completionCaseId");offeringId=requiredText(offering,"offeringId");employeeNo=requiredText(employee,"employeeNo");frameworkVersionId=requiredText(framework,"frameworkVersionId");frameworkFingerprint=requiredText(fingerprint,"frameworkFingerprint");levelType=Objects.requireNonNull(level.getLevelType());required=level.isRequired();scoreMethod=level.getScoreMethod();passingScore=level.getPassingScore();evidenceRequirements=requiredText(level.getEvidenceRequirements(),"evidenceRequirements");instrumentGuidance=requiredText(level.getInstrumentGuidance(),"instrumentGuidance");evaluatorRole=Objects.requireNonNull(role);assignedEvaluatorNo=requiredText(evaluator,"assignedEvaluatorNo");dueDate=Objects.requireNonNull(completionDate).plusDays(level.getDueAfterDays());status=Status.OPEN;
    }
    public void satisfy(String resultId){if(status==Status.SATISFIED)throw new IllegalStateException("Evaluation task is already satisfied");currentResultId=requiredText(resultId,"resultId");status=Status.SATISFIED;}
    public void reopen(){status=Status.OPEN;currentResultId=null;}
    public String getCompletionCaseId(){return completionCaseId;} public String getOfferingId(){return offeringId;} public String getEmployeeNo(){return employeeNo;} public String getFrameworkVersionId(){return frameworkVersionId;} public String getFrameworkFingerprint(){return frameworkFingerprint;} public LdEvaluationLevel.Type getLevelType(){return levelType;} public boolean isRequired(){return required;} public LdEvaluationLevel.ScoreMethod getScoreMethod(){return scoreMethod;} public BigDecimal getPassingScore(){return passingScore;} public String getEvidenceRequirements(){return evidenceRequirements;} public String getInstrumentGuidance(){return instrumentGuidance;} public EvaluatorRole getEvaluatorRole(){return evaluatorRole;} public String getAssignedEvaluatorNo(){return assignedEvaluatorNo;} public LocalDate getDueDate(){return dueDate;} public Status getStatus(){return status;} public String getCurrentResultId(){return currentResultId;}
}
