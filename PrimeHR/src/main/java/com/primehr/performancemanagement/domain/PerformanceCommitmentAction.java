package com.primehr.performancemanagement.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="spms_commitment_action",uniqueConstraints=@UniqueConstraint(name="uk_spms_commitment_action_request",columnNames={"agency_id","commitment_version_id","request_key"}))
public class PerformanceCommitmentAction extends RspAuditedEntity {
    public enum Type { SUBMIT, RESUBMIT, WITHDRAW, RECOMMEND, RETURN, APPROVE, REJECT, AMEND, VOID, ROUTE_REBASE, WINDOW_OVERRIDE }
    @Column(name="commitment_version_id",nullable=false,length=36) private String commitmentVersionId;
    @Column(name="route_id",length=36) private String routeId; @Column(name="route_step_id",length=36) private String routeStepId;
    @Enumerated(EnumType.STRING) @Column(name="action_type",nullable=false,length=24) private Type type;
    @Column(name="request_key",nullable=false,length=100) private String requestKey; @Column(name="actor_employee_no",nullable=false,length=100) private String actorEmployeeNo;
    @Column(name="prior_status",nullable=false,length=24) private String priorStatus; @Column(name="result_status",nullable=false,length=24) private String resultStatus;
    @Column(name="content_revision",nullable=false) private int contentRevision; @Column(name="lifecycle_revision",nullable=false) private int lifecycleRevision; @Column(name="route_revision") private Integer routeRevision;
    @Column(name="action_reason",length=2000) private String reason; @Column(name="override_milestone",length=80) private String overrideMilestone; @Column(name="acted_at",nullable=false) private Instant actedAt;
    protected PerformanceCommitmentAction() {}
    public PerformanceCommitmentAction(String agency,String version,String route,String step,Type type,String key,String actor,String before,String after,int content,int lifecycle,Integer routeRevision,String reason,String overrideMilestone,Instant at){super(agency);commitmentVersionId=requiredText(version,"commitmentVersionId");routeId=optionalText(route);routeStepId=optionalText(step);this.type=type;requestKey=requiredText(key,"requestKey");actorEmployeeNo=requiredText(actor,"actorEmployeeNo");priorStatus=requiredText(before,"priorStatus");resultStatus=requiredText(after,"resultStatus");contentRevision=content;lifecycleRevision=lifecycle;this.routeRevision=routeRevision;this.reason=optionalText(reason);this.overrideMilestone=optionalText(overrideMilestone);actedAt=at;}
    public String getCommitmentVersionId(){return commitmentVersionId;} public String getRouteId(){return routeId;} public String getRouteStepId(){return routeStepId;} public Type getType(){return type;} public String getRequestKey(){return requestKey;} public String getActorEmployeeNo(){return actorEmployeeNo;} public String getPriorStatus(){return priorStatus;} public String getResultStatus(){return resultStatus;} public int getContentRevision(){return contentRevision;} public int getLifecycleRevision(){return lifecycleRevision;} public Integer getRouteRevision(){return routeRevision;} public String getReason(){return reason;} public String getOverrideMilestone(){return overrideMilestone;} public Instant getActedAt(){return actedAt;}
}
