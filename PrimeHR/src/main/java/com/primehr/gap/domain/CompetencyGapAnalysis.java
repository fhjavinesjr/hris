package com.primehr.gap.domain;

import com.primehr.assessment.domain.PersonCompetencyProfile;
import com.primehr.integration.humanresource.HumanResourceAssessmentSubject;
import com.primehr.positionprofile.domain.PositionProfile;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "prime_competency_gap_analysis", uniqueConstraints = {
        @UniqueConstraint(name = "uk_prime_gap_analysis_request", columnNames = {"agency_id", "request_key"}),
        @UniqueConstraint(name = "uk_prime_gap_analysis_source", columnNames = {"agency_id", "subject_employee_id",
                "analysis_date", "position_profile_id", "person_profile_id", "priority_scheme_id"})
})
public class CompetencyGapAnalysis {
    @Id @Column(length = 36, nullable = false, updatable = false) private String id;
    @Column(name = "agency_id", length = 64, nullable = false, updatable = false) private String agencyId;
    @Column(name = "subject_employee_id", nullable = false, updatable = false) private Long subjectEmployeeId;
    @Column(name = "subject_employee_no", length = 100, nullable = false, updatable = false) private String subjectEmployeeNo;
    @Nationalized @Column(name = "subject_display_name", length = 300, nullable = false, updatable = false) private String subjectDisplayName;
    @Column(name = "appointment_id", nullable = false, updatable = false) private Long appointmentId;
    @Column(name = "job_position_id", nullable = false, updatable = false) private Long jobPositionId;
    @Column(name = "plantilla_id", updatable = false) private Long plantillaId;
    @Column(name = "hrm_source_fingerprint", length = 128, nullable = false, updatable = false) private String hrmSourceFingerprint;
    @Nationalized @Column(name = "source_job_position_name", length = 200, nullable = false, updatable = false) private String sourceJobPositionName;
    @Nationalized @Column(name = "source_plantilla_name", length = 200, updatable = false) private String sourcePlantillaName;
    @Column(name = "source_salary_grade", updatable = false) private Long sourceSalaryGrade;
    @Column(name = "source_salary_step", updatable = false) private Long sourceSalaryStep;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "position_profile_id", nullable = false, updatable = false) private PositionProfile positionProfile;
    @Column(name = "position_profile_definition_version", nullable = false, updatable = false) private int positionProfileDefinitionVersion;
    @Column(name = "position_profile_content_revision", nullable = false, updatable = false) private long positionProfileContentRevision;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "person_profile_id", nullable = false, updatable = false) private PersonCompetencyProfile personProfile;
    @Column(name = "person_profile_version", nullable = false, updatable = false) private int personProfileVersion;
    @Column(name = "person_profile_valid_from", nullable = false, updatable = false) private LocalDate personProfileValidFrom;
    @Column(name = "person_profile_valid_to", updatable = false) private LocalDate personProfileValidTo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "priority_scheme_id", nullable = false, updatable = false) private GapPriorityScheme priorityScheme;
    @Column(name = "priority_scheme_definition_version", nullable = false, updatable = false) private int prioritySchemeDefinitionVersion;
    @Column(name = "analysis_date", nullable = false, updatable = false) private LocalDate analysisDate;
    @Column(name = "request_key", length = 100, nullable = false, updatable = false) private String requestKey;
    @Column(name = "generated_by", length = 100, nullable = false, updatable = false) private String generatedBy;
    @Column(name = "generated_at", nullable = false, updatable = false) private Instant generatedAt;
    @Version @Column(name = "record_version", nullable = false) private long version;
    @CreatedBy @Column(name = "created_by", length = 100, nullable = false, updatable = false) private String createdBy;
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @LastModifiedBy @Column(name = "updated_by", length = 100, nullable = false) private String updatedBy;
    @LastModifiedDate @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected CompetencyGapAnalysis() { }

    public CompetencyGapAnalysis(String agencyId, HumanResourceAssessmentSubject subject,
                                 PositionProfile positionProfile, PersonCompetencyProfile personProfile,
                                 GapPriorityScheme priorityScheme, LocalDate analysisDate,
                                 String requestKey, String generatedBy, Instant generatedAt) {
        this.agencyId = requireText(agencyId, "agencyId");
        this.subjectEmployeeId = java.util.Objects.requireNonNull(subject.employeeId(), "employeeId");
        this.subjectEmployeeNo = requireText(subject.employeeNo(), "employeeNo");
        this.subjectDisplayName = requireText(subject.displayName(), "displayName");
        this.appointmentId = java.util.Objects.requireNonNull(subject.appointmentId(), "appointmentId");
        this.jobPositionId = java.util.Objects.requireNonNull(subject.jobPositionId(), "jobPositionId");
        this.plantillaId = subject.plantillaId();
        this.hrmSourceFingerprint = requireText(subject.sourceFingerprint(), "sourceFingerprint");
        this.positionProfile = java.util.Objects.requireNonNull(positionProfile, "positionProfile");
        this.sourceJobPositionName = requireText(positionProfile.getSourceJobPositionName(), "sourceJobPositionName");
        this.sourcePlantillaName = normalize(positionProfile.getSourcePlantillaName());
        this.sourceSalaryGrade = positionProfile.getSourceSalaryGrade();
        this.sourceSalaryStep = positionProfile.getSourceSalaryStep();
        this.positionProfileDefinitionVersion = positionProfile.getDefinitionVersion();
        this.positionProfileContentRevision = positionProfile.getContentRevision();
        this.personProfile = java.util.Objects.requireNonNull(personProfile, "personProfile");
        this.personProfileVersion = personProfile.getProfileVersion();
        this.personProfileValidFrom = personProfile.getValidFrom();
        this.personProfileValidTo = personProfile.getValidTo();
        this.priorityScheme = java.util.Objects.requireNonNull(priorityScheme, "priorityScheme");
        this.prioritySchemeDefinitionVersion = priorityScheme.getDefinitionVersion();
        this.analysisDate = java.util.Objects.requireNonNull(analysisDate, "analysisDate");
        this.requestKey = requireText(requestKey, "requestKey");
        this.generatedBy = requireText(generatedBy, "generatedBy");
        this.generatedAt = java.util.Objects.requireNonNull(generatedAt, "generatedAt");
        if (!agencyId.equals(positionProfile.getAgencyId()) || !agencyId.equals(personProfile.getAgencyId())
                || !agencyId.equals(priorityScheme.getAgencyId())) {
            throw new IllegalArgumentException("Gap analysis sources must use the same agency");
        }
    }

    @PrePersist void assignId() { if (id == null) id = UUID.randomUUID().toString(); }
    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public String getId() { return id; }
    public String getAgencyId() { return agencyId; }
    public Long getSubjectEmployeeId() { return subjectEmployeeId; }
    public String getSubjectEmployeeNo() { return subjectEmployeeNo; }
    public String getSubjectDisplayName() { return subjectDisplayName; }
    public Long getAppointmentId() { return appointmentId; }
    public Long getJobPositionId() { return jobPositionId; }
    public Long getPlantillaId() { return plantillaId; }
    public String getHrmSourceFingerprint() { return hrmSourceFingerprint; }
    public String getSourceJobPositionName() { return sourceJobPositionName; }
    public String getSourcePlantillaName() { return sourcePlantillaName; }
    public Long getSourceSalaryGrade() { return sourceSalaryGrade; }
    public Long getSourceSalaryStep() { return sourceSalaryStep; }
    public PositionProfile getPositionProfile() { return positionProfile; }
    public int getPositionProfileDefinitionVersion() { return positionProfileDefinitionVersion; }
    public long getPositionProfileContentRevision() { return positionProfileContentRevision; }
    public PersonCompetencyProfile getPersonProfile() { return personProfile; }
    public int getPersonProfileVersion() { return personProfileVersion; }
    public LocalDate getPersonProfileValidFrom() { return personProfileValidFrom; }
    public LocalDate getPersonProfileValidTo() { return personProfileValidTo; }
    public GapPriorityScheme getPriorityScheme() { return priorityScheme; }
    public int getPrioritySchemeDefinitionVersion() { return prioritySchemeDefinitionVersion; }
    public LocalDate getAnalysisDate() { return analysisDate; }
    public String getRequestKey() { return requestKey; }
    public String getGeneratedBy() { return generatedBy; }
    public Instant getGeneratedAt() { return generatedAt; }
    public long getVersion() { return version; }
}
