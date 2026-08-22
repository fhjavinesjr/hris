package com.primehr.positionprofile.application;

import com.primehr.competency.domain.Competency;
import com.primehr.competency.domain.DefinitionStatus;
import com.primehr.competency.domain.ProficiencyLevel;
import com.primehr.competency.infrastructure.CompetencyRepository;
import com.primehr.competency.infrastructure.ProficiencyLevelRepository;
import com.primehr.integration.administrative.AdministrativePositionTargetClient;
import com.primehr.positionprofile.api.*;
import com.primehr.positionprofile.domain.*;
import com.primehr.positionprofile.infrastructure.PositionProfileRepository;
import com.primehr.positionprofile.infrastructure.PositionProfileRequirementRepository;
import com.primehr.positionprofile.infrastructure.PositionProfileSpecifications;
import com.primehr.shared.api.PageResponse;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.audit.PrimeHrAuditEventRepository;
import com.primehr.shared.audit.AuditEventResponse;
import com.primehr.shared.exception.OptimisticConflictException;
import com.primehr.shared.exception.ResourceNotFoundException;
import com.primehr.shared.exception.PublicationConflictException;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class PositionProfileAdminServiceImpl implements PositionProfileAdminService {
    private static final Sort ORDER = Sort.by("name").ascending()
            .and(Sort.by("definitionVersion").descending());

    private final PositionProfileRepository profiles;
    private final PositionProfileRequirementRepository requirements;
    private final CompetencyRepository competencies;
    private final ProficiencyLevelRepository levels;
    private final AdministrativePositionTargetClient targets;
    private final PrimeHrAuditService audit;
    private final PrimeHrAuditEventRepository auditEvents;

    public PositionProfileAdminServiceImpl(PositionProfileRepository profiles,
                                           PositionProfileRequirementRepository requirements,
                                           CompetencyRepository competencies,
                                           ProficiencyLevelRepository levels,
                                           AdministrativePositionTargetClient targets,
                                           PrimeHrAuditService audit,
                                           PrimeHrAuditEventRepository auditEvents) {
        this.profiles = profiles;
        this.requirements = requirements;
        this.competencies = competencies;
        this.levels = levels;
        this.targets = targets;
        this.audit = audit;
        this.auditEvents = auditEvents;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PositionProfileSummaryResponse> list(String agencyId, PositionProfileStatus status,
                                                             PositionTargetType targetType, String search,
                                                             int page, int size) {
        validatePage(page, size);
        return PageResponse.from(profiles.findAll(PositionProfileSpecifications.filter(
                agencyId, status, targetType, search), PageRequest.of(page, size, ORDER)), this::summary);
    }

    @Override
    @Transactional(readOnly = true)
    public PositionProfileResponse get(String agencyId, String id) {
        return response(profile(agencyId, id));
    }

    @Override
    public PositionProfileResponse create(String agencyId, CreatePositionProfileRequest request,
                                          String authorizationHeader, String correlationId) {
        if (request.recordVersion() != null) {
            throw new IllegalArgumentException("recordVersion must not be supplied when creating");
        }
        PositionTargetSnapshot target = PositionTargetSnapshot.from(
                targets.get(request.targetType(), request.targetId(), authorizationHeader));
        if (profiles.existsByAgencyIdAndTargetKey(agencyId, target.targetKey())) {
            throw new IllegalArgumentException(
                    "A position profile chain already exists for this target; create a successor version instead");
        }
        PositionProfile entity = profiles.saveAndFlush(PositionProfile.draft(agencyId, target,
                request.name(), request.description(), request.effectiveFrom(), request.effectiveTo()));
        PositionProfileResponse after = response(entity);
        audit.record(agencyId, "CREATE_DRAFT", "POSITION_PROFILE", entity.getId(),
                entity.getDefinitionVersion(), entity.getVersion(), null, after, null, correlationId);
        return after;
    }

    @Override
    public PositionProfileResponse update(String agencyId, String id, UpdatePositionProfileRequest request,
                                          String authorizationHeader, String correlationId) {
        PositionProfile entity = profile(agencyId, id);
        requireVersion(entity.getVersion(), request.recordVersion());
        PositionProfileResponse before = response(entity);
        PositionTargetSnapshot target = PositionTargetSnapshot.from(targets.get(entity.getTargetType(),
                entity.getTargetType() == PositionTargetType.PLANTILLA
                        ? entity.getPlantillaId() : entity.getJobPositionId(), authorizationHeader));
        entity.updateDraft(request.name(), request.description(), request.effectiveFrom(), request.effectiveTo(), target);
        validateExistingRequirements(entity);
        entity = profiles.saveAndFlush(entity);
        PositionProfileResponse after = response(entity);
        audit.record(agencyId, "UPDATE_DRAFT", "POSITION_PROFILE", id, entity.getDefinitionVersion(),
                entity.getVersion(), before, after, null, correlationId);
        return after;
    }

    @Override
    public PositionProfileResponse archive(String agencyId, String id, PositionProfileTransitionRequest request,
                                           String correlationId) {
        PositionProfile entity = profile(agencyId, id);
        requireVersion(entity.getVersion(), request.recordVersion());
        PositionProfileResponse before = response(entity);
        entity.archiveDraft();
        entity = profiles.saveAndFlush(entity);
        PositionProfileResponse after = response(entity);
        audit.record(agencyId, "ARCHIVE_DRAFT", "POSITION_PROFILE", id, entity.getDefinitionVersion(),
                entity.getVersion(), before, after, reason(request.reason()), correlationId);
        return after;
    }

    @Override
    public PositionProfileResponse createSuccessor(String agencyId, String id,
                                                   PositionProfileTransitionRequest request,
                                                   String authorizationHeader, String correlationId) {
        PositionProfile source = profile(agencyId, id);
        requireVersion(source.getVersion(), request.recordVersion());
        if (profiles.existsByAgencyIdAndTargetKeyAndStatusIn(agencyId, source.getTargetKey(),
                List.of(PositionProfileStatus.DRAFT.name(), PositionProfileStatus.SUBMITTED.name()))) {
            throw new IllegalArgumentException("An unfinished profile version already exists for this position target");
        }
        Long targetId = source.getTargetType() == PositionTargetType.PLANTILLA
                ? source.getPlantillaId() : source.getJobPositionId();
        PositionTargetSnapshot target = PositionTargetSnapshot.from(
                targets.get(source.getTargetType(), targetId, authorizationHeader));
        PositionProfile successor = profiles.saveAndFlush(source.successorDraft(target));
        for (PositionProfileRequirement requirement : activeRequirements(source)) {
            requirements.save(requirement.copyTo(successor));
            successor.markRequirementsChanged();
        }
        successor = profiles.saveAndFlush(successor);
        PositionProfileResponse after = response(successor);
        audit.record(agencyId, "CREATE_SUCCESSOR_DRAFT", "POSITION_PROFILE", successor.getId(),
                successor.getDefinitionVersion(), successor.getVersion(), response(source), after,
                reason(request.reason()), correlationId);
        return after;
    }

    @Override
    public PositionProfileResponse addRequirement(String agencyId, String profileId,
                                                  CreatePositionRequirementRequest request,
                                                  String correlationId) {
        PositionProfile profile = profile(agencyId, profileId);
        requireVersion(profile.getVersion(), request.profileRecordVersion());
        if (!profile.isDraft()) throw new com.primehr.shared.exception.IllegalLifecycleTransitionException(
                "Requirements may be added only to a DRAFT position profile");
        Competency competency = publishedCompetency(agencyId, request.competencyVersionId(), profile.getEffectiveFrom());
        if (requirements.existsByProfileIdAndCompetencyId(profileId, competency.getId())) {
            throw new IllegalArgumentException("This exact competency version is already in the position profile");
        }
        ProficiencyLevel level = requiredLevel(agencyId, competency, request.requiredProficiencyLevelId(),
                profile.getEffectiveFrom());
        PositionProfileRequirement requirement = requirements.saveAndFlush(new PositionProfileRequirement(
                agencyId, profile, competency, level, request.classification(), request.criticalityCode(),
                request.remarks(), request.displayOrder()));
        profile.markRequirementsChanged();
        profile = profiles.saveAndFlush(profile);
        PositionRequirementResponse child = requirementResponse(requirement);
        audit.record(agencyId, "CREATE_DRAFT_CHILD", "POSITION_PROFILE_REQUIREMENT", requirement.getId(),
                profile.getDefinitionVersion(), requirement.getVersion(), null, child, null, correlationId);
        return response(profile);
    }

    @Override
    public PositionProfileResponse updateRequirement(String agencyId, String profileId, String requirementId,
                                                     UpdatePositionRequirementRequest request,
                                                     String correlationId) {
        PositionProfile profile = profile(agencyId, profileId);
        requireVersion(profile.getVersion(), request.profileRecordVersion());
        PositionProfileRequirement requirement = requirement(agencyId, profileId, requirementId);
        requireVersion(requirement.getVersion(), request.recordVersion());
        PositionRequirementResponse before = requirementResponse(requirement);
        ProficiencyLevel level = requiredLevel(agencyId, requirement.getCompetency(),
                request.requiredProficiencyLevelId(), profile.getEffectiveFrom());
        requirement.updateDraft(level, request.classification(), request.criticalityCode(), request.remarks(),
                request.displayOrder());
        requirement = requirements.saveAndFlush(requirement);
        profile.markRequirementsChanged();
        profile = profiles.saveAndFlush(profile);
        audit.record(agencyId, "UPDATE_DRAFT_CHILD", "POSITION_PROFILE_REQUIREMENT", requirementId,
                profile.getDefinitionVersion(), requirement.getVersion(), before, requirementResponse(requirement),
                null, correlationId);
        return response(profile);
    }

    @Override
    public PositionProfileResponse archiveRequirement(String agencyId, String profileId, String requirementId,
                                                      PositionRequirementTransitionRequest request,
                                                      String correlationId) {
        PositionProfile profile = profile(agencyId, profileId);
        requireVersion(profile.getVersion(), request.profileRecordVersion());
        PositionProfileRequirement requirement = requirement(agencyId, profileId, requirementId);
        requireVersion(requirement.getVersion(), request.recordVersion());
        PositionRequirementResponse before = requirementResponse(requirement);
        requirement.archiveDraft();
        requirement = requirements.saveAndFlush(requirement);
        profile.markRequirementsChanged();
        profile = profiles.saveAndFlush(profile);
        audit.record(agencyId, "ARCHIVE_DRAFT_CHILD", "POSITION_PROFILE_REQUIREMENT", requirementId,
                profile.getDefinitionVersion(), requirement.getVersion(), before, requirementResponse(requirement),
                reason(request.reason()), correlationId);
        return response(profile);
    }

    @Override
    public PositionProfileResponse submit(String agencyId, String id, SubmitPositionProfileRequest request,
                                          String authorizationHeader, String correlationId) {
        PositionProfile entity = profile(agencyId, id);
        requireVersion(entity.getVersion(), request.recordVersion());
        validateCompleteness(entity);
        PositionProfileResponse before = response(entity);
        PositionTargetSnapshot currentTarget = currentTarget(entity, authorizationHeader);
        entity.submit(audit.currentActor(), Instant.now(), currentTarget);
        entity = profiles.saveAndFlush(entity);
        PositionProfileResponse after = response(entity);
        audit.record(agencyId, "SUBMIT_PROFILE", "POSITION_PROFILE", id, entity.getDefinitionVersion(),
                entity.getVersion(), before, after, null, correlationId);
        return after;
    }

    @Override
    public PositionProfileResponse returnSubmission(String agencyId, String id,
                                                    PositionProfileTransitionRequest request,
                                                    String correlationId) {
        PositionProfile entity = profile(agencyId, id);
        requireVersion(entity.getVersion(), request.recordVersion());
        PositionProfileResponse before = response(entity);
        entity.returnToDraft();
        entity = profiles.saveAndFlush(entity);
        PositionProfileResponse after = response(entity);
        audit.record(agencyId, "RETURN_SUBMISSION", "POSITION_PROFILE", id, entity.getDefinitionVersion(),
                entity.getVersion(), before, after, reason(request.reason()), correlationId);
        return after;
    }

    @Override
    public PositionProfileResponse approve(String agencyId, String id, ApprovePositionProfileRequest request,
                                           String authorizationHeader, boolean administrator,
                                           String correlationId) {
        PositionProfile initial = profile(agencyId, id);
        PositionTargetSnapshot currentTarget = currentTarget(initial, authorizationHeader);
        List<PositionProfile> chain = profiles.findChainForUpdate(agencyId, initial.getTargetKey());
        PositionProfile submitted = chain.stream().filter(item -> item.getId().equals(id)).findFirst()
                .orElseThrow(() -> new PublicationConflictException(
                        "The position profile changed before approval"));
        requireApprovalVersion(submitted.getVersion(), request.recordVersion());
        validateApprovalChain(chain, submitted);
        validateCompleteness(submitted);
        String actor = audit.currentActor();
        String approvalReason = normalizeReason(request.reason());
        if (actor.equalsIgnoreCase(submitted.getSubmittedBy())) {
            if (!administrator) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "A submitter cannot approve their own position profile");
            }
            if (approvalReason == null) {
                throw new IllegalArgumentException("Administrator self-approval requires a reason");
            }
        }
        PositionProfile predecessor = approvedPredecessor(chain, submitted);
        closePredecessor(agencyId, predecessor, submitted.getEffectiveFrom(), approvalReason, correlationId);
        validateNoApprovedOverlap(chain, submitted, predecessor);
        PositionProfileResponse before = response(submitted);
        submitted.approve(actor, Instant.now(), currentTarget);
        submitted = profiles.saveAndFlush(submitted);
        PositionProfileResponse after = response(submitted);
        audit.record(agencyId, administrator && actor.equalsIgnoreCase(submitted.getSubmittedBy())
                        ? "ADMIN_APPROVE_PROFILE" : "APPROVE_PROFILE",
                "POSITION_PROFILE", id, submitted.getDefinitionVersion(), submitted.getVersion(),
                before, after, approvalReason, correlationId);
        return after;
    }

    @Override
    @Transactional(readOnly = true)
    public PositionProfileResolutionResponse resolve(String agencyId, Long jobPositionId, Long plantillaId,
                                                     LocalDate asOf) {
        if (jobPositionId == null || jobPositionId < 1) {
            throw new IllegalArgumentException("jobPositionId must be positive");
        }
        LocalDate effectiveDate = Objects.requireNonNull(asOf, "asOf is required");
        if (plantillaId != null && plantillaId < 1) {
            throw new IllegalArgumentException("plantillaId must be positive");
        }
        if (plantillaId != null) {
            PositionProfile plantilla = effectiveProfile(agencyId, PositionTargetType.PLANTILLA,
                    jobPositionId, plantillaId, effectiveDate);
            if (plantilla != null) {
                return new PositionProfileResolutionResponse(effectiveDate, PositionTargetType.PLANTILLA,
                        response(plantilla));
            }
        }
        PositionProfile job = effectiveProfile(agencyId, PositionTargetType.JOB_POSITION,
                jobPositionId, null, effectiveDate);
        if (job == null) {
            throw new ResourceNotFoundException("No effective approved position profile was found");
        }
        return new PositionProfileResolutionResponse(effectiveDate, PositionTargetType.JOB_POSITION, response(job));
    }

    @Override
    @Transactional(readOnly = true)
    public PositionProfileComparisonResponse compare(String agencyId, String leftProfileId, String rightProfileId) {
        PositionProfile left = profile(agencyId, leftProfileId);
        PositionProfile right = profile(agencyId, rightProfileId);
        if (!left.getTargetKey().equals(right.getTargetKey())) {
            throw new IllegalArgumentException("Only versions of the same position target may be compared");
        }
        Map<String, PositionRequirementResponse> leftItems = activeRequirements(left).stream()
                .map(PositionProfileAdminServiceImpl::requirementResponse)
                .collect(Collectors.toMap(PositionRequirementResponse::competencyVersionId, Function.identity()));
        Map<String, PositionRequirementResponse> rightItems = activeRequirements(right).stream()
                .map(PositionProfileAdminServiceImpl::requirementResponse)
                .collect(Collectors.toMap(PositionRequirementResponse::competencyVersionId, Function.identity()));
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        ids.addAll(leftItems.keySet());
        ids.addAll(rightItems.keySet());
        List<PositionProfileComparisonItemResponse> items = ids.stream()
                .map(id -> comparisonItem(leftItems.get(id), rightItems.get(id)))
                .sorted(Comparator.comparing(PositionProfileComparisonItemResponse::competencyCode)
                        .thenComparing(PositionProfileComparisonItemResponse::competencyVersionId))
                .toList();
        return new PositionProfileComparisonResponse(left.getId(), left.getDefinitionVersion(), right.getId(),
                right.getDefinitionVersion(), count(items, PositionProfileComparisonChange.ADDED),
                count(items, PositionProfileComparisonChange.REMOVED),
                count(items, PositionProfileComparisonChange.CHANGED),
                count(items, PositionProfileComparisonChange.UNCHANGED), items);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditEventResponse> auditEvents(String agencyId, String profileId, int page, int size) {
        validatePage(page, size);
        profile(agencyId, profileId);
        return PageResponse.from(auditEvents.findByAgencyIdAndAggregateTypeAndAggregateId(agencyId,
                "POSITION_PROFILE", profileId,
                PageRequest.of(page, size, Sort.by("occurredAt").descending())), AuditEventResponse::from);
    }

    private void validateExistingRequirements(PositionProfile profile) {
        for (PositionProfileRequirement requirement : activeRequirements(profile)) {
            publishedCompetency(profile.getAgencyId(), requirement.getCompetency().getId(), profile.getEffectiveFrom());
            requiredLevel(profile.getAgencyId(), requirement.getCompetency(),
                    requirement.getRequiredProficiencyLevel().getId(), profile.getEffectiveFrom());
        }
    }

    private void validateCompleteness(PositionProfile profile) {
        if (profile.getEffectiveFrom() == null) {
            throw new IllegalArgumentException("effectiveFrom is required before submission or approval");
        }
        List<PositionProfileRequirement> active = activeRequirements(profile);
        if (active.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one active competency requirement is required before submission or approval");
        }
        for (PositionProfileRequirement requirement : active) {
            publishedCompetency(profile.getAgencyId(), requirement.getCompetency().getId(),
                    profile.getEffectiveFrom());
            requiredLevel(profile.getAgencyId(), requirement.getCompetency(),
                    requirement.getRequiredProficiencyLevel().getId(), profile.getEffectiveFrom());
        }
    }

    private PositionTargetSnapshot currentTarget(PositionProfile profile, String authorizationHeader) {
        Long targetId = profile.getTargetType() == PositionTargetType.PLANTILLA
                ? profile.getPlantillaId() : profile.getJobPositionId();
        return PositionTargetSnapshot.from(targets.get(profile.getTargetType(), targetId, authorizationHeader));
    }

    private PositionProfile effectiveProfile(String agencyId, PositionTargetType type, Long jobPositionId,
                                             Long plantillaId, LocalDate asOf) {
        var page = profiles.findAll(PositionProfileSpecifications.effective(agencyId, type, jobPositionId,
                        plantillaId, asOf),
                PageRequest.of(0, 2, Sort.by("definitionVersion").descending()));
        if (page.getTotalElements() > 1) {
            throw new PublicationConflictException("More than one effective approved position profile was found");
        }
        return page.isEmpty() ? null : page.getContent().get(0);
    }

    private static void validateApprovalChain(List<PositionProfile> chain, PositionProfile submitted) {
        if (submitted.getStatus() != PositionProfileStatus.SUBMITTED) {
            throw new IllegalLifecycleTransitionException("Only SUBMITTED position profiles may be approved");
        }
        if (chain.stream().filter(item -> item.getStatus() == PositionProfileStatus.SUBMITTED).count() != 1) {
            throw new PublicationConflictException("Exactly one submitted version must exist in the target chain");
        }
        List<PositionProfile> active = chain.stream()
                .filter(item -> item.getStatus() == PositionProfileStatus.ACTIVE).toList();
        if (submitted.getSupersedesId() == null) {
            if (submitted.getDefinitionVersion() != 1 || !active.isEmpty()) {
                throw new PublicationConflictException("A first profile version cannot bypass approved history");
            }
            return;
        }
        PositionProfile latest = active.stream().max(Comparator.comparingInt(PositionProfile::getDefinitionVersion))
                .orElseThrow(() -> new PublicationConflictException(
                        "The submitted successor has no approved predecessor"));
        if (!latest.getId().equals(submitted.getSupersedesId())
                || submitted.getDefinitionVersion() != latest.getDefinitionVersion() + 1) {
            throw new PublicationConflictException(
                    "The submitted profile does not supersede the latest approved version");
        }
        if (!submitted.getEffectiveFrom().isAfter(latest.getEffectiveFrom())) {
            throw new IllegalArgumentException(
                    "A successor effectiveFrom must be after its predecessor effectiveFrom");
        }
    }

    private static PositionProfile approvedPredecessor(List<PositionProfile> chain, PositionProfile submitted) {
        if (submitted.getSupersedesId() == null) return null;
        return chain.stream().filter(item -> item.getId().equals(submitted.getSupersedesId())
                        && item.getStatus() == PositionProfileStatus.ACTIVE).findFirst()
                .orElseThrow(() -> new PublicationConflictException("The approved predecessor was not found"));
    }

    private void closePredecessor(String agencyId, PositionProfile predecessor, LocalDate successorFrom,
                                  String reason, String correlationId) {
        if (predecessor == null || predecessor.getEffectiveTo() != null
                && predecessor.getEffectiveTo().isBefore(successorFrom)) return;
        PositionProfileResponse before = response(predecessor);
        predecessor.closeEffectivePeriodBefore(successorFrom);
        predecessor = profiles.saveAndFlush(predecessor);
        audit.record(agencyId, "CLOSE_APPROVED_EFFECTIVITY", "POSITION_PROFILE", predecessor.getId(),
                predecessor.getDefinitionVersion(), predecessor.getVersion(), before, response(predecessor),
                reason, correlationId);
    }

    private static void validateNoApprovedOverlap(List<PositionProfile> chain, PositionProfile submitted,
                                                  PositionProfile predecessor) {
        List<PositionProfile> active = new ArrayList<>(chain.stream()
                .filter(item -> item.getStatus() == PositionProfileStatus.ACTIVE)
                .filter(item -> predecessor == null || !item.getId().equals(predecessor.getId())).toList());
        if (predecessor != null) active.add(predecessor);
        for (PositionProfile item : active) {
            if (rangesOverlap(item.getEffectiveFrom(), item.getEffectiveTo(),
                    submitted.getEffectiveFrom(), submitted.getEffectiveTo())) {
                throw new PublicationConflictException("Approved position-profile effective ranges cannot overlap");
            }
        }
    }

    private static boolean rangesOverlap(LocalDate leftFrom, LocalDate leftTo,
                                         LocalDate rightFrom, LocalDate rightTo) {
        boolean leftBefore = leftTo != null && leftTo.isBefore(rightFrom);
        boolean rightBefore = rightTo != null && rightTo.isBefore(leftFrom);
        return !leftBefore && !rightBefore;
    }

    private static PositionProfileComparisonItemResponse comparisonItem(PositionRequirementResponse left,
                                                                        PositionRequirementResponse right) {
        PositionProfileComparisonChange change;
        if (left == null) change = PositionProfileComparisonChange.ADDED;
        else if (right == null) change = PositionProfileComparisonChange.REMOVED;
        else if (Objects.equals(left.requiredProficiencyLevelId(), right.requiredProficiencyLevelId())
                && left.classification() == right.classification()
                && Objects.equals(left.criticalityCode(), right.criticalityCode())) {
            change = PositionProfileComparisonChange.UNCHANGED;
        } else change = PositionProfileComparisonChange.CHANGED;
        PositionRequirementResponse basis = left == null ? right : left;
        return new PositionProfileComparisonItemResponse(change, basis.competencyVersionId(), basis.competencyCode(),
                basis.competencyName(), left == null ? null : left.requiredProficiencyLevelId(),
                left == null ? null : left.requiredProficiencyLevelLabel(),
                left == null ? null : left.classification(), left == null ? null : left.criticalityCode(),
                right == null ? null : right.requiredProficiencyLevelId(),
                right == null ? null : right.requiredProficiencyLevelLabel(),
                right == null ? null : right.classification(), right == null ? null : right.criticalityCode());
    }

    private static int count(List<PositionProfileComparisonItemResponse> items,
                             PositionProfileComparisonChange change) {
        return (int) items.stream().filter(item -> item.change() == change).count();
    }

    private Competency publishedCompetency(String agencyId, String id, LocalDate asOf) {
        Competency competency = competencies.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Competency version was not found"));
        if (competency.getDefinitionStatus() != DefinitionStatus.ACTIVE || !competency.isActive()) {
            throw new IllegalArgumentException("The exact competency version must be published");
        }
        if (competency.getProficiencyScale().getStatus() != DefinitionStatus.ACTIVE
                || !competency.getProficiencyScale().isActive()) {
            throw new IllegalArgumentException("The competency's exact proficiency scale version must be published");
        }
        if (asOf != null && !competency.isEffectiveOn(asOf)) {
            throw new IllegalArgumentException("The competency version is not effective on the profile start date");
        }
        if (asOf != null && !competency.getProficiencyScale().isEffectiveOn(asOf)) {
            throw new IllegalArgumentException(
                    "The competency's proficiency scale is not effective on the profile start date");
        }
        return competency;
    }

    private ProficiencyLevel requiredLevel(String agencyId, Competency competency, String id, LocalDate asOf) {
        ProficiencyLevel level = levels.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Required proficiency level was not found"));
        if (!level.isActive() || !competency.getProficiencyScale().getId().equals(level.getScale().getId())) {
            throw new IllegalArgumentException(
                    "The required level must belong to the competency's exact published scale version");
        }
        if (asOf != null && !level.isEffectiveOn(asOf)) {
            throw new IllegalArgumentException("The required level is not effective on the profile start date");
        }
        return level;
    }

    private PositionProfile profile(String agencyId, String id) {
        return profiles.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Position profile was not found"));
    }

    private PositionProfileRequirement requirement(String agencyId, String profileId, String id) {
        return requirements.findByIdAndProfileIdAndAgencyId(id, profileId, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Position profile requirement was not found"));
    }

    private List<PositionProfileRequirement> activeRequirements(PositionProfile profile) {
        return requirements.findByProfileIdAndAgencyIdOrderByDisplayOrderAscIdAsc(
                profile.getId(), profile.getAgencyId()).stream().filter(PositionProfileRequirement::isActive).toList();
    }

    private PositionProfileResponse response(PositionProfile item) {
        List<PositionRequirementResponse> children = item.getId() == null ? List.of()
                : requirements.findByProfileIdAndAgencyIdOrderByDisplayOrderAscIdAsc(
                item.getId(), item.getAgencyId()).stream()
                .map(PositionProfileAdminServiceImpl::requirementResponse).toList();
        return new PositionProfileResponse(item.getId(), item.getName(), item.getDescription(), item.getStatus(),
                item.getDefinitionVersion(), item.getSupersedesId(), item.getEffectiveFrom(), item.getEffectiveTo(),
                item.getVersion(), item.getContentRevision(), item.getSubmittedBy(), item.getSubmittedAt(),
                item.getApprovedBy(), item.getApprovedAt(), targetResponse(item), children);
    }

    private PositionProfileSummaryResponse summary(PositionProfile item) {
        String targetName = item.getTargetType() == PositionTargetType.PLANTILLA
                ? item.getSourcePlantillaName() + " - " + item.getSourceJobPositionName()
                : item.getSourceJobPositionName();
        return new PositionProfileSummaryResponse(item.getId(), item.getName(), item.getTargetType(),
                item.getJobPositionId(), item.getPlantillaId(), targetName, item.getStatus(),
                item.getDefinitionVersion(), item.getSupersedesId(), item.getEffectiveFrom(), item.getEffectiveTo(),
                item.getVersion());
    }

    private static PositionTargetSnapshotResponse targetResponse(PositionProfile item) {
        Long targetId = item.getTargetType() == PositionTargetType.PLANTILLA
                ? item.getPlantillaId() : item.getJobPositionId();
        return new PositionTargetSnapshotResponse(item.getTargetType(), targetId, item.getJobPositionId(),
                item.getSourceJobPositionName(), item.getSourceSalaryGrade(), item.getSourceSalaryStep(),
                item.getPlantillaId(), item.getSourcePlantillaName(), item.getSourceFingerprint(),
                item.getSourceSnapshotAt());
    }

    private static PositionRequirementResponse requirementResponse(PositionProfileRequirement item) {
        Competency competency = item.getCompetency();
        ProficiencyLevel level = item.getRequiredProficiencyLevel();
        return new PositionRequirementResponse(item.getId(), competency.getId(), competency.getCode(),
                competency.getName(), competency.getDefinitionVersion(), level.getId(), level.getCode(),
                level.getLabel(), item.getClassification(), item.getCriticalityCode(), item.getRemarks(),
                item.isActive(), item.getDisplayOrder(), item.getVersion());
    }

    private static void requireVersion(long actual, Long supplied) {
        if (supplied == null) throw new IllegalArgumentException("recordVersion is required");
        if (actual != supplied) throw new OptimisticConflictException(
                "Expected recordVersion " + supplied + " but current version is " + actual);
    }

    private static void requireApprovalVersion(long actual, Long supplied) {
        if (supplied == null || actual != supplied) {
            throw new PublicationConflictException(
                    "Expected recordVersion " + supplied + " but current version is " + actual);
        }
    }

    private static String normalizeReason(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String reason(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("A reason is required");
        return value.trim();
    }

    private static void validatePage(int page, int size) {
        if (page < 0) throw new IllegalArgumentException("page cannot be negative");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");
    }
}
