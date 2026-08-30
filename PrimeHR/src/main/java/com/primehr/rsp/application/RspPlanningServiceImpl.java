package com.primehr.rsp.application;

import com.primehr.integration.administrative.AdministrativeRspPositionSource;
import com.primehr.integration.administrative.AdministrativeRspPositionSourceClient;
import com.primehr.integration.humanresource.HumanResourcePlantillaOccupancy;
import com.primehr.integration.humanresource.HumanResourcePlantillaOccupancyClient;
import com.primehr.positionprofile.api.PositionProfileResolutionResponse;
import com.primehr.positionprofile.api.PositionProfileResponse;
import com.primehr.positionprofile.application.PositionProfileAdminService;
import com.primehr.rsp.api.RspPlanningDtos.CreatePlan;
import com.primehr.rsp.api.RspPlanningDtos.PlanResponse;
import com.primehr.rsp.api.RspPlanningDtos.Readiness;
import com.primehr.rsp.api.RspPlanningDtos.SaveVacancy;
import com.primehr.rsp.api.RspPlanningDtos.Transition;
import com.primehr.rsp.api.RspPlanningDtos.UpdatePlan;
import com.primehr.rsp.api.RspPlanningDtos.VacancyResponse;
import com.primehr.rsp.domain.RecruitmentPlan;
import com.primehr.rsp.domain.VacancyRequest;
import com.primehr.rsp.domain.VacancyRequestStatus;
import com.primehr.rsp.domain.VacancyType;
import com.primehr.rsp.infrastructure.RecruitmentPlanRepository;
import com.primehr.rsp.infrastructure.VacancyRequestRepository;
import com.primehr.shared.api.PageResponse;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.OptimisticConflictException;
import com.primehr.shared.exception.PublicationConflictException;
import com.primehr.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class RspPlanningServiceImpl implements RspPlanningService {
    private final RecruitmentPlanRepository plans;
    private final VacancyRequestRepository vacancies;
    private final AdministrativeRspPositionSourceClient administrative;
    private final HumanResourcePlantillaOccupancyClient humanResource;
    private final PositionProfileAdminService positionProfiles;
    private final PrimeHrAuditService audit;

    public RspPlanningServiceImpl(RecruitmentPlanRepository plans,
                                  VacancyRequestRepository vacancies,
                                  AdministrativeRspPositionSourceClient administrative,
                                  HumanResourcePlantillaOccupancyClient humanResource,
                                  PositionProfileAdminService positionProfiles,
                                  PrimeHrAuditService audit) {
        this.plans = plans;
        this.vacancies = vacancies;
        this.administrative = administrative;
        this.humanResource = humanResource;
        this.positionProfiles = positionProfiles;
        this.audit = audit;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PlanResponse> list(String agencyId, int page, int size) {
        validatePage(page, size);
        return PageResponse.from(plans.findByAgencyId(agencyId, PageRequest.of(page, size,
                Sort.by("periodStart").descending().and(Sort.by("code")))),
                plan -> response(plan, agencyId));
    }

    @Override
    @Transactional(readOnly = true)
    public PlanResponse get(String agencyId, String id) {
        return response(plan(agencyId, id), agencyId);
    }

    @Override
    public PlanResponse create(String agencyId, CreatePlan request, String correlationId) {
        if (plans.existsByAgencyIdAndCodeIgnoreCase(agencyId, request.code())) {
            throw new IllegalArgumentException("Recruitment plan code already exists");
        }
        RecruitmentPlan plan = plans.saveAndFlush(new RecruitmentPlan(agencyId, request.code(),
                request.title(), request.periodStart(), request.periodEnd(), request.description()));
        PlanResponse after = response(plan, agencyId);
        audit.record(agencyId, "CREATE_DRAFT", "RSP_RECRUITMENT_PLAN", plan.getId(), null,
                plan.getVersion(), null, after, null, correlationId);
        return after;
    }

    @Override
    public PlanResponse update(String agencyId, String id, UpdatePlan request, String correlationId) {
        RecruitmentPlan plan = plan(agencyId, id);
        requireVersion(plan.getVersion(), request.recordVersion());
        PlanResponse before = response(plan, agencyId);
        plan.update(request.title(), request.periodStart(), request.periodEnd(), request.description());
        requireNoDuplicateVacanciesAfterPeriodChange(agencyId, plan);
        plan = plans.saveAndFlush(plan);
        PlanResponse after = response(plan, agencyId);
        audit.record(agencyId, "UPDATE_DRAFT", "RSP_RECRUITMENT_PLAN", id, null,
                plan.getVersion(), before, after, null, correlationId);
        return after;
    }

    @Override
    public PlanResponse archive(String agencyId, String id, Transition request, String correlationId) {
        RecruitmentPlan plan = plan(agencyId, id);
        requireVersion(plan.getVersion(), request.recordVersion());
        if (activeVacancies(plan, agencyId).stream().findAny().isPresent()) {
            throw new IllegalArgumentException("Archive or cancel active vacancy requests first");
        }
        PlanResponse before = response(plan, agencyId);
        plan.archive();
        plan = plans.saveAndFlush(plan);
        PlanResponse after = response(plan, agencyId);
        audit.record(agencyId, "ARCHIVE_PLAN", "RSP_RECRUITMENT_PLAN", id, null,
                plan.getVersion(), before, after, requiredReason(request.reason()), correlationId);
        return after;
    }

    @Override
    public PlanResponse submitPlan(String agencyId, String id, Transition request, String token,
                                   String correlationId) {
        RecruitmentPlan plan = plan(agencyId, id);
        requireVersion(plan.getVersion(), request.recordVersion());
        List<VacancyRequest> active = activeVacancies(plan, agencyId);
        if (active.isEmpty()) {
            throw new IllegalArgumentException("A recruitment plan requires at least one vacancy request");
        }
        if (active.stream().anyMatch(item -> item.getStatus() != VacancyRequestStatus.SUBMITTED)) {
            throw new IllegalArgumentException(
                    "Every active vacancy request must be SUBMITTED before submitting the plan");
        }
        active.forEach(item -> requireSourcesUnchanged(agencyId, item, token));
        PlanResponse before = response(plan, agencyId);
        plan.submit(audit.currentActor(), Instant.now());
        plan = plans.saveAndFlush(plan);
        PlanResponse after = response(plan, agencyId);
        audit.record(agencyId, "SUBMIT_PLAN", "RSP_RECRUITMENT_PLAN", id, null,
                plan.getVersion(), before, after, normalizeReason(request.reason()), correlationId);
        return after;
    }

    @Override
    public PlanResponse returnPlan(String agencyId, String id, Transition request, String correlationId) {
        RecruitmentPlan plan = plan(agencyId, id);
        requireVersion(plan.getVersion(), request.recordVersion());
        PlanResponse before = response(plan, agencyId);
        plan.returnSubmission();
        plan = plans.saveAndFlush(plan);
        PlanResponse after = response(plan, agencyId);
        audit.record(agencyId, "RETURN_PLAN", "RSP_RECRUITMENT_PLAN", id, null,
                plan.getVersion(), before, after, requiredReason(request.reason()), correlationId);
        return after;
    }

    @Override
    public PlanResponse approvePlan(String agencyId, String id, Transition request, String token,
                                    boolean administrator, String correlationId) {
        RecruitmentPlan plan = plan(agencyId, id);
        requireVersion(plan.getVersion(), request.recordVersion());
        List<VacancyRequest> active = activeVacancies(plan, agencyId);
        if (active.isEmpty()) {
            throw new IllegalArgumentException("An approvable plan requires at least one submitted vacancy");
        }
        if (active.stream().anyMatch(v -> v.getStatus() != VacancyRequestStatus.SUBMITTED)) {
            throw new IllegalArgumentException(
                    "Every active vacancy request must remain SUBMITTED before approving the plan");
        }
        active.forEach(item -> requireSourcesUnchanged(agencyId, item, token));
        String actor = audit.currentActor();
        String reason = requireSeparation(actor, plan.getSubmittedBy(), administrator,
                request.reason(), "plan approval");
        PlanResponse before = response(plan, agencyId);
        plan.approve(actor, Instant.now());
        plan = plans.saveAndFlush(plan);
        PlanResponse after = response(plan, agencyId);
        audit.record(agencyId, isOverride(actor, plan.getSubmittedBy(), administrator)
                        ? "ADMIN_APPROVE_PLAN" : "APPROVE_PLAN",
                "RSP_RECRUITMENT_PLAN", id, null, plan.getVersion(), before, after, reason, correlationId);
        return after;
    }

    @Override
    public PlanResponse addVacancy(String agencyId, String planId, SaveVacancy request,
                                   String token, String correlationId) {
        RecruitmentPlan plan = plan(agencyId, planId);
        plan.requireEditable();
        if (request.recordVersion() != null) {
            throw new IllegalArgumentException("recordVersion must not be supplied when creating");
        }
        requireNoOverlappingVacancy(agencyId, request.plantillaId(), plan, null);
        Sources sources = sources(agencyId, request.plantillaId(), request.businessUnitId(),
                LocalDate.now(), token);
        VacancyRequest vacancy = vacancies.saveAndFlush(new VacancyRequest(agencyId, plan,
                request.vacancyType(), request.anticipatedVacancyDate(), request.anticipatedReasonCode(),
                request.anticipatedExplanation(), request.authorityReference(), request.recruitmentPriority(),
                request.targetFillDate(), request.justification(), sources.administrative(),
                sources.occupancy(), sources.profile()));
        VacancyResponse after = vacancy(vacancy);
        audit.record(agencyId, "CREATE_DRAFT", "RSP_VACANCY_REQUEST", vacancy.getId(), null,
                vacancy.getVersion(), null, after, null, correlationId);
        return response(plan, agencyId);
    }

    @Override
    public PlanResponse updateVacancy(String agencyId, String id, SaveVacancy request,
                                      String token, String correlationId) {
        VacancyRequest vacancy = vacancyEntity(agencyId, id);
        requireVersion(vacancy.getVersion(), request.recordVersion());
        if (!vacancy.getPlantillaId().equals(request.plantillaId())) {
            throw new IllegalArgumentException("plantillaId cannot change");
        }
        requireNoOverlappingVacancy(agencyId, request.plantillaId(), vacancy.getPlan(), vacancy.getId());
        Sources sources = sources(agencyId, request.plantillaId(), request.businessUnitId(),
                LocalDate.now(), token);
        VacancyResponse before = vacancy(vacancy);
        vacancy.update(request.vacancyType(), request.anticipatedVacancyDate(),
                request.anticipatedReasonCode(), request.anticipatedExplanation(),
                request.authorityReference(), request.recruitmentPriority(), request.targetFillDate(),
                request.justification(), sources.administrative(), sources.occupancy(), sources.profile());
        vacancy = vacancies.saveAndFlush(vacancy);
        VacancyResponse after = vacancy(vacancy);
        audit.record(agencyId, "UPDATE_DRAFT", "RSP_VACANCY_REQUEST", id, null,
                vacancy.getVersion(), before, after, null, correlationId);
        return response(vacancy.getPlan(), agencyId);
    }

    @Override
    public PlanResponse archiveVacancy(String agencyId, String id, Transition request, String correlationId) {
        VacancyRequest vacancy = vacancyEntity(agencyId, id);
        requireVersion(vacancy.getVersion(), request.recordVersion());
        VacancyResponse before = vacancy(vacancy);
        vacancy.archiveDraft();
        vacancy = vacancies.saveAndFlush(vacancy);
        VacancyResponse after = vacancy(vacancy);
        audit.record(agencyId, "ARCHIVE_DRAFT", "RSP_VACANCY_REQUEST", id, null,
                vacancy.getVersion(), before, after, requiredReason(request.reason()), correlationId);
        return response(vacancy.getPlan(), agencyId);
    }

    @Override
    public PlanResponse submitVacancy(String agencyId, String id, Transition request, String token,
                                      String correlationId) {
        VacancyRequest vacancy = vacancyEntity(agencyId, id);
        requireVersion(vacancy.getVersion(), request.recordVersion());
        requireSourcesUnchanged(agencyId, vacancy, token);
        VacancyResponse before = vacancy(vacancy);
        vacancy.submit(audit.currentActor(), Instant.now());
        vacancy = vacancies.saveAndFlush(vacancy);
        VacancyResponse after = vacancy(vacancy);
        audit.record(agencyId, "SUBMIT_VACANCY", "RSP_VACANCY_REQUEST", id, null,
                vacancy.getVersion(), before, after, normalizeReason(request.reason()), correlationId);
        return response(vacancy.getPlan(), agencyId);
    }

    @Override
    public PlanResponse returnVacancy(String agencyId, String id, Transition request, String correlationId) {
        VacancyRequest vacancy = vacancyEntity(agencyId, id);
        requireVersion(vacancy.getVersion(), request.recordVersion());
        VacancyResponse before = vacancy(vacancy);
        vacancy.returnSubmission();
        vacancy = vacancies.saveAndFlush(vacancy);
        VacancyResponse after = vacancy(vacancy);
        audit.record(agencyId, "RETURN_VACANCY", "RSP_VACANCY_REQUEST", id, null,
                vacancy.getVersion(), before, after, requiredReason(request.reason()), correlationId);
        return response(vacancy.getPlan(), agencyId);
    }

    @Override
    public PlanResponse authorizeVacancy(String agencyId, String id, Transition request, String token,
                                         boolean administrator, String correlationId) {
        VacancyRequest vacancy = vacancyEntity(agencyId, id);
        requireVersion(vacancy.getVersion(), request.recordVersion());
        requireSourcesUnchanged(agencyId, vacancy, token);
        String actor = audit.currentActor();
        String reason = requireSeparation(actor, vacancy.getSubmittedBy(), administrator,
                request.reason(), "vacancy authorization");
        VacancyResponse before = vacancy(vacancy);
        vacancy.authorize(actor, Instant.now());
        vacancy = vacancies.saveAndFlush(vacancy);
        VacancyResponse after = vacancy(vacancy);
        audit.record(agencyId, isOverride(actor, vacancy.getSubmittedBy(), administrator)
                        ? "ADMIN_AUTHORIZE_VACANCY" : "AUTHORIZE_VACANCY",
                "RSP_VACANCY_REQUEST", id, null, vacancy.getVersion(), before, after, reason, correlationId);
        return response(vacancy.getPlan(), agencyId);
    }

    @Override
    public PlanResponse declineVacancy(String agencyId, String id, Transition request,
                                       boolean administrator, String correlationId) {
        VacancyRequest vacancy = vacancyEntity(agencyId, id);
        requireVersion(vacancy.getVersion(), request.recordVersion());
        String actor = audit.currentActor();
        String reason = requireSeparation(actor, vacancy.getSubmittedBy(), administrator,
                requiredReason(request.reason()), "vacancy decision");
        VacancyResponse before = vacancy(vacancy);
        vacancy.decline(actor, Instant.now());
        vacancy = vacancies.saveAndFlush(vacancy);
        VacancyResponse after = vacancy(vacancy);
        audit.record(agencyId, isOverride(actor, vacancy.getSubmittedBy(), administrator)
                        ? "ADMIN_DECLINE_VACANCY" : "DECLINE_VACANCY",
                "RSP_VACANCY_REQUEST", id, null, vacancy.getVersion(), before, after, reason, correlationId);
        return response(vacancy.getPlan(), agencyId);
    }

    @Override
    public PlanResponse cancelVacancy(String agencyId, String id, Transition request, String correlationId) {
        VacancyRequest vacancy = vacancyEntity(agencyId, id);
        requireVersion(vacancy.getVersion(), request.recordVersion());
        VacancyResponse before = vacancy(vacancy);
        vacancy.cancel(audit.currentActor(), Instant.now());
        vacancy = vacancies.saveAndFlush(vacancy);
        VacancyResponse after = vacancy(vacancy);
        audit.record(agencyId, "CANCEL_VACANCY", "RSP_VACANCY_REQUEST", id, null,
                vacancy.getVersion(), before, after, requiredReason(request.reason()), correlationId);
        return response(vacancy.getPlan(), agencyId);
    }

    @Override
    @Transactional(readOnly = true)
    public Readiness vacancyReadiness(String agencyId, String id, String token) {
        VacancyRequest vacancy = vacancyEntity(agencyId, id);
        Sources current = sources(agencyId, vacancy.getPlantillaId(), vacancy.getBusinessUnitId(),
                LocalDate.now(), token);
        boolean unchanged = sourcesMatch(vacancy, current);
        List<String> blockers = unchanged ? List.of()
                : List.of("Authoritative vacancy sources changed; refresh the draft before continuing");
        return new Readiness(vacancy.getPlantillaId(), current.occupancy().occupied(),
                current.administrative().sourceFingerprint(), current.occupancy().sourceFingerprint(),
                current.profile().id(), current.profile().definitionVersion(), unchanged, blockers, Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Readiness readiness(String agencyId, Long plantillaId, Long businessUnitId,
                               LocalDate asOf, String token) {
        Sources sources = sources(agencyId, plantillaId, businessUnitId,
                asOf == null ? LocalDate.now() : asOf, token);
        return new Readiness(plantillaId, sources.occupancy().occupied(),
                sources.administrative().sourceFingerprint(), sources.occupancy().sourceFingerprint(),
                sources.profile().id(), sources.profile().definitionVersion(), true, List.of(), Instant.now());
    }

    private void requireSourcesUnchanged(String agencyId, VacancyRequest vacancy, String token) {
        Sources current = sources(agencyId, vacancy.getPlantillaId(), vacancy.getBusinessUnitId(),
                LocalDate.now(), token);
        if (!sourcesMatch(vacancy, current)) {
            throw new PublicationConflictException(
                    "Authoritative vacancy sources changed; return to draft, refresh, and retry");
        }
        if (vacancy.getVacancyType() == VacancyType.ACTUAL && current.occupancy().occupied()) {
            throw new PublicationConflictException("The Plantilla is now occupied and is not an ACTUAL vacancy");
        }
    }

    private static boolean sourcesMatch(VacancyRequest vacancy, Sources current) {
        return Objects.equals(vacancy.getAdministrativeFingerprint(),
                        current.administrative().sourceFingerprint())
                && Objects.equals(vacancy.getHrmFingerprint(), current.occupancy().sourceFingerprint())
                && Objects.equals(vacancy.getPositionProfileId(), current.profile().id())
                && vacancy.getPositionProfileDefinitionVersion() == current.profile().definitionVersion()
                && vacancy.getPositionProfileRecordRevision() == current.profile().contentRevision();
    }

    private Sources sources(String agencyId, Long plantillaId, Long businessUnitId,
                            LocalDate date, String token) {
        AdministrativeRspPositionSource admin = administrative.get(
                plantillaId, businessUnitId, date, token);
        HumanResourcePlantillaOccupancy occupancy = humanResource.get(plantillaId, token);
        PositionProfileResolutionResponse resolution = positionProfiles.resolve(
                agencyId, admin.jobPositionId(), plantillaId, date);
        return new Sources(admin, occupancy, resolution.profile());
    }

    private RecruitmentPlan plan(String agencyId, String id) {
        return plans.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruitment plan was not found"));
    }

    private VacancyRequest vacancyEntity(String agencyId, String id) {
        return vacancies.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy request was not found"));
    }

    private List<VacancyRequest> activeVacancies(RecruitmentPlan plan, String agencyId) {
        return vacancies.findByPlanIdAndAgencyIdOrderByCreatedAtAsc(plan.getId(), agencyId).stream()
                .filter(VacancyRequest::isActive).toList();
    }

    private PlanResponse response(RecruitmentPlan plan, String agencyId) {
        return new PlanResponse(plan.getId(), plan.getCode(), plan.getTitle(), plan.getPeriodStart(),
                plan.getPeriodEnd(), plan.getDescription(), plan.getStatus(), plan.getSubmittedBy(),
                plan.getSubmittedAt(), plan.getApprovedBy(), plan.getApprovedAt(), plan.getVersion(),
                vacancies.findByPlanIdAndAgencyIdOrderByCreatedAtAsc(plan.getId(), agencyId).stream()
                        .map(this::vacancy).toList());
    }

    private VacancyResponse vacancy(VacancyRequest item) {
        return new VacancyResponse(item.getId(), item.getPlan().getId(), item.getStatus(), item.isActive(),
                item.getVacancyType(), item.getAnticipatedVacancyDate(), item.getAnticipatedReasonCode(),
                item.getAnticipatedExplanation(), item.getAuthorityReference(), item.getRecruitmentPriority(),
                item.getTargetFillDate(), item.getJustification(), item.getPlantillaId(), item.getPlantillaName(),
                item.getJobPositionId(), item.getJobPositionName(), item.getSalaryGrade(), item.getSalaryStep(),
                item.getBusinessUnitId(), item.getBusinessUnitCode(), item.getBusinessUnitName(),
                item.getQualificationStandardId(), item.getQualificationStandardVersion(),
                item.getEducationRequirement(), item.getTrainingRequirement(), item.getExperienceRequirement(),
                item.getEligibilityRequirement(), item.getLicenseRequirement(), item.getQualificationSourceBasis(),
                item.getPositionProfileId(), item.getPositionProfileDefinitionVersion(),
                item.getPositionProfileRecordRevision(), item.getAdministrativeFingerprint(),
                item.getAdministrativeFetchedAt(), item.isOccupied(), item.getActiveAppointmentId(),
                item.getOccupancyAssumptionDate(), item.getHrmFingerprint(), item.getHrmFetchedAt(),
                item.getSubmittedBy(), item.getSubmittedAt(), item.getDecidedBy(), item.getDecidedAt(),
                item.getVersion());
    }

    private void requireNoOverlappingVacancy(String agencyId, Long plantillaId,
                                             RecruitmentPlan plan, String excludeId) {
        if (vacancies.existsActiveForOverlappingPeriod(agencyId, plantillaId,
                plan.getPeriodStart(), plan.getPeriodEnd(), excludeId)) {
            throw new IllegalArgumentException(
                    "This Plantilla already has an active vacancy request in an overlapping recruitment period");
        }
    }

    private void requireNoDuplicateVacanciesAfterPeriodChange(String agencyId, RecruitmentPlan plan) {
        for (VacancyRequest vacancy : activeVacancies(plan, agencyId)) {
            requireNoOverlappingVacancy(agencyId, vacancy.getPlantillaId(), plan, vacancy.getId());
        }
    }

    private static void requireVersion(long actual, Long expected) {
        if (expected == null || actual != expected) {
            throw new OptimisticConflictException(
                    "Expected recordVersion " + expected + " but current version is " + actual);
        }
    }

    private static String requireSeparation(String actor, String submitter, boolean administrator,
                                            String suppliedReason, String action) {
        String reason = normalizeReason(suppliedReason);
        if (actor != null && actor.equalsIgnoreCase(submitter)) {
            if (!administrator) {
                throw new AccessDeniedException("A submitter cannot perform their own " + action);
            }
            if (reason == null) {
                throw new IllegalArgumentException(
                        "Administrator self-action requires an explicit reason");
            }
        }
        return reason;
    }

    private static boolean isOverride(String actor, String submitter, boolean administrator) {
        return administrator && actor != null && actor.equalsIgnoreCase(submitter);
    }

    private static String requiredReason(String value) {
        String reason = normalizeReason(value);
        if (reason == null) throw new IllegalArgumentException("A reason is required");
        return reason;
    }

    private static String normalizeReason(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid page request");
        }
    }

    private record Sources(AdministrativeRspPositionSource administrative,
                           HumanResourcePlantillaOccupancy occupancy,
                           PositionProfileResponse profile) {
    }
}
