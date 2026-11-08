package com.primehr.succession.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name="succession_bench_coverage_entry",uniqueConstraints=
        @UniqueConstraint(name="uk_succ_bench_plan_decision",columnNames={"agency_id","continuity_plan_id","pool_decision_id"}))
public class SuccessionBenchCoverageEntry extends RspAuditedEntity {
    @Column(name="continuity_plan_id",nullable=false,length=36) private String continuityPlanId;
    @Column(name="candidate_id",nullable=false,length=36) private String candidateId;
    @Column(name="pool_decision_id",nullable=false,length=36) private String poolDecisionId;
    @Column(name="membership_event_id",nullable=false,length=36) private String membershipEventId;
    @Column(name="employee_no",nullable=false,length=100) private String employeeNo;
    @Column(name="employee_name",nullable=false,length=300) private String employeeName;
    @Column(name="readiness_level_code",nullable=false,length=80) private String readinessLevelCode;
    @Column(name="membership_effective_on",nullable=false) private LocalDate membershipEffectiveOn;
    @Column(name="membership_expires_on",nullable=false) private LocalDate membershipExpiresOn;
    @Column(name="development_plan_id",length=36) private String developmentPlanId;
    @Column(name="development_status",length=20) private String developmentStatus;
    @Column(name="snapshot_at",nullable=false) private Instant snapshotAt;

    protected SuccessionBenchCoverageEntry() { }
    public SuccessionBenchCoverageEntry(String agency,String planId,SuccessionCandidate candidate,
                                        SuccessionPoolDecision decision,SuccessionPoolMembershipEvent membership,
                                        SuccessionDevelopmentPlan development,Instant at){super(agency);
        continuityPlanId=requiredText(planId,"continuityPlanId");candidateId=candidate.getId();poolDecisionId=decision.getId();membershipEventId=membership.getId();employeeNo=requiredText(candidate.getEmployeeNo(),"employeeNo");employeeName=requiredText(candidate.getEmployeeName(),"employeeName");readinessLevelCode=requiredText(decision.getReadinessLevelCode(),"readinessLevelCode");membershipEffectiveOn=membership.getEffectiveOn();membershipExpiresOn=membership.getExpiresOn();if(development!=null){developmentPlanId=development.getId();developmentStatus=development.getStatus().name();}snapshotAt=at;}
    public String getContinuityPlanId(){return continuityPlanId;}public String getCandidateId(){return candidateId;}public String getPoolDecisionId(){return poolDecisionId;}public String getMembershipEventId(){return membershipEventId;}public String getEmployeeNo(){return employeeNo;}public String getEmployeeName(){return employeeName;}public String getReadinessLevelCode(){return readinessLevelCode;}public LocalDate getMembershipEffectiveOn(){return membershipEffectiveOn;}public LocalDate getMembershipExpiresOn(){return membershipExpiresOn;}public String getDevelopmentPlanId(){return developmentPlanId;}public String getDevelopmentStatus(){return developmentStatus;}public Instant getSnapshotAt(){return snapshotAt;}
}
