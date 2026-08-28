package com.primehr.assessment.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.*;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name="prime_person_competency_profile", uniqueConstraints={
        @UniqueConstraint(name="uk_prime_person_profile_case", columnNames="assessment_case_id"),
        @UniqueConstraint(name="uk_prime_person_profile_version", columnNames={"agency_id","subject_employee_id","profile_version"})})
public class PersonCompetencyProfile {
    @Id @Column(length=36,nullable=false,updatable=false) private String id;
    @Column(name="agency_id",length=64,nullable=false,updatable=false) private String agencyId;
    @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="assessment_case_id",nullable=false,updatable=false) private AssessmentCase assessmentCase;
    @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="validation_id",nullable=false,updatable=false,unique=true) private AssessmentValidation validation;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="predecessor_id",updatable=false) private PersonCompetencyProfile predecessor;
    @Column(name="subject_employee_id",nullable=false,updatable=false) private Long subjectEmployeeId;
    @Column(name="subject_employee_no",length=100,nullable=false,updatable=false) private String subjectEmployeeNo;
    @Nationalized @Column(name="subject_display_name",length=300,nullable=false,updatable=false) private String subjectDisplayName;
    @Column(name="appointment_id",nullable=false,updatable=false) private Long appointmentId;
    @Column(name="job_position_id",nullable=false,updatable=false) private Long jobPositionId;
    @Column(name="plantilla_id",updatable=false) private Long plantillaId;
    @Column(name="cycle_id",length=36,nullable=false,updatable=false) private String cycleId;
    @Column(name="tool_id",length=36,nullable=false,updatable=false) private String toolId;
    @Column(name="position_profile_id",length=36,nullable=false,updatable=false) private String positionProfileId;
    @Column(name="position_profile_definition_version",nullable=false,updatable=false) private int positionProfileDefinitionVersion;
    @Column(name="position_profile_content_revision",nullable=false,updatable=false) private long positionProfileContentRevision;
    @Column(name="profile_version",nullable=false,updatable=false) private int profileVersion;
    @Column(name="valid_from",nullable=false,updatable=false) private LocalDate validFrom;
    @Column(name="valid_to") private LocalDate validTo;
    @Column(name="reassessment_date",updatable=false) private LocalDate reassessmentDate;
    @Column(name="source_method_summary",length=1000,nullable=false,updatable=false) private String sourceMethodSummary;
    @Column(name="status",length=30,nullable=false,updatable=false) private String status;
    @Column(name="validated_at",nullable=false,updatable=false) private Instant validatedAt;
    @Column(nullable=false,updatable=false) private boolean active;
    @Version @Column(name="record_version",nullable=false) private long version;
    @CreatedBy @Column(name="created_by",length=100,nullable=false,updatable=false) private String createdBy;
    @CreatedDate @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @LastModifiedBy @Column(name="updated_by",length=100,nullable=false) private String updatedBy;
    @LastModifiedDate @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected PersonCompetencyProfile() { }
    public PersonCompetencyProfile(String agencyId, AssessmentCase assessmentCase, AssessmentValidation validation,
            PersonCompetencyProfile predecessor, int profileVersion, LocalDate validFrom,
            LocalDate reassessmentDate, String sourceMethodSummary) {
        if(!agencyId.equals(assessmentCase.getAgencyId())||!agencyId.equals(validation.getAgencyId())) throw new IllegalArgumentException("Person profile agency does not match");
        if(profileVersion<1) throw new IllegalArgumentException("profileVersion must be positive");
        this.agencyId=agencyId;this.assessmentCase=assessmentCase;this.validation=validation;this.predecessor=predecessor;
        this.subjectEmployeeId=assessmentCase.getSubjectEmployeeId();this.subjectEmployeeNo=assessmentCase.getSubjectEmployeeNo();this.subjectDisplayName=assessmentCase.getSubjectDisplayName();
        this.appointmentId=assessmentCase.getAppointmentId();this.jobPositionId=assessmentCase.getJobPositionId();this.plantillaId=assessmentCase.getPlantillaId();
        this.cycleId=assessmentCase.getTool().getCycle().getId();this.toolId=assessmentCase.getTool().getId();this.positionProfileId=assessmentCase.getTool().getPositionProfile().getId();
        this.positionProfileDefinitionVersion=assessmentCase.getTool().getProfileDefinitionVersion();this.positionProfileContentRevision=assessmentCase.getTool().getProfileContentRevision();
        this.profileVersion=profileVersion;this.validFrom=java.util.Objects.requireNonNull(validFrom);this.reassessmentDate=reassessmentDate;
        if(reassessmentDate!=null&&!reassessmentDate.isAfter(validFrom)) throw new IllegalArgumentException("reassessmentDate must be after validFrom");
        this.sourceMethodSummary=sourceMethodSummary;this.status="VALIDATED";this.validatedAt=validation.getValidatedAt();this.active=true;
    }
    public void closeBefore(LocalDate successorFrom){if(validTo!=null)throw new IllegalArgumentException("Predecessor is already closed");if(!successorFrom.isAfter(validFrom))throw new IllegalArgumentException("Successor validFrom must be after predecessor validFrom");validTo=successorFrom.minusDays(1);}
    @PrePersist void assignId(){if(id==null)id=UUID.randomUUID().toString();}
    public String getId(){return id;} public String getAgencyId(){return agencyId;} public AssessmentCase getAssessmentCase(){return assessmentCase;}
    public AssessmentValidation getValidation(){return validation;} public PersonCompetencyProfile getPredecessor(){return predecessor;}
    public Long getSubjectEmployeeId(){return subjectEmployeeId;} public String getSubjectEmployeeNo(){return subjectEmployeeNo;} public String getSubjectDisplayName(){return subjectDisplayName;}
    public Long getAppointmentId(){return appointmentId;} public Long getJobPositionId(){return jobPositionId;} public Long getPlantillaId(){return plantillaId;}
    public String getCycleId(){return cycleId;} public String getToolId(){return toolId;} public String getPositionProfileId(){return positionProfileId;}
    public int getPositionProfileDefinitionVersion(){return positionProfileDefinitionVersion;} public long getPositionProfileContentRevision(){return positionProfileContentRevision;}
    public int getProfileVersion(){return profileVersion;} public LocalDate getValidFrom(){return validFrom;} public LocalDate getValidTo(){return validTo;}
    public LocalDate getReassessmentDate(){return reassessmentDate;} public String getSourceMethodSummary(){return sourceMethodSummary;}
    public String getStatus(){return status;} public Instant getValidatedAt(){return validatedAt;} public long getVersion(){return version;}
}
