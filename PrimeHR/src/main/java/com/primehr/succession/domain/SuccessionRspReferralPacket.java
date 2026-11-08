package com.primehr.succession.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import com.primehr.succession.application.RspReferralSourcePort.VacancySnapshot;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="succession_rsp_referral_packet",uniqueConstraints={
        @UniqueConstraint(name="uk_succ_rsp_referral_code",columnNames={"agency_id","referral_code"}),
        @UniqueConstraint(name="uk_succ_rsp_referral_bench_vacancy",columnNames={"agency_id","bench_entry_id","vacancy_publication_id"})})
public class SuccessionRspReferralPacket extends RspAuditedEntity {
    public enum Status { DRAFT, AUTHORIZED, SENT, RECEIVED, UNDER_REVIEW, CLOSED, DECLINED, WITHDRAWN }
    @Column(name="referral_code",nullable=false,length=40) private String referralCode;
    @Column(name="continuity_plan_id",nullable=false,length=36) private String continuityPlanId;
    @Column(name="bench_entry_id",nullable=false,length=36) private String benchEntryId;
    @Column(name="candidate_id",nullable=false,length=36) private String candidateId;
    @Column(name="pool_decision_id",nullable=false,length=36) private String poolDecisionId;
    @Column(name="membership_event_id",nullable=false,length=36) private String membershipEventId;
    @Column(name="employee_no",nullable=false,length=100) private String employeeNo;
    @Column(name="employee_name",nullable=false,length=300) private String employeeName;
    @Column(name="readiness_level_code",nullable=false,length=80) private String readinessLevelCode;
    @Column(name="target_job_position_id",nullable=false) private Long targetJobPositionId;
    @Column(name="target_position_name",nullable=false,length=250) private String targetPositionName;
    @Column(name="target_plantilla_id") private Long targetPlantillaId;
    @Column(name="target_plantilla_name",length=250) private String targetPlantillaName;
    @Column(name="membership_expires_on",nullable=false) private LocalDate membershipExpiresOn;
    @Column(name="referral_reason",nullable=false,length=4000) private String referralReason;
    @Column(name="employee_visible_notice",nullable=false,length=4000) private String employeeVisibleNotice;
    @Column(name="restricted_rationale",nullable=false,length=4000) private String restrictedRationale;
    @Column(name="vacancy_publication_id",length=36) private String vacancyPublicationId;
    @Column(name="vacancy_request_id",length=36) private String vacancyRequestId;
    @Column(name="vacancy_status",length=30) private String vacancyStatus;
    @Column(name="vacancy_record_version") private Long vacancyRecordVersion;
    @Column(name="vacancy_fingerprint",length=200) private String vacancyFingerprint;
    @Column(name="vacancy_fetched_at") private Instant vacancyFetchedAt;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private Status status;
    @Column(name="created_by_actor",nullable=false,length=100) private String createdByActor;
    @Column(name="authorized_by",length=100) private String authorizedBy;
    @Column(name="authorized_at") private Instant authorizedAt;
    @Column(name="sent_by",length=100) private String sentBy;
    @Column(name="sent_at") private Instant sentAt;
    @Column(name="received_by",length=100) private String receivedBy;
    @Column(name="received_at") private Instant receivedAt;
    @Column(name="status_reason",length=4000) private String statusReason;

    protected SuccessionRspReferralPacket() { }
    public SuccessionRspReferralPacket(String agency,SuccessionContinuityPlan plan,SuccessionBenchCoverageEntry bench,
                                       String reason,String notice,String restricted,String actor){super(agency);
        if(plan.getStatus()!=SuccessionContinuityPlan.Status.FINALIZED)throw new IllegalArgumentException("A finalized continuity plan is required");
        referralCode="SR-"+UUID.randomUUID().toString().substring(0,12).toUpperCase();continuityPlanId=plan.getId();benchEntryId=bench.getId();candidateId=bench.getCandidateId();poolDecisionId=bench.getPoolDecisionId();membershipEventId=bench.getMembershipEventId();employeeNo=requiredText(bench.getEmployeeNo(),"employeeNo");employeeName=requiredText(bench.getEmployeeName(),"employeeName");readinessLevelCode=requiredText(bench.getReadinessLevelCode(),"readinessLevelCode");targetJobPositionId=plan.getJobPositionId();targetPositionName=requiredText(plan.getPositionName(),"targetPositionName");targetPlantillaId=plan.getPlantillaId();targetPlantillaName=optionalText(plan.getPlantillaName());membershipExpiresOn=bench.getMembershipExpiresOn();referralReason=requiredText(reason,"referralReason");employeeVisibleNotice=requiredText(notice,"employeeVisibleNotice");restrictedRationale=requiredText(restricted,"restrictedRationale");createdByActor=requiredText(actor,"actor");status=Status.DRAFT;}
    public void updateNarrative(String reason,String notice,String restricted){require(Status.DRAFT);referralReason=requiredText(reason,"referralReason");employeeVisibleNotice=requiredText(notice,"employeeVisibleNotice");restrictedRationale=requiredText(restricted,"restrictedRationale");}
    public void linkVacancy(VacancySnapshot v){require(Status.DRAFT);vacancyPublicationId=v.publicationId();vacancyRequestId=v.vacancyRequestId();vacancyStatus=v.status();vacancyRecordVersion=v.recordVersion();vacancyFingerprint=v.sourceFingerprint();vacancyFetchedAt=v.fetchedAt();}
    public void authorize(String actor,String reason,Instant at){require(Status.DRAFT);if(actor.equalsIgnoreCase(createdByActor))throw new IllegalArgumentException("Referral creator cannot authorize the same packet");status=Status.AUTHORIZED;authorizedBy=requiredText(actor,"actor");authorizedAt=at;statusReason=requiredText(reason,"reason");}
    public void send(String actor,String reason,Instant at){require(Status.AUTHORIZED);status=Status.SENT;sentBy=requiredText(actor,"actor");sentAt=at;statusReason=requiredText(reason,"reason");}
    public void receive(String actor,String reason,Instant at){require(Status.SENT);status=Status.RECEIVED;receivedBy=requiredText(actor,"actor");receivedAt=at;statusReason=requiredText(reason,"reason");}
    public void updateRspStatus(Status next,String reason){if(status!=Status.RECEIVED&&status!=Status.UNDER_REVIEW)throw new IllegalLifecycleTransitionException("Only a received referral may be reviewed or closed");if(next!=Status.UNDER_REVIEW&&next!=Status.CLOSED&&next!=Status.DECLINED)throw new IllegalArgumentException("Unsupported RSP referral status");status=next;statusReason=requiredText(reason,"reason");}
    public void withdraw(String reason){if(status!=Status.DRAFT&&status!=Status.AUTHORIZED&&status!=Status.SENT)throw new IllegalLifecycleTransitionException("Only a draft, authorized, or sent referral may be withdrawn");status=Status.WITHDRAWN;statusReason=requiredText(reason,"reason");}
    private void require(Status expected){if(status!=expected)throw new IllegalLifecycleTransitionException("Referral must be "+expected+" for this action");}
    public String getReferralCode(){return referralCode;}public String getContinuityPlanId(){return continuityPlanId;}public String getBenchEntryId(){return benchEntryId;}public String getCandidateId(){return candidateId;}public String getPoolDecisionId(){return poolDecisionId;}public String getMembershipEventId(){return membershipEventId;}public String getEmployeeNo(){return employeeNo;}public String getEmployeeName(){return employeeName;}public String getReadinessLevelCode(){return readinessLevelCode;}public Long getTargetJobPositionId(){return targetJobPositionId;}public String getTargetPositionName(){return targetPositionName;}public Long getTargetPlantillaId(){return targetPlantillaId;}public String getTargetPlantillaName(){return targetPlantillaName;}public LocalDate getMembershipExpiresOn(){return membershipExpiresOn;}public String getReferralReason(){return referralReason;}public String getEmployeeVisibleNotice(){return employeeVisibleNotice;}public String getRestrictedRationale(){return restrictedRationale;}public String getVacancyPublicationId(){return vacancyPublicationId;}public String getVacancyRequestId(){return vacancyRequestId;}public String getVacancyStatus(){return vacancyStatus;}public Long getVacancyRecordVersion(){return vacancyRecordVersion;}public String getVacancyFingerprint(){return vacancyFingerprint;}public Instant getVacancyFetchedAt(){return vacancyFetchedAt;}public Status getStatus(){return status;}public String getCreatedByActor(){return createdByActor;}public String getAuthorizedBy(){return authorizedBy;}public Instant getAuthorizedAt(){return authorizedAt;}public String getSentBy(){return sentBy;}public Instant getSentAt(){return sentAt;}public String getReceivedBy(){return receivedBy;}public Instant getReceivedAt(){return receivedAt;}public String getStatusReason(){return statusReason;}
}
