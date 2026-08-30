package com.primehr.rsp.domain;

import com.primehr.integration.administrative.AdministrativeRspPositionSource;
import com.primehr.integration.humanresource.HumanResourcePlantillaOccupancy;
import com.primehr.positionprofile.api.PositionProfileResponse;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "rsp_vacancy_request", uniqueConstraints =
        @UniqueConstraint(name = "uk_rsp_vacancy_plan_plantilla", columnNames = {"plan_id", "plantilla_id"}))
public class VacancyRequest extends RspAuditedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id")
    private RecruitmentPlan plan;

    @Column(nullable = false)
    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VacancyRequestStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "vacancy_type", nullable = false, length = 30)
    private VacancyType vacancyType;

    @Column(name = "anticipated_vacancy_date") private LocalDate anticipatedVacancyDate;
    @Column(name = "anticipated_reason_code", length = 80) private String anticipatedReasonCode;
    @Column(name = "anticipated_explanation", length = 2000) private String anticipatedExplanation;
    @Column(name = "authority_reference", length = 500) private String authorityReference;
    @Column(name = "recruitment_priority", nullable = false, length = 80) private String recruitmentPriority;
    @Column(name = "target_fill_date") private LocalDate targetFillDate;
    @Column(nullable = false, length = 4000) private String justification;

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
    @Column(name = "administrative_fetched_at", nullable = false) private Instant administrativeFetchedAt;
    @Column(nullable = false) private boolean occupied;
    @Column(name = "active_appointment_id") private Long activeAppointmentId;
    @Column(name = "occupancy_assumption_date") private LocalDateTime occupancyAssumptionDate;
    @Column(name = "hrm_fingerprint", nullable = false, length = 64) private String hrmFingerprint;
    @Column(name = "hrm_fetched_at", nullable = false) private Instant hrmFetchedAt;

    @Column(name = "submitted_by", length = 100) private String submittedBy;
    @Column(name = "submitted_at") private Instant submittedAt;
    @Column(name = "decided_by", length = 100) private String decidedBy;
    @Column(name = "decided_at") private Instant decidedAt;

    protected VacancyRequest() {
    }

    public VacancyRequest(String agencyId, RecruitmentPlan plan, VacancyType vacancyType,
                          LocalDate anticipatedVacancyDate, String anticipatedReasonCode,
                          String anticipatedExplanation, String authorityReference,
                          String recruitmentPriority, LocalDate targetFillDate, String justification,
                          AdministrativeRspPositionSource administrative,
                          HumanResourcePlantillaOccupancy occupancy,
                          PositionProfileResponse profile) {
        super(agencyId);
        this.plan = Objects.requireNonNull(plan, "plan");
        this.active = true;
        this.status = VacancyRequestStatus.DRAFT;
        apply(vacancyType, anticipatedVacancyDate, anticipatedReasonCode, anticipatedExplanation,
                authorityReference, recruitmentPriority, targetFillDate, justification,
                administrative, occupancy, profile);
    }

    public void update(VacancyType vacancyType, LocalDate anticipatedVacancyDate,
                       String anticipatedReasonCode, String anticipatedExplanation,
                       String authorityReference, String recruitmentPriority,
                       LocalDate targetFillDate, String justification,
                       AdministrativeRspPositionSource administrative,
                       HumanResourcePlantillaOccupancy occupancy,
                       PositionProfileResponse profile) {
        requireEditable();
        apply(vacancyType, anticipatedVacancyDate, anticipatedReasonCode, anticipatedExplanation,
                authorityReference, recruitmentPriority, targetFillDate, justification,
                administrative, occupancy, profile);
    }

    public void archiveDraft() {
        requireEditable();
        active = false;
        status = VacancyRequestStatus.CANCELLED;
    }

    public void submit(String actor, Instant at) {
        requireEditable();
        if (plan.getStatus() != RecruitmentPlanStatus.DRAFT
                && plan.getStatus() != RecruitmentPlanStatus.RETURNED
                && plan.getStatus() != RecruitmentPlanStatus.APPROVED) {
            throw new IllegalLifecycleTransitionException(
                    "The recruitment plan does not allow vacancy submission");
        }
        submittedBy = text(actor, "submitter");
        submittedAt = Objects.requireNonNull(at, "submittedAt");
        decidedBy = null;
        decidedAt = null;
        status = VacancyRequestStatus.SUBMITTED;
    }

    public void returnSubmission() {
        requireStatus(VacancyRequestStatus.SUBMITTED);
        status = VacancyRequestStatus.RETURNED;
        decidedBy = null;
        decidedAt = null;
    }

    public void authorize(String actor, Instant at) {
        requireStatus(VacancyRequestStatus.SUBMITTED);
        if (!plan.isApproved()) {
            throw new IllegalLifecycleTransitionException(
                    "A vacancy can be authorized only within an APPROVED recruitment plan");
        }
        decidedBy = text(actor, "authorizer");
        decidedAt = Objects.requireNonNull(at, "decidedAt");
        status = VacancyRequestStatus.AUTHORIZED;
    }

    public void decline(String actor, Instant at) {
        requireStatus(VacancyRequestStatus.SUBMITTED);
        decidedBy = text(actor, "decision actor");
        decidedAt = Objects.requireNonNull(at, "decidedAt");
        active = false;
        status = VacancyRequestStatus.DECLINED;
    }

    public void cancel(String actor, Instant at) {
        if (status != VacancyRequestStatus.AUTHORIZED) {
            throw new IllegalLifecycleTransitionException(
                    "Only an AUTHORIZED vacancy request can be cancelled");
        }
        decidedBy = text(actor, "cancellation actor");
        decidedAt = Objects.requireNonNull(at, "decidedAt");
        active = false;
        status = VacancyRequestStatus.CANCELLED;
    }

    private void apply(VacancyType type, LocalDate date, String reason, String explanation,
                       String authority, String priority, LocalDate target, String justification,
                       AdministrativeRspPositionSource administrative,
                       HumanResourcePlantillaOccupancy occupancy,
                       PositionProfileResponse profile) {
        if (type == null) throw new IllegalArgumentException("vacancyType is required");
        if (administrative == null || occupancy == null || profile == null) {
            throw new IllegalArgumentException("authoritative sources are required");
        }
        if (!administrative.plantillaId().equals(occupancy.plantillaId())) {
            throw new IllegalArgumentException("source Plantilla mismatch");
        }
        if (type == VacancyType.ACTUAL) {
            if (occupancy.occupied()) {
                throw new IllegalArgumentException("An occupied Plantilla cannot be filed as ACTUAL");
            }
            date = null;
            reason = null;
            explanation = null;
        } else {
            if (date == null || reason == null || reason.isBlank()
                    || explanation == null || explanation.isBlank()) {
                throw new IllegalArgumentException(
                        "Anticipated vacancy date, reason, and explanation are required");
            }
            if (!date.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("anticipatedVacancyDate must be in the future");
            }
        }

        this.vacancyType = type;
        this.anticipatedVacancyDate = date;
        this.anticipatedReasonCode = optional(reason);
        this.anticipatedExplanation = optional(explanation);
        this.authorityReference = optional(authority);
        this.recruitmentPriority = text(priority, "recruitmentPriority");
        this.targetFillDate = target;
        this.justification = text(justification, "justification");
        applyAdministrative(administrative, profile);
        applyOccupancy(occupancy);
    }

    private void applyAdministrative(AdministrativeRspPositionSource source,
                                     PositionProfileResponse profile) {
        plantillaId = source.plantillaId();
        plantillaName = text(source.plantillaName(), "plantillaName");
        jobPositionId = source.jobPositionId();
        jobPositionName = text(source.jobPositionName(), "jobPositionName");
        salaryGrade = source.salaryGrade();
        salaryStep = source.salaryStep();
        businessUnitId = source.businessUnitId();
        businessUnitCode = optional(source.businessUnitCode());
        businessUnitName = text(source.businessUnitName(), "businessUnitName");
        qualificationStandardId = source.qualificationStandardId();
        qualificationStandardVersion = source.qualificationStandardVersion();
        educationRequirement = text(source.education(), "education");
        trainingRequirement = text(source.training(), "training");
        experienceRequirement = text(source.experience(), "experience");
        eligibilityRequirement = text(source.eligibility(), "eligibility");
        licenseRequirement = optional(source.licenseRequirement());
        qualificationSourceBasis = optional(source.sourceBasis());
        positionProfileId = profile.id();
        positionProfileDefinitionVersion = profile.definitionVersion();
        positionProfileRecordRevision = profile.contentRevision();
        administrativeFingerprint = text(source.sourceFingerprint(), "administrativeFingerprint");
        administrativeFetchedAt = Objects.requireNonNull(source.fetchedAt(), "administrativeFetchedAt");
    }

    private void applyOccupancy(HumanResourcePlantillaOccupancy source) {
        occupied = source.occupied();
        activeAppointmentId = source.activeAppointmentId();
        occupancyAssumptionDate = source.assumptionToDutyDate();
        hrmFingerprint = text(source.sourceFingerprint(), "hrmFingerprint");
        hrmFetchedAt = Objects.requireNonNull(source.fetchedAt(), "hrmFetchedAt");
    }

    private void requireEditable() {
        if (status != VacancyRequestStatus.DRAFT && status != VacancyRequestStatus.RETURNED) {
            throw new IllegalLifecycleTransitionException(
                    "Only DRAFT or RETURNED vacancy requests can be changed");
        }
    }

    private void requireStatus(VacancyRequestStatus expected) {
        if (status != expected) {
            throw new IllegalLifecycleTransitionException(
                    "Only " + expected + " vacancy requests may perform this action");
        }
    }

    public RecruitmentPlan getPlan() { return plan; }
    public boolean isActive() { return active; }
    public VacancyRequestStatus getStatus() { return status; }
    public VacancyType getVacancyType() { return vacancyType; }
    public LocalDate getAnticipatedVacancyDate() { return anticipatedVacancyDate; }
    public String getAnticipatedReasonCode() { return anticipatedReasonCode; }
    public String getAnticipatedExplanation() { return anticipatedExplanation; }
    public String getAuthorityReference() { return authorityReference; }
    public String getRecruitmentPriority() { return recruitmentPriority; }
    public LocalDate getTargetFillDate() { return targetFillDate; }
    public String getJustification() { return justification; }
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
    public Instant getAdministrativeFetchedAt() { return administrativeFetchedAt; }
    public boolean isOccupied() { return occupied; }
    public Long getActiveAppointmentId() { return activeAppointmentId; }
    public LocalDateTime getOccupancyAssumptionDate() { return occupancyAssumptionDate; }
    public String getHrmFingerprint() { return hrmFingerprint; }
    public Instant getHrmFetchedAt() { return hrmFetchedAt; }
    public String getSubmittedBy() { return submittedBy; }
    public Instant getSubmittedAt() { return submittedAt; }
    public String getDecidedBy() { return decidedBy; }
    public Instant getDecidedAt() { return decidedAt; }
}
