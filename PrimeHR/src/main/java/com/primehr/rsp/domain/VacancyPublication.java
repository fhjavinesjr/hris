package com.primehr.rsp.domain;

import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "rsp_vacancy_publication", uniqueConstraints =
        @UniqueConstraint(name = "uk_rsp_publication_vacancy", columnNames = "vacancy_request_id"))
public class VacancyPublication extends RspAuditedEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vacancy_request_id", nullable = false)
    private VacancyRequest vacancyRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VacancyPublicationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VacancyVisibility visibility;

    @Column(name = "opening_date", nullable = false) private LocalDate openingDate;
    @Column(name = "closing_date", nullable = false) private LocalDate closingDate;
    @Column(name = "instructions", nullable = false, length = 4000) private String instructions;
    @Column(name = "place_of_assignment", nullable = false, length = 300) private String placeOfAssignment;
    @Column(name = "contact_guidance", nullable = false, length = 2000) private String contactGuidance;
    @Column(name = "notice_text", nullable = false, length = 4000) private String noticeText;

    @Column(name = "plantilla_id", nullable = false) private Long plantillaId;
    @Column(name = "plantilla_name", nullable = false, length = 200) private String plantillaName;
    @Column(name = "job_position_id", nullable = false) private Long jobPositionId;
    @Column(name = "job_position_name", nullable = false, length = 200) private String jobPositionName;
    @Column(name = "salary_grade") private Long salaryGrade;
    @Column(name = "salary_step") private Long salaryStep;
    @Column(name = "business_unit_id", nullable = false) private Long businessUnitId;
    @Column(name = "business_unit_code", length = 100) private String businessUnitCode;
    @Column(name = "business_unit_name", nullable = false, length = 300) private String businessUnitName;

    @Column(name = "qualification_standard_id", nullable = false) private Long qualificationStandardId;
    @Column(name = "qualification_standard_version", nullable = false) private int qualificationStandardVersion;
    @Column(name = "education_requirement", nullable = false, length = 2000) private String educationRequirement;
    @Column(name = "training_requirement", nullable = false, length = 2000) private String trainingRequirement;
    @Column(name = "experience_requirement", nullable = false, length = 2000) private String experienceRequirement;
    @Column(name = "eligibility_requirement", nullable = false, length = 2000) private String eligibilityRequirement;
    @Column(name = "license_requirement", length = 2000) private String licenseRequirement;
    @Column(name = "qualification_source_basis", length = 1000) private String qualificationSourceBasis;

    @Column(name = "position_profile_id", nullable = false, length = 36) private String positionProfileId;
    @Column(name = "position_profile_definition_version", nullable = false) private int positionProfileDefinitionVersion;
    @Column(name = "position_profile_record_revision", nullable = false) private long positionProfileRecordRevision;
    @Column(name = "administrative_fingerprint", nullable = false, length = 64) private String administrativeFingerprint;
    @Column(name = "hrm_fingerprint", nullable = false, length = 64) private String hrmFingerprint;
    @Column(name = "source_snapshot_at", nullable = false) private Instant sourceSnapshotAt;

    @Column(name = "submitted_by", length = 100) private String submittedBy;
    @Column(name = "submitted_at") private Instant submittedAt;
    @Column(name = "approved_by", length = 100) private String approvedBy;
    @Column(name = "approved_at") private Instant approvedAt;
    @Column(name = "published_by", length = 100) private String publishedBy;
    @Column(name = "published_at") private Instant publishedAt;
    @Column(name = "closed_by", length = 100) private String closedBy;
    @Column(name = "closed_at") private Instant closedAt;
    @Column(name = "cancelled_by", length = 100) private String cancelledBy;
    @Column(name = "cancelled_at") private Instant cancelledAt;

    protected VacancyPublication() {
    }

    public VacancyPublication(String agencyId, VacancyRequest vacancyRequest, VacancyVisibility visibility,
                              LocalDate openingDate, LocalDate closingDate, String instructions,
                              String contactGuidance, String noticeText, Instant snapshotAt) {
        super(agencyId);
        this.vacancyRequest = Objects.requireNonNull(vacancyRequest, "vacancyRequest");
        if (vacancyRequest.getStatus() != VacancyRequestStatus.AUTHORIZED
                || !vacancyRequest.getPlan().isApproved()) {
            throw new IllegalLifecycleTransitionException(
                    "A publication draft requires an AUTHORIZED vacancy in an APPROVED plan");
        }
        snapshot(vacancyRequest, snapshotAt);
        applyDraft(visibility, openingDate, closingDate, instructions, contactGuidance, noticeText);
        this.status = VacancyPublicationStatus.DRAFT;
    }

    public void updateDraft(VacancyVisibility visibility, LocalDate openingDate, LocalDate closingDate,
                            String instructions, String contactGuidance, String noticeText) {
        requireEditable();
        applyDraft(visibility, openingDate, closingDate, instructions, contactGuidance, noticeText);
    }

    public void refreshSourceSnapshot(VacancyRequest vacancyRequest, Instant at) {
        requireEditable();
        if (!this.vacancyRequest.getId().equals(vacancyRequest.getId())) {
            throw new IllegalArgumentException("The publication vacancy request cannot change");
        }
        snapshot(vacancyRequest, at);
    }

    public void submit(String actor, Instant at) {
        requireEditable();
        submittedBy = text(actor, "submitter");
        submittedAt = Objects.requireNonNull(at, "submittedAt");
        approvedBy = null;
        approvedAt = null;
        status = VacancyPublicationStatus.SUBMITTED;
    }

    public void returnSubmission() {
        requireStatus(VacancyPublicationStatus.SUBMITTED);
        status = VacancyPublicationStatus.RETURNED;
        approvedBy = null;
        approvedAt = null;
    }

    public void approve(String actor, Instant at) {
        requireStatus(VacancyPublicationStatus.SUBMITTED);
        approvedBy = text(actor, "approver");
        approvedAt = Objects.requireNonNull(at, "approvedAt");
        status = VacancyPublicationStatus.APPROVED;
    }

    public void publish(String actor, Instant at) {
        requireStatus(VacancyPublicationStatus.APPROVED);
        publishedBy = text(actor, "publisher");
        publishedAt = Objects.requireNonNull(at, "publishedAt");
        status = VacancyPublicationStatus.PUBLISHED;
    }

    public void close(String actor, Instant at) {
        requireStatus(VacancyPublicationStatus.PUBLISHED);
        closedBy = text(actor, "closing actor");
        closedAt = Objects.requireNonNull(at, "closedAt");
        status = VacancyPublicationStatus.CLOSED;
    }

    public void cancel(String actor, Instant at) {
        if (status != VacancyPublicationStatus.APPROVED && status != VacancyPublicationStatus.PUBLISHED) {
            throw new IllegalLifecycleTransitionException(
                    "Only APPROVED or PUBLISHED vacancy publications can be cancelled");
        }
        cancelledBy = text(actor, "cancellation actor");
        cancelledAt = Objects.requireNonNull(at, "cancelledAt");
        status = VacancyPublicationStatus.CANCELLED;
    }

    public void requireEditable() {
        if (status != VacancyPublicationStatus.DRAFT && status != VacancyPublicationStatus.RETURNED) {
            throw new IllegalLifecycleTransitionException(
                    "Only DRAFT or RETURNED vacancy publications can be changed");
        }
    }

    private void applyDraft(VacancyVisibility visibility, LocalDate openingDate, LocalDate closingDate,
                            String instructions, String contactGuidance, String noticeText) {
        this.visibility = Objects.requireNonNull(visibility, "visibility");
        this.openingDate = Objects.requireNonNull(openingDate, "openingDate");
        this.closingDate = Objects.requireNonNull(closingDate, "closingDate");
        if (closingDate.isBefore(openingDate)) {
            throw new IllegalArgumentException("closingDate cannot be before openingDate");
        }
        this.instructions = text(instructions, "instructions");
        this.contactGuidance = text(contactGuidance, "contactGuidance");
        this.noticeText = text(noticeText, "noticeText");
    }

    private void snapshot(VacancyRequest vacancy, Instant at) {
        plantillaId = vacancy.getPlantillaId();
        plantillaName = vacancy.getPlantillaName();
        jobPositionId = vacancy.getJobPositionId();
        jobPositionName = vacancy.getJobPositionName();
        salaryGrade = vacancy.getSalaryGrade();
        salaryStep = vacancy.getSalaryStep();
        businessUnitId = vacancy.getBusinessUnitId();
        businessUnitCode = vacancy.getBusinessUnitCode();
        businessUnitName = vacancy.getBusinessUnitName();
        placeOfAssignment = vacancy.getBusinessUnitName();
        qualificationStandardId = vacancy.getQualificationStandardId();
        qualificationStandardVersion = vacancy.getQualificationStandardVersion();
        educationRequirement = vacancy.getEducationRequirement();
        trainingRequirement = vacancy.getTrainingRequirement();
        experienceRequirement = vacancy.getExperienceRequirement();
        eligibilityRequirement = vacancy.getEligibilityRequirement();
        licenseRequirement = vacancy.getLicenseRequirement();
        qualificationSourceBasis = vacancy.getQualificationSourceBasis();
        positionProfileId = vacancy.getPositionProfileId();
        positionProfileDefinitionVersion = vacancy.getPositionProfileDefinitionVersion();
        positionProfileRecordRevision = vacancy.getPositionProfileRecordRevision();
        administrativeFingerprint = vacancy.getAdministrativeFingerprint();
        hrmFingerprint = vacancy.getHrmFingerprint();
        sourceSnapshotAt = Objects.requireNonNull(at, "sourceSnapshotAt");
    }

    private void requireStatus(VacancyPublicationStatus expected) {
        if (status != expected) {
            throw new IllegalLifecycleTransitionException(
                    "Only " + expected + " vacancy publications may perform this action");
        }
    }

    public VacancyRequest getVacancyRequest() { return vacancyRequest; }
    public VacancyPublicationStatus getStatus() { return status; }
    public VacancyVisibility getVisibility() { return visibility; }
    public LocalDate getOpeningDate() { return openingDate; }
    public LocalDate getClosingDate() { return closingDate; }
    public String getInstructions() { return instructions; }
    public String getPlaceOfAssignment() { return placeOfAssignment; }
    public String getContactGuidance() { return contactGuidance; }
    public String getNoticeText() { return noticeText; }
    public Long getPlantillaId() { return plantillaId; }
    public String getPlantillaName() { return plantillaName; }
    public Long getJobPositionId() { return jobPositionId; }
    public String getJobPositionName() { return jobPositionName; }
    public Long getSalaryGrade() { return salaryGrade; }
    public Long getSalaryStep() { return salaryStep; }
    public Long getBusinessUnitId() { return businessUnitId; }
    public String getBusinessUnitCode() { return businessUnitCode; }
    public String getBusinessUnitName() { return businessUnitName; }
    public Long getQualificationStandardId() { return qualificationStandardId; }
    public int getQualificationStandardVersion() { return qualificationStandardVersion; }
    public String getEducationRequirement() { return educationRequirement; }
    public String getTrainingRequirement() { return trainingRequirement; }
    public String getExperienceRequirement() { return experienceRequirement; }
    public String getEligibilityRequirement() { return eligibilityRequirement; }
    public String getLicenseRequirement() { return licenseRequirement; }
    public String getQualificationSourceBasis() { return qualificationSourceBasis; }
    public String getPositionProfileId() { return positionProfileId; }
    public int getPositionProfileDefinitionVersion() { return positionProfileDefinitionVersion; }
    public long getPositionProfileRecordRevision() { return positionProfileRecordRevision; }
    public String getAdministrativeFingerprint() { return administrativeFingerprint; }
    public String getHrmFingerprint() { return hrmFingerprint; }
    public Instant getSourceSnapshotAt() { return sourceSnapshotAt; }
    public String getSubmittedBy() { return submittedBy; }
    public Instant getSubmittedAt() { return submittedAt; }
    public String getApprovedBy() { return approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
    public String getPublishedBy() { return publishedBy; }
    public Instant getPublishedAt() { return publishedAt; }
    public String getClosedBy() { return closedBy; }
    public Instant getClosedAt() { return closedAt; }
    public String getCancelledBy() { return cancelledBy; }
    public Instant getCancelledAt() { return cancelledAt; }
}
