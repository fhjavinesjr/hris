package com.primehr.rewardsrecognition.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="rr_nomination")
public class RrNomination extends RspAuditedEntity {
 public enum Status{DRAFT,SUBMITTED,WITHDRAWN}
 @Column(name="cycle_id",nullable=false,length=36)private String cycleId;
 @Column(name="category_version_id",nullable=false,length=36)private String categoryVersionId;
 @Column(name="category_definition_version",nullable=false)private int categoryDefinitionVersion;
 @Column(name="category_fingerprint",nullable=false,length=64)private String categoryFingerprint;
 @Enumerated(EnumType.STRING)@Column(name="nominee_type",nullable=false,length=30)private RrAwardCategoryVersion.NomineeType nomineeType;
 @Column(name="nominator_employee_id",nullable=false)private Long nominatorEmployeeId;
 @Column(name="nominator_employee_no",nullable=false,length=100)private String nominatorEmployeeNo;
 @Column(name="nominator_name",nullable=false,length=300)private String nominatorName;
 @Column(name="nominator_appointment_id",nullable=false)private Long nominatorAppointmentId;
 @Column(name="nominator_business_unit_id")private Long nominatorBusinessUnitId;
 @Column(name="nominator_fingerprint",nullable=false,length=64)private String nominatorFingerprint;
 @Column(name="individual_employee_id")private Long individualEmployeeId;
 @Column(name="individual_employee_no",length=100)private String individualEmployeeNo;
 @Column(name="individual_name",length=300)private String individualName;
 @Column(name="individual_appointment_id")private Long individualAppointmentId;
 @Column(name="individual_business_unit_id")private Long individualBusinessUnitId;
 @Column(name="individual_fingerprint",length=64)private String individualFingerprint;
 @Column(name="group_name",length=300)private String groupName;
 @Column(name="organization_type",length=30)private String organizationType;
 @Column(name="organization_id")private Long organizationId;
 @Column(name="organization_code",length=100)private String organizationCode;
 @Column(name="organization_name",length=300)private String organizationName;
 @Column(name="organization_fingerprint",length=64)private String organizationFingerprint;
 @Column(name="contribution_narrative",nullable=false,length=8000)private String contributionNarrative;
 @Column(name="committee_narrative",length=8000)private String committeeNarrative;
 @Column(name="publication_narrative",length=4000)private String publicationNarrative;
 @Enumerated(EnumType.STRING)@Column(nullable=false,length=20)private Status status;
 @Column(name="content_revision",nullable=false)private int contentRevision;
 @Column(name="submitted_by",length=100)private String submittedBy;
 @Column(name="submitted_at")private Instant submittedAt;
 @Column(name="submission_fingerprint",length=64)private String submissionFingerprint;
 @Column(name="withdrawn_by",length=100)private String withdrawnBy;
 @Column(name="withdrawn_at")private Instant withdrawnAt;
 @Column(name="withdrawal_reason",length=2000)private String withdrawalReason;
 protected RrNomination(){}
 public RrNomination(String agency,String cycle,String category,int categoryVersion,String categoryFingerprint,RrAwardCategoryVersion.NomineeType type,Long nominatorId,String nominatorNo,String nominatorName,Long nominatorAppointment,Long nominatorBusinessUnit,String nominatorFingerprint){super(UUID.randomUUID().toString(),agency);cycleId=requiredText(cycle,"cycleId");categoryVersionId=requiredText(category,"categoryVersionId");if(categoryVersion<1)throw new IllegalArgumentException("categoryDefinitionVersion must be positive");categoryDefinitionVersion=categoryVersion;this.categoryFingerprint=requiredText(categoryFingerprint,"categoryFingerprint");nomineeType=java.util.Objects.requireNonNull(type,"nomineeType");nominatorEmployeeId=java.util.Objects.requireNonNull(nominatorId,"nominatorEmployeeId");nominatorEmployeeNo=requiredText(nominatorNo,"nominatorEmployeeNo");this.nominatorName=requiredText(nominatorName,"nominatorName");nominatorAppointmentId=java.util.Objects.requireNonNull(nominatorAppointment,"nominatorAppointmentId");nominatorBusinessUnitId=nominatorBusinessUnit;this.nominatorFingerprint=requiredText(nominatorFingerprint,"nominatorFingerprint");status=Status.DRAFT;contentRevision=1;}
 public void individual(Long id,String no,String name,Long appointment,Long businessUnit,String fingerprint){draft();clearNominee();individualEmployeeId=java.util.Objects.requireNonNull(id,"employeeId");individualEmployeeNo=requiredText(no,"employeeNo");individualName=requiredText(name,"employeeName");individualAppointmentId=java.util.Objects.requireNonNull(appointment,"appointmentId");individualBusinessUnitId=businessUnit;individualFingerprint=requiredText(fingerprint,"employeeFingerprint");}
 public void group(String name){draft();clearNominee();groupName=requiredText(name,"groupName");}
 public void organization(String type,Long id,String code,String name,String fingerprint){draft();clearNominee();organizationType=requiredText(type,"organizationType");organizationId=java.util.Objects.requireNonNull(id,"organizationId");organizationCode=requiredText(code,"organizationCode");organizationName=requiredText(name,"organizationName");organizationFingerprint=requiredText(fingerprint,"organizationFingerprint");}
 public void narratives(String contribution,String committee,String publication){draft();contributionNarrative=requiredText(contribution,"contributionNarrative");committeeNarrative=optionalText(committee);publicationNarrative=optionalText(publication);contentRevision++;}
 public void submit(String actor,String fingerprint,Instant at){draft();status=Status.SUBMITTED;submittedBy=requiredText(actor,"actor");submissionFingerprint=requiredText(fingerprint,"submissionFingerprint");submittedAt=at;}
 public void withdraw(String actor,String reason,Instant at){if(status!=Status.DRAFT&&status!=Status.SUBMITTED)throw new IllegalLifecycleTransitionException("Only a draft or submitted nomination may be withdrawn");status=Status.WITHDRAWN;withdrawnBy=requiredText(actor,"actor");withdrawalReason=requiredText(reason,"reason");withdrawnAt=at;}
 private void clearNominee(){individualEmployeeId=null;individualEmployeeNo=null;individualName=null;individualAppointmentId=null;individualBusinessUnitId=null;individualFingerprint=null;groupName=null;organizationType=null;organizationId=null;organizationCode=null;organizationName=null;organizationFingerprint=null;}
 public void draft(){if(status!=Status.DRAFT)throw new IllegalLifecycleTransitionException("Submitted nominations are immutable; withdraw or use a later controlled return revision");}
 public String getCycleId(){return cycleId;}public String getCategoryVersionId(){return categoryVersionId;}public int getCategoryDefinitionVersion(){return categoryDefinitionVersion;}public String getCategoryFingerprint(){return categoryFingerprint;}public RrAwardCategoryVersion.NomineeType getNomineeType(){return nomineeType;}public Long getNominatorEmployeeId(){return nominatorEmployeeId;}public String getNominatorEmployeeNo(){return nominatorEmployeeNo;}public String getNominatorName(){return nominatorName;}public Long getNominatorAppointmentId(){return nominatorAppointmentId;}public Long getNominatorBusinessUnitId(){return nominatorBusinessUnitId;}public String getNominatorFingerprint(){return nominatorFingerprint;}public Long getIndividualEmployeeId(){return individualEmployeeId;}public String getIndividualEmployeeNo(){return individualEmployeeNo;}public String getIndividualName(){return individualName;}public Long getIndividualAppointmentId(){return individualAppointmentId;}public Long getIndividualBusinessUnitId(){return individualBusinessUnitId;}public String getIndividualFingerprint(){return individualFingerprint;}public String getGroupName(){return groupName;}public String getOrganizationType(){return organizationType;}public Long getOrganizationId(){return organizationId;}public String getOrganizationCode(){return organizationCode;}public String getOrganizationName(){return organizationName;}public String getOrganizationFingerprint(){return organizationFingerprint;}public String getContributionNarrative(){return contributionNarrative;}public String getCommitteeNarrative(){return committeeNarrative;}public String getPublicationNarrative(){return publicationNarrative;}public Status getStatus(){return status;}public int getContentRevision(){return contentRevision;}public String getSubmittedBy(){return submittedBy;}public Instant getSubmittedAt(){return submittedAt;}public String getSubmissionFingerprint(){return submissionFingerprint;}public String getWithdrawnBy(){return withdrawnBy;}public Instant getWithdrawnAt(){return withdrawnAt;}public String getWithdrawalReason(){return withdrawalReason;}
}
