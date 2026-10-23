package com.primehr.performancemanagement.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name="spms_monitoring_update",uniqueConstraints=@UniqueConstraint(name="uk_spms_monitoring_update_revision",columnNames={"agency_id","root_update_id","revision_no"}))
public class PerformanceMonitoringUpdate extends RspAuditedEntity {
    public enum Status { DRAFT, SUBMITTED, ACCEPTED, RETURNED, SUPERSEDED, VOIDED }
    @Column(name="monitoring_case_id",nullable=false,length=36) private String monitoringCaseId;
    @Column(name="commitment_item_id",nullable=false,length=36) private String commitmentItemId;
    @Column(name="root_update_id",nullable=false,length=36) private String rootUpdateId;
    @Column(name="revision_no",nullable=false) private int revisionNo;
    @Column(name="supersedes_id",length=36) private String supersedesId;
    @Column(name="reporting_date",nullable=false) private LocalDate reportingDate;
    @Column(name="narrative_accomplishment",nullable=false,length=4000) private String narrativeAccomplishment;
    @Column(name="accomplished_value",precision=19,scale=6) private BigDecimal accomplishedValue;
    @Column(name="progress_percent",precision=7,scale=4) private BigDecimal progressPercent;
    @Column(name="issues_risks",length=3000) private String issuesRisks;
    @Column(name="support_needed",length=3000) private String supportNeeded;
    @Column(name="employee_remarks",length=3000) private String employeeRemarks;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Column(name="submitted_by",length=100) private String submittedBy;
    @Column(name="submitted_at") private Instant submittedAt;
    protected PerformanceMonitoringUpdate() {}
    public PerformanceMonitoringUpdate(String agency,String monitoringCaseId,String itemId,String rootId,int revision,String supersedes,LocalDate reportingDate,String narrative,BigDecimal value,BigDecimal progress,String issues,String support,String remarks){super(agency);this.monitoringCaseId=requiredText(monitoringCaseId,"monitoringCaseId");commitmentItemId=requiredText(itemId,"commitmentItemId");rootUpdateId=requiredText(rootId,"rootUpdateId");if(revision<1)throw new IllegalArgumentException("revisionNo must be positive");revisionNo=revision;supersedesId=optionalText(supersedes);apply(reportingDate,narrative,value,progress,issues,support,remarks);status=Status.DRAFT;}
    public void edit(LocalDate date,String narrative,BigDecimal value,BigDecimal progress,String issues,String support,String remarks){if(status!=Status.DRAFT&&status!=Status.RETURNED)throw new IllegalStateException("Only a draft or returned update may be edited");apply(date,narrative,value,progress,issues,support,remarks);}
    private void apply(LocalDate date,String narrative,BigDecimal value,BigDecimal progress,String issues,String support,String remarks){reportingDate=java.util.Objects.requireNonNull(date,"reportingDate");narrativeAccomplishment=requiredText(narrative,"narrativeAccomplishment");if(progress!=null&&(progress.compareTo(BigDecimal.ZERO)<0||progress.compareTo(new BigDecimal("100"))>0))throw new IllegalArgumentException("progressPercent must be from 0 through 100");accomplishedValue=value;progressPercent=progress;issuesRisks=optionalText(issues);supportNeeded=optionalText(support);employeeRemarks=optionalText(remarks);}
    public void submit(String actor,Instant at){if(status!=Status.DRAFT&&status!=Status.RETURNED)throw new IllegalStateException("Only a draft or returned update may be submitted");status=Status.SUBMITTED;submittedBy=requiredText(actor,"actor");submittedAt=at;}
    public void accept(){if(status!=Status.SUBMITTED)throw new IllegalStateException("Only a submitted update may be accepted");status=Status.ACCEPTED;}
    public void returned(){if(status!=Status.SUBMITTED)throw new IllegalStateException("Only a submitted update may be returned");status=Status.RETURNED;}
    public void supersede(){if(status!=Status.SUBMITTED&&status!=Status.ACCEPTED&&status!=Status.RETURNED)throw new IllegalStateException("Only submitted history may be corrected");status=Status.SUPERSEDED;}
    public String getMonitoringCaseId(){return monitoringCaseId;} public String getCommitmentItemId(){return commitmentItemId;} public String getRootUpdateId(){return rootUpdateId;} public int getRevisionNo(){return revisionNo;} public String getSupersedesId(){return supersedesId;} public LocalDate getReportingDate(){return reportingDate;} public String getNarrativeAccomplishment(){return narrativeAccomplishment;} public BigDecimal getAccomplishedValue(){return accomplishedValue;} public BigDecimal getProgressPercent(){return progressPercent;} public String getIssuesRisks(){return issuesRisks;} public String getSupportNeeded(){return supportNeeded;} public String getEmployeeRemarks(){return employeeRemarks;} public Status getStatus(){return status;} public String getSubmittedBy(){return submittedBy;} public Instant getSubmittedAt(){return submittedAt;}
}
