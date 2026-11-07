package com.primehr.operations.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="primehr_reconciliation_run")
public class ReconciliationRun extends RspAuditedEntity {
    public enum TriggerType { MANUAL, SCHEDULED }
    public enum Status { RUNNING, COMPLETED, COMPLETED_WITH_FINDINGS, FAILED }
    @Enumerated(EnumType.STRING) @Column(name="trigger_type",nullable=false,length=20) private TriggerType triggerType;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private Status status;
    @Column(name="started_by",nullable=false,length=100) private String startedBy;
    @Column(name="started_at",nullable=false) private Instant startedAt;
    @Column(name="completed_at") private Instant completedAt;
    @Column(name="checked_count",nullable=false) private int checkedCount;
    @Column(name="finding_count",nullable=false) private int findingCount;
    @Column(name="failure_summary",length=1000) private String failureSummary;
    protected ReconciliationRun() {}
    public ReconciliationRun(String agency,TriggerType trigger,String actor,Instant at){super(agency);triggerType=trigger;status=Status.RUNNING;startedBy=requiredText(actor,"actor");startedAt=at;}
    public void complete(int checked,int findings,Instant at){if(status!=Status.RUNNING)throw new IllegalStateException("Reconciliation run is already final");checkedCount=checked;findingCount=findings;completedAt=at;status=findings==0?Status.COMPLETED:Status.COMPLETED_WITH_FINDINGS;}
    public void fail(String summary,Instant at){if(status!=Status.RUNNING)throw new IllegalStateException("Reconciliation run is already final");failureSummary=requiredText(summary,"summary");completedAt=at;status=Status.FAILED;}
    public TriggerType getTriggerType(){return triggerType;} public Status getStatus(){return status;} public String getStartedBy(){return startedBy;} public Instant getStartedAt(){return startedAt;} public Instant getCompletedAt(){return completedAt;} public int getCheckedCount(){return checkedCount;} public int getFindingCount(){return findingCount;} public String getFailureSummary(){return failureSummary;}
}
