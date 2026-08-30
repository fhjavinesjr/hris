package com.primehr.rsp.application;

import com.primehr.integration.administrative.AdministrativeRspPositionSource;
import com.primehr.integration.administrative.AdministrativeRspPositionSourceClient;
import com.primehr.integration.humanresource.HumanResourcePlantillaOccupancy;
import com.primehr.integration.humanresource.HumanResourcePlantillaOccupancyClient;
import com.primehr.positionprofile.api.PositionProfileResolutionResponse;
import com.primehr.positionprofile.api.PositionProfileResponse;
import com.primehr.positionprofile.application.PositionProfileAdminService;
import com.primehr.rsp.api.RspPublicationDtos.ChannelInput;
import com.primehr.rsp.api.RspPublicationDtos.ChannelResponse;
import com.primehr.rsp.api.RspPublicationDtos.CreatePublication;
import com.primehr.rsp.api.RspPublicationDtos.PublicationResponse;
import com.primehr.rsp.api.RspPublicationDtos.PublicationTransition;
import com.primehr.rsp.api.RspPublicationDtos.RequirementSnapshotResponse;
import com.primehr.rsp.api.RspPublicationDtos.UpdatePublication;
import com.primehr.rsp.domain.VacancyPublication;
import com.primehr.rsp.domain.VacancyPublicationChannel;
import com.primehr.rsp.domain.VacancyPublicationRequirementSnapshot;
import com.primehr.rsp.domain.VacancyRequest;
import com.primehr.rsp.domain.VacancyType;
import com.primehr.rsp.infrastructure.VacancyPublicationChannelRepository;
import com.primehr.rsp.infrastructure.VacancyPublicationRepository;
import com.primehr.rsp.infrastructure.VacancyPublicationRequirementRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
public class RspPublicationServiceImpl implements RspPublicationService {
    private final VacancyPublicationRepository publications;
    private final VacancyPublicationChannelRepository channels;
    private final VacancyPublicationRequirementRepository requirements;
    private final VacancyRequestRepository vacancies;
    private final AdministrativeRspPositionSourceClient administrative;
    private final HumanResourcePlantillaOccupancyClient humanResource;
    private final PositionProfileAdminService positionProfiles;
    private final PrimeHrAuditService audit;

    public RspPublicationServiceImpl(VacancyPublicationRepository publications,
                                     VacancyPublicationChannelRepository channels,
                                     VacancyPublicationRequirementRepository requirements,
                                     VacancyRequestRepository vacancies,
                                     AdministrativeRspPositionSourceClient administrative,
                                     HumanResourcePlantillaOccupancyClient humanResource,
                                     PositionProfileAdminService positionProfiles,
                                     PrimeHrAuditService audit) {
        this.publications = publications;
        this.channels = channels;
        this.requirements = requirements;
        this.vacancies = vacancies;
        this.administrative = administrative;
        this.humanResource = humanResource;
        this.positionProfiles = positionProfiles;
        this.audit = audit;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PublicationResponse> list(String agencyId, int page, int size) {
        validatePage(page, size);
        return PageResponse.from(publications.findByAgencyId(agencyId,
                PageRequest.of(page, size, Sort.by("openingDate").descending().and(Sort.by("createdAt")))),
                this::response);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicationResponse get(String agencyId, String id) {
        return response(publication(agencyId, id));
    }

    @Override
    public PublicationResponse create(String agencyId, CreatePublication request,
                                      String token, String correlationId) {
        if (request.recordVersion() != null) {
            throw new IllegalArgumentException("recordVersion must not be supplied when creating");
        }
        VacancyRequest vacancy = vacancy(agencyId, request.vacancyRequestId());
        if (publications.existsByAgencyIdAndVacancyRequestId(agencyId, vacancy.getId())) {
            throw new IllegalArgumentException("This vacancy request already has a publication record");
        }
        PositionProfileResponse currentProfile = requireSourcesUnchanged(agencyId, vacancy, token);
        validateChannels(request.channels(), request.openingDate(), request.closingDate(), false);
        VacancyPublication publication = publications.saveAndFlush(new VacancyPublication(agencyId, vacancy,
                request.visibility(), request.openingDate(), request.closingDate(), request.instructions(),
                request.contactGuidance(), request.noticeText(), Instant.now()));
        replaceChannels(agencyId, publication, request.channels());
        snapshotRequirements(agencyId, publication, currentProfile);
        PublicationResponse after = response(publication);
        audit.record(agencyId, "CREATE_PUBLICATION_DRAFT", "RSP_VACANCY_PUBLICATION",
                publication.getId(), null, publication.getVersion(), null, after, null, correlationId);
        return after;
    }

    @Override
    public PublicationResponse update(String agencyId, String id, UpdatePublication request,
                                      String token, String correlationId) {
        VacancyPublication publication = publication(agencyId, id);
        requireVersion(publication.getVersion(), request.recordVersion());
        PositionProfileResponse currentProfile = requireSourcesUnchanged(
                agencyId, publication.getVacancyRequest(), token);
        validateChannels(request.channels(), request.openingDate(), request.closingDate(), false);
        PublicationResponse before = response(publication);
        publication.updateDraft(request.visibility(), request.openingDate(), request.closingDate(),
                request.instructions(), request.contactGuidance(), request.noticeText());
        publication.refreshSourceSnapshot(publication.getVacancyRequest(), Instant.now());
        publication = publications.saveAndFlush(publication);
        replaceChannels(agencyId, publication, request.channels());
        replaceRequirements(agencyId, publication, currentProfile);
        PublicationResponse after = response(publication);
        audit.record(agencyId, "UPDATE_PUBLICATION_DRAFT", "RSP_VACANCY_PUBLICATION", id, null,
                publication.getVersion(), before, after, null, correlationId);
        return after;
    }

    @Override
    public PublicationResponse submit(String agencyId, String id, PublicationTransition request,
                                      String token, String correlationId) {
        VacancyPublication publication = publication(agencyId, id);
        requireVersion(publication.getVersion(), request.recordVersion());
        requireSourcesUnchanged(agencyId, publication.getVacancyRequest(), token);
        requireActiveChannels(publication);
        PublicationResponse before = response(publication);
        publication.submit(audit.currentActor(), Instant.now());
        publication = publications.saveAndFlush(publication);
        PublicationResponse after = response(publication);
        audit.record(agencyId, "SUBMIT_PUBLICATION", "RSP_VACANCY_PUBLICATION", id, null,
                publication.getVersion(), before, after, normalizeReason(request.reason()), correlationId);
        return after;
    }

    @Override
    public PublicationResponse returnSubmission(String agencyId, String id,
                                                 PublicationTransition request, String correlationId) {
        VacancyPublication publication = publication(agencyId, id);
        requireVersion(publication.getVersion(), request.recordVersion());
        PublicationResponse before = response(publication);
        publication.returnSubmission();
        publication = publications.saveAndFlush(publication);
        PublicationResponse after = response(publication);
        audit.record(agencyId, "RETURN_PUBLICATION", "RSP_VACANCY_PUBLICATION", id, null,
                publication.getVersion(), before, after, requiredReason(request.reason()), correlationId);
        return after;
    }

    @Override
    public PublicationResponse approve(String agencyId, String id, PublicationTransition request,
                                       String token, boolean administrator, String correlationId) {
        VacancyPublication publication = publication(agencyId, id);
        requireVersion(publication.getVersion(), request.recordVersion());
        requireSourcesUnchanged(agencyId, publication.getVacancyRequest(), token);
        requireActiveChannels(publication);
        String actor = audit.currentActor();
        String reason = requireSeparation(actor, publication.getSubmittedBy(), administrator,
                request.reason(), "publication approval");
        PublicationResponse before = response(publication);
        publication.approve(actor, Instant.now());
        publication = publications.saveAndFlush(publication);
        PublicationResponse after = response(publication);
        audit.record(agencyId, isOverride(actor, publication.getSubmittedBy(), administrator)
                        ? "ADMIN_APPROVE_PUBLICATION" : "APPROVE_PUBLICATION",
                "RSP_VACANCY_PUBLICATION", id, null, publication.getVersion(),
                before, after, reason, correlationId);
        return after;
    }

    @Override
    public PublicationResponse publish(String agencyId, String id, PublicationTransition request,
                                       String token, boolean administrator, String correlationId) {
        VacancyPublication publication = publication(agencyId, id);
        requireVersion(publication.getVersion(), request.recordVersion());
        requireSourcesUnchanged(agencyId, publication.getVacancyRequest(), token);
        requireActiveChannels(publication);
        String actor = audit.currentActor();
        String reason = requireSeparation(actor, publication.getSubmittedBy(), administrator,
                request.reason(), "publication");
        PublicationResponse before = response(publication);
        publication.publish(actor, Instant.now());
        publication = publications.saveAndFlush(publication);
        PublicationResponse after = response(publication);
        audit.record(agencyId, isOverride(actor, publication.getSubmittedBy(), administrator)
                        ? "ADMIN_PUBLISH_VACANCY" : "PUBLISH_VACANCY",
                "RSP_VACANCY_PUBLICATION", id, null, publication.getVersion(),
                before, after, reason, correlationId);
        return after;
    }

    @Override
    public PublicationResponse cancel(String agencyId, String id, PublicationTransition request,
                                      String correlationId) {
        VacancyPublication publication = publication(agencyId, id);
        requireVersion(publication.getVersion(), request.recordVersion());
        PublicationResponse before = response(publication);
        publication.cancel(audit.currentActor(), Instant.now());
        publication = publications.saveAndFlush(publication);
        PublicationResponse after = response(publication);
        audit.record(agencyId, "CANCEL_PUBLICATION", "RSP_VACANCY_PUBLICATION", id, null,
                publication.getVersion(), before, after, requiredReason(request.reason()), correlationId);
        return after;
    }

    @Override
    public PublicationResponse close(String agencyId, String id, PublicationTransition request,
                                     String correlationId) {
        VacancyPublication publication = publication(agencyId, id);
        requireVersion(publication.getVersion(), request.recordVersion());
        PublicationResponse before = response(publication);
        publication.close(audit.currentActor(), Instant.now());
        publication = publications.saveAndFlush(publication);
        PublicationResponse after = response(publication);
        audit.record(agencyId, "CLOSE_PUBLICATION", "RSP_VACANCY_PUBLICATION", id, null,
                publication.getVersion(), before, after, normalizeReason(request.reason()), correlationId);
        return after;
    }

    private PositionProfileResponse requireSourcesUnchanged(String agencyId, VacancyRequest vacancy,
                                                            String token) {
        LocalDate asOf = LocalDate.now();
        AdministrativeRspPositionSource currentAdministrative = administrative.get(
                vacancy.getPlantillaId(), vacancy.getBusinessUnitId(), asOf, token);
        HumanResourcePlantillaOccupancy currentOccupancy = humanResource.get(vacancy.getPlantillaId(), token);
        PositionProfileResolutionResponse resolution = positionProfiles.resolve(
                agencyId, currentAdministrative.jobPositionId(), vacancy.getPlantillaId(), asOf);
        PositionProfileResponse currentProfile = resolution.profile();
        if (!Objects.equals(vacancy.getAdministrativeFingerprint(),
                        currentAdministrative.sourceFingerprint())
                || !Objects.equals(vacancy.getHrmFingerprint(), currentOccupancy.sourceFingerprint())
                || !Objects.equals(vacancy.getPositionProfileId(), currentProfile.id())
                || vacancy.getPositionProfileDefinitionVersion() != currentProfile.definitionVersion()
                || vacancy.getPositionProfileRecordRevision() != currentProfile.contentRevision()) {
            throw new PublicationConflictException(
                    "Authoritative vacancy sources changed; the publication cannot proceed");
        }
        if (vacancy.getVacancyType() == VacancyType.ACTUAL && currentOccupancy.occupied()) {
            throw new PublicationConflictException("The Plantilla is now occupied and is not an ACTUAL vacancy");
        }
        return currentProfile;
    }

    private void replaceChannels(String agencyId, VacancyPublication publication,
                                 List<ChannelInput> requested) {
        List<VacancyPublicationChannel> existing = channels
                .findByPublicationIdAndAgencyIdOrderByPublicationDateAscChannelNameAsc(
                        publication.getId(), agencyId);
        channels.deleteAll(existing);
        channels.flush();
        if (requested == null) return;
        for (ChannelInput item : requested) {
            channels.save(new VacancyPublicationChannel(agencyId, publication, item.channelName(),
                    item.publicationDate(), item.reference()));
        }
        channels.flush();
    }

    private void snapshotRequirements(String agencyId, VacancyPublication publication,
                                      PositionProfileResponse profile) {
        profile.requirements().stream().filter(item -> item.active())
                .forEach(item -> requirements.save(
                        new VacancyPublicationRequirementSnapshot(agencyId, publication, item)));
        requirements.flush();
    }

    private void replaceRequirements(String agencyId, VacancyPublication publication,
                                     PositionProfileResponse profile) {
        requirements.deleteAll(requirements
                .findByPublicationIdAndAgencyIdOrderByDisplayOrderAscIdAsc(publication.getId(), agencyId));
        requirements.flush();
        snapshotRequirements(agencyId, publication, profile);
    }

    private void requireActiveChannels(VacancyPublication publication) {
        boolean present = channels.findByPublicationIdAndAgencyIdOrderByPublicationDateAscChannelNameAsc(
                publication.getId(), publication.getAgencyId()).stream()
                .anyMatch(VacancyPublicationChannel::isActive);
        if (!present) throw new IllegalArgumentException("At least one publication channel is required");
    }

    private static void validateChannels(List<ChannelInput> requested, LocalDate opening,
                                         LocalDate closing, boolean required) {
        if (required && (requested == null || requested.isEmpty())) {
            throw new IllegalArgumentException("At least one publication channel is required");
        }
        if (requested == null) return;
        Set<String> names = new HashSet<>();
        for (ChannelInput item : requested) {
            String normalized = item.channelName().trim().toUpperCase(Locale.ROOT);
            if (!names.add(normalized)) {
                throw new IllegalArgumentException("Publication channel names must be unique");
            }
            if (item.publicationDate().isBefore(opening) || item.publicationDate().isAfter(closing)) {
                throw new IllegalArgumentException(
                        "Publication channel dates must be within the opening and closing dates");
            }
        }
    }

    private VacancyPublication publication(String agencyId, String id) {
        return publications.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy publication was not found"));
    }

    private VacancyRequest vacancy(String agencyId, String id) {
        return vacancies.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy request was not found"));
    }

    private PublicationResponse response(VacancyPublication item) {
        List<ChannelResponse> channelResponses = item.getId() == null ? List.of()
                : channels.findByPublicationIdAndAgencyIdOrderByPublicationDateAscChannelNameAsc(
                        item.getId(), item.getAgencyId()).stream().map(channel -> new ChannelResponse(
                        channel.getId(), channel.getChannelName(), channel.getPublicationDate(),
                        channel.getReference(), channel.isActive(), channel.getVersion())).toList();
        List<RequirementSnapshotResponse> requirementResponses = item.getId() == null ? List.of()
                : requirements.findByPublicationIdAndAgencyIdOrderByDisplayOrderAscIdAsc(
                        item.getId(), item.getAgencyId()).stream().map(requirement ->
                        new RequirementSnapshotResponse(requirement.getId(),
                                requirement.getCompetencyVersionId(), requirement.getCompetencyCode(),
                                requirement.getCompetencyName(), requirement.getCompetencyDefinitionVersion(),
                                requirement.getRequiredLevelId(), requirement.getRequiredLevelCode(),
                                requirement.getRequiredLevelLabel(), requirement.getClassification(),
                                requirement.getCriticalityCode(), requirement.getRemarks(),
                                requirement.getDisplayOrder())).toList();
        VacancyRequest vacancy = item.getVacancyRequest();
        return new PublicationResponse(item.getId(), vacancy.getId(), vacancy.getPlan().getId(),
                item.getStatus(), item.getVisibility(), item.getOpeningDate(), item.getClosingDate(),
                item.getInstructions(), item.getPlaceOfAssignment(), item.getContactGuidance(),
                item.getNoticeText(), item.getPlantillaId(), item.getPlantillaName(), item.getJobPositionId(),
                item.getJobPositionName(), item.getSalaryGrade(), item.getSalaryStep(), item.getBusinessUnitId(),
                item.getBusinessUnitCode(), item.getBusinessUnitName(), item.getQualificationStandardId(),
                item.getQualificationStandardVersion(), item.getEducationRequirement(),
                item.getTrainingRequirement(), item.getExperienceRequirement(), item.getEligibilityRequirement(),
                item.getLicenseRequirement(), item.getQualificationSourceBasis(), item.getPositionProfileId(),
                item.getPositionProfileDefinitionVersion(), item.getPositionProfileRecordRevision(),
                item.getAdministrativeFingerprint(), item.getHrmFingerprint(), item.getSourceSnapshotAt(),
                item.getSubmittedBy(), item.getSubmittedAt(), item.getApprovedBy(), item.getApprovedAt(),
                item.getPublishedBy(), item.getPublishedAt(), item.getClosedBy(), item.getClosedAt(),
                item.getCancelledBy(), item.getCancelledAt(), item.getVersion(), channelResponses,
                requirementResponses);
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
}
