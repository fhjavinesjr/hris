package com.primehr.performancemanagement.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.time.*;
import java.util.Objects;

@Entity @Table(name="spms_coaching_session", uniqueConstraints=@UniqueConstraint(name="uk_spms_coaching_revision",columnNames={"agency_id","root_session_id","revision_no"}))
public class PerformanceCoachingSession extends RspAuditedEntity {
 public enum Status { DRAFT, ISSUED, ACKNOWLEDGED, SUPERSEDED, VOIDED }
 @Column(name="monitoring_case_id",nullable=false,length=36)private String monitoringCaseId;
 @Column(name="root_session_id",nullable=false,length=36)private String rootSessionId;
 @Column(name="revision_no",nullable=false)private int revisionNo;
 @Column(name="supersedes_id",length=36)private String supersedesId;
 @Column(name="session_at",nullable=false)private Instant sessionAt;
 @Column(nullable=false,length=2000)private String agenda;
 @Column(nullable=false,length=2000)private String goal;
 @Column(name="observed_issue",nullable=false,length=3000)private String observedIssue;
 @Column(name="agreed_action",nullable=false,length=3000)private String agreedAction;
 @Column(name="resources_support",length=3000)private String resourcesSupport;
 @Column(name="action_due_date")private LocalDate actionDueDate;
 @Column(name="next_meeting_at")private Instant nextMeetingAt;
 @Column(name="employee_visible_feedback",length=3000)private String employeeVisibleFeedback;
 @Column(name="private_notes",length=3000)private String privateNotes;
 @Column(name="employee_response",length=3000)private String employeeResponse;
 @Enumerated(EnumType.STRING)@Column(nullable=false,length=20)private Status status;
 @Column(name="issued_by",length=100)private String issuedBy;@Column(name="issued_at")private Instant issuedAt;
 @Column(name="acknowledged_by",length=100)private String acknowledgedBy;@Column(name="acknowledged_at")private Instant acknowledgedAt;
 @Column(name="void_reason",length=2000)private String voidReason;
 protected PerformanceCoachingSession(){}
 public PerformanceCoachingSession(String agency,String caseId,String root,int revision,String prior,Instant at,String agenda,String goal,String issue,String action,String resources,LocalDate due,Instant next,String visible,String privateNotes){super(agency);monitoringCaseId=requiredText(caseId,"monitoringCaseId");rootSessionId=requiredText(root,"rootSessionId");if(revision<1)throw new IllegalArgumentException("revisionNo must be positive");revisionNo=revision;supersedesId=optionalText(prior);status=Status.DRAFT;edit(at,agenda,goal,issue,action,resources,due,next,visible,privateNotes);}
 public void edit(Instant at,String agenda,String goal,String issue,String action,String resources,LocalDate due,Instant next,String visible,String notes){if(status!=Status.DRAFT)throw new IllegalStateException("Only draft coaching sessions may be edited");sessionAt=Objects.requireNonNull(at,"sessionAt");this.agenda=requiredText(agenda,"agenda");this.goal=requiredText(goal,"goal");observedIssue=requiredText(issue,"observedIssue");agreedAction=requiredText(action,"agreedAction");resourcesSupport=optionalText(resources);actionDueDate=due;nextMeetingAt=next;employeeVisibleFeedback=optionalText(visible);privateNotes=optionalText(notes);}
 public void issue(String actor,Instant at){if(status!=Status.DRAFT)throw new IllegalStateException("Only a draft coaching session may be issued");status=Status.ISSUED;issuedBy=requiredText(actor,"actor");issuedAt=Objects.requireNonNull(at);}
 public void acknowledge(String actor,String response,Instant at){if(status!=Status.ISSUED)throw new IllegalStateException("Only an issued coaching session may be acknowledged");status=Status.ACKNOWLEDGED;acknowledgedBy=requiredText(actor,"actor");acknowledgedAt=Objects.requireNonNull(at);employeeResponse=optionalText(response);}
 public void supersede(){if(status==Status.VOIDED||status==Status.SUPERSEDED)throw new IllegalStateException("Coaching session is not correctable");status=Status.SUPERSEDED;}
 public void voided(String reason){if(status==Status.VOIDED||status==Status.SUPERSEDED)throw new IllegalStateException("Coaching session is not voidable");String validated=requiredText(reason,"reason");status=Status.VOIDED;voidReason=validated;}
 public String getMonitoringCaseId(){return monitoringCaseId;}public String getRootSessionId(){return rootSessionId;}public int getRevisionNo(){return revisionNo;}public String getSupersedesId(){return supersedesId;}public Instant getSessionAt(){return sessionAt;}public String getAgenda(){return agenda;}public String getGoal(){return goal;}public String getObservedIssue(){return observedIssue;}public String getAgreedAction(){return agreedAction;}public String getResourcesSupport(){return resourcesSupport;}public LocalDate getActionDueDate(){return actionDueDate;}public Instant getNextMeetingAt(){return nextMeetingAt;}public String getEmployeeVisibleFeedback(){return employeeVisibleFeedback;}public String getPrivateNotes(){return privateNotes;}public String getEmployeeResponse(){return employeeResponse;}public Status getStatus(){return status;}public String getIssuedBy(){return issuedBy;}public Instant getIssuedAt(){return issuedAt;}public String getAcknowledgedBy(){return acknowledgedBy;}public Instant getAcknowledgedAt(){return acknowledgedAt;}public String getVoidReason(){return voidReason;}
}
