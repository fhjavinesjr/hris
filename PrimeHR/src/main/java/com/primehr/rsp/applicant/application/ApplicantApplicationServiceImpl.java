package com.primehr.rsp.applicant.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.primehr.config.PrimeHrProperties;
import com.primehr.rsp.applicant.api.ApplicationDtos.*;
import com.primehr.rsp.applicant.domain.*;
import com.primehr.rsp.applicant.infrastructure.*;
import com.primehr.rsp.applicant.storage.DocumentStorage;
import com.primehr.rsp.domain.*;
import com.primehr.rsp.infrastructure.*;
import com.primehr.shared.api.PageResponse;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@Transactional
public class ApplicantApplicationServiceImpl implements ApplicantApplicationService {
    private static final Set<PositionApplication.Status> ACTIVE_STATUSES =
            Set.of(PositionApplication.Status.DRAFT, PositionApplication.Status.SUBMITTED);

    private final PrimeHrProperties properties;
    private final PositionApplicationRepository applications;
    private final ApplicationDocumentSnapshotRepository evidence;
    private final ApplicantCommunicationRepository communications;
    private final ApplicantAccountRepository accounts;
    private final ApplicantProfileRepository profiles;
    private final ApplicantProfileEntryRepository profileEntries;
    private final ApplicantDocumentRepository documents;
    private final PrivacyNoticeRepository notices;
    private final ApplicantConsentRepository consents;
    private final VacancyPublicationRepository publications;
    private final VacancyPublicationRequirementRepository publicationRequirements;
    private final DocumentStorage storage;
    private final PrimeHrAuditService audit;
    private final ObjectMapper json;

    public ApplicantApplicationServiceImpl(PrimeHrProperties properties,
                                           PositionApplicationRepository applications,
                                           ApplicationDocumentSnapshotRepository evidence,
                                           ApplicantCommunicationRepository communications,
                                           ApplicantAccountRepository accounts,
                                           ApplicantProfileRepository profiles,
                                           ApplicantProfileEntryRepository profileEntries,
                                           ApplicantDocumentRepository documents,
                                           PrivacyNoticeRepository notices,
                                           ApplicantConsentRepository consents,
                                           VacancyPublicationRepository publications,
                                           VacancyPublicationRequirementRepository publicationRequirements,
                                           DocumentStorage storage, PrimeHrAuditService audit,
                                           ObjectMapper objectMapper) {
        this.properties = properties;
        this.applications = applications;
        this.evidence = evidence;
        this.communications = communications;
        this.accounts = accounts;
        this.profiles = profiles;
        this.profileEntries = profileEntries;
        this.documents = documents;
        this.notices = notices;
        this.consents = consents;
        this.publications = publications;
        this.publicationRequirements = publicationRequirements;
        this.storage = storage;
        this.audit = audit;
        this.json = objectMapper;
    }

    @Override @Transactional(readOnly = true)
    public PageResponse<Application> applicantApplications(String applicantId, int page, int size) {
        requireAccount(applicantId);
        return PageResponse.from(applications.findByAgencyIdAndApplicantIdOrderByCreatedAtDesc(
                agency(), applicantId, page(page, size)), this::response);
    }

    @Override @Transactional(readOnly = true)
    public Application applicantApplication(String applicantId, String applicationId) {
        return response(requireOwned(applicantId, applicationId));
    }

    @Override
    public Application create(String applicantId, Create request, String correlationId) {
        requireAccount(applicantId);
        VacancyPublication publication = requireOpenPublication(request.publicationId());
        if (applications.existsByAgencyIdAndApplicantIdAndPublicationIdAndStatusIn(
                agency(), applicantId, publication.getId(), ACTIVE_STATUSES)) {
            throw new ApplicationConflictException("An active application already exists for this vacancy");
        }
        Optional<PositionApplication> prior = applications
                .findFirstByAgencyIdAndApplicantIdAndPublicationIdOrderByApplicationVersionDesc(
                        agency(), applicantId, publication.getId());
        if (prior.isPresent() && !properties.applicant().allowReapplicationAfterWithdrawal()) {
            throw new ApplicationConflictException("A previous application exists for this vacancy");
        }
        int version = prior.map(value -> value.getApplicationVersion() + 1).orElse(1);
        PositionApplication application = applications.saveAndFlush(
                new PositionApplication(agency(), applicantId, publication, version));
        audit.record(agency(), "CREATE_APPLICATION_DRAFT", "POSITION_APPLICATION", application.getId(),
                version, application.getVersion(), null, Map.of("publicationId", publication.getId()),
                null, correlationId);
        return response(application);
    }

    @Override
    public Application save(String applicantId, String applicationId, Save request, String correlationId) {
        PositionApplication application = requireOwned(applicantId, applicationId);
        requireDraftAndVersion(application, request.recordVersion());
        requireOpenPublication(application.getPublication().getId());
        List<ApplicantDocument> selected = selectedDocuments(applicantId, request.documentIds());
        Instant draftUpdate = Instant.now();
        if (!draftUpdate.isAfter(application.getDraftUpdatedAt())) {
            draftUpdate = application.getDraftUpdatedAt().plusNanos(1);
        }
        application.touchDraft(draftUpdate);
        applications.saveAndFlush(application);
        evidence.deleteByAgencyIdAndApplicationId(agency(), application.getId());
        evidence.flush();
        int order = 0;
        for (ApplicantDocument document : selected) {
            evidence.save(new ApplicationDocumentSnapshot(agency(), application.getId(), document, order++));
        }
        audit.record(agency(), "UPDATE_APPLICATION_DRAFT", "POSITION_APPLICATION", application.getId(),
                application.getApplicationVersion(), application.getVersion(), null,
                Map.of("documentCount", selected.size()), null, correlationId);
        return response(application);
    }

    @Override
    public Application submit(String applicantId, String applicationId, Submit request, String correlationId) {
        PositionApplication application = requireOwned(applicantId, applicationId);
        if (application.getStatus() == PositionApplication.Status.SUBMITTED) return response(application);
        requireDraftAndVersion(application, request.recordVersion());
        VacancyPublication publication = requireOpenPublication(application.getPublication().getId());
        ApplicantProfile profile = profiles.findByAgencyIdAndApplicantId(agency(), applicantId)
                .orElseThrow(() -> new ResourceNotFoundException("Applicant profile was not found"));
        if (!profile.isDeclarationAccepted()) {
            throw new ApplicationConflictException("The applicant declaration must be accepted before submission");
        }
        PrivacyNotice notice = effectiveNotice();
        if (!consents.existsByAgencyIdAndApplicantIdAndNoticeIdAndWithdrawnAtIsNull(
                agency(), applicantId, notice.getId())) {
            throw new ApplicationConflictException("The effective privacy notice must be accepted before submission");
        }
        List<ApplicationDocumentSnapshot> selected = evidence
                .findByAgencyIdAndApplicationIdOrderByDisplayOrderAsc(agency(), application.getId());
        validateSubmissionDocuments(applicantId, selected);
        String acknowledgment = acknowledgment();
        application.submit(acknowledgment, notice.getId(), notice.getDefinitionVersion(),
                vacancySnapshot(publication), qualificationSnapshot(publication),
                competencySnapshot(publication), profileSnapshot(profile), Instant.now());
        applications.saveAndFlush(application);
        communications.save(new ApplicantCommunication(agency(), application.getId(), applicantId,
                ApplicantCommunication.Direction.SYSTEM_TO_APPLICANT, "Application received",
                "Your application was received. Acknowledgment: " + acknowledgment,
                "system", Instant.now(), correlationId));
        audit.record(agency(), "SUBMIT_APPLICATION", "POSITION_APPLICATION", application.getId(),
                application.getApplicationVersion(), application.getVersion(), null,
                Map.of("acknowledgmentNumber", acknowledgment, "documentCount", selected.size()),
                null, correlationId);
        return response(application);
    }

    @Override
    public Application withdraw(String applicantId, String applicationId, Withdraw request,
                                String correlationId) {
        PositionApplication application = requireOwned(applicantId, applicationId);
        if (application.getStatus() == PositionApplication.Status.WITHDRAWN) return response(application);
        requireVersion(application, request.recordVersion());
        application.withdraw(request.reason(), Instant.now());
        applications.saveAndFlush(application);
        communications.save(new ApplicantCommunication(agency(), application.getId(), applicantId,
                ApplicantCommunication.Direction.SYSTEM_TO_APPLICANT, "Application withdrawn",
                "Your application has been withdrawn.", "system", Instant.now(), correlationId));
        audit.record(agency(), "WITHDRAW_APPLICATION", "POSITION_APPLICATION", application.getId(),
                application.getApplicationVersion(), application.getVersion(), null,
                Map.of("status", application.getStatus().name()), request.reason(), correlationId);
        return response(application);
    }

    @Override
    public List<Communication> applicantCommunications(String applicantId, String applicationId) {
        PositionApplication application = requireOwned(applicantId, applicationId);
        List<ApplicantCommunication> messages = communications
                .findByAgencyIdAndApplicationIdOrderByOccurredAtAsc(agency(), application.getId());
        Instant readAt = Instant.now();
        messages.forEach(message -> message.markRead(readAt));
        communications.saveAll(messages);
        return messages.stream().map(this::communication).toList();
    }

    @Override @Transactional(readOnly = true)
    public PageResponse<Application> staffApplications(String agencyId, int page, int size) {
        requireAgency(agencyId);
        return PageResponse.from(applications.findByAgencyIdOrderByCreatedAtDesc(
                agencyId, page(page, size)), this::response);
    }

    @Override @Transactional(readOnly = true)
    public Application staffApplication(String agencyId, String applicationId) {
        return response(requireStaffApplication(agencyId, applicationId));
    }

    @Override @Transactional(readOnly = true)
    public List<Communication> staffCommunications(String agencyId, String applicationId) {
        PositionApplication application = requireStaffApplication(agencyId, applicationId);
        return communications.findByAgencyIdAndApplicationIdOrderByOccurredAtAsc(agencyId, application.getId())
                .stream().map(this::communication).toList();
    }

    @Override
    public Communication sendStaffMessage(String agencyId, String applicationId, StaffMessage request,
                                          String actor, String correlationId) {
        PositionApplication application = requireStaffApplication(agencyId, applicationId);
        if (application.getStatus() == PositionApplication.Status.DRAFT) {
            throw new ApplicationConflictException("Staff messages require a submitted or withdrawn application");
        }
        ApplicantCommunication message = communications.saveAndFlush(new ApplicantCommunication(
                agencyId, application.getId(), application.getApplicantId(),
                ApplicantCommunication.Direction.STAFF_TO_APPLICANT, request.subject(), request.body(),
                actor, Instant.now(), correlationId));
        audit.record(agencyId, "SEND_APPLICATION_MESSAGE", "POSITION_APPLICATION", application.getId(),
                application.getApplicationVersion(), application.getVersion(), null,
                Map.of("communicationId", message.getId(), "subject", message.getSubject()),
                null, correlationId);
        return communication(message);
    }

    @Override
    public ApplicantFoundationService.DocumentContent staffDocument(String agencyId, String applicationId,
                                                                     String evidenceId, String actor) {
        PositionApplication application = requireStaffApplication(agencyId, applicationId);
        ApplicationDocumentSnapshot document = evidence.findByIdAndAgencyIdAndApplicationId(
                evidenceId, agencyId, application.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Application document was not found"));
        audit.record(agencyId, "STAFF_DOWNLOAD_APPLICATION_DOCUMENT", "POSITION_APPLICATION",
                application.getId(), application.getApplicationVersion(), application.getVersion(), null,
                Map.of("evidenceId", evidenceId, "actor", actor), null, null);
        return new ApplicantFoundationService.DocumentContent(document.getOriginalFilename(),
                document.getMediaType(), document.getByteSize(), storage.get(document.getStorageObjectKey()));
    }

    private PositionApplication requireOwned(String applicantId, String applicationId) {
        requireAccount(applicantId);
        return applications.findByIdAndAgencyIdAndApplicantId(applicationId, agency(), applicantId)
                .orElseThrow(() -> new ResourceNotFoundException("Application was not found"));
    }

    private PositionApplication requireStaffApplication(String agencyId, String applicationId) {
        requireAgency(agencyId);
        return applications.findByIdAndAgencyId(applicationId, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Application was not found"));
    }

    private void requireAccount(String applicantId) {
        accounts.findByIdAndAgencyId(applicantId, agency())
                .orElseThrow(() -> new ResourceNotFoundException("Applicant account was not found"));
    }

    private VacancyPublication requireOpenPublication(String publicationId) {
        VacancyPublication publication = publications.findByIdAndAgencyId(publicationId, agency())
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy was not found"));
        LocalDate today = LocalDate.now();
        if (publication.getStatus() != VacancyPublicationStatus.PUBLISHED
                || today.isBefore(publication.getOpeningDate()) || today.isAfter(publication.getClosingDate())) {
            throw new ApplicationConflictException("The vacancy is not open for applications");
        }
        return publication;
    }

    private List<ApplicantDocument> selectedDocuments(String applicantId, List<String> ids) {
        List<String> distinct = ids.stream().distinct().toList();
        if (distinct.size() != ids.size()) throw new IllegalArgumentException("Duplicate document IDs are not allowed");
        return distinct.stream().map(id -> {
            ApplicantDocument document = documents.findByIdAndAgencyIdAndApplicantId(id, agency(), applicantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Applicant document was not found"));
            if (!document.isActive() || document.getScanStatus() == ApplicantDocument.ScanStatus.REJECTED) {
                throw new ApplicationConflictException("Only active, non-rejected documents may be selected");
            }
            return document;
        }).toList();
    }

    private void validateSubmissionDocuments(String applicantId, List<ApplicationDocumentSnapshot> selected) {
        Set<String> required = new HashSet<>(properties.applicant().requiredDocumentTypes());
        for (ApplicationDocumentSnapshot snapshot : selected) {
            ApplicantDocument current = documents.findByIdAndAgencyIdAndApplicantId(
                    snapshot.getApplicantDocumentId(), agency(), applicantId)
                    .orElseThrow(() -> new ApplicationConflictException("A selected document is no longer available"));
            if (!current.isActive() || current.getScanStatus() == ApplicantDocument.ScanStatus.REJECTED
                    || !current.getChecksum().equals(snapshot.getChecksum())) {
                throw new ApplicationConflictException("A selected document changed and the draft must be saved again");
            }
            required.remove(current.getDocumentType());
        }
        if (!required.isEmpty()) throw new ApplicationConflictException(
                "Required application documents are missing: " + String.join(", ", required));
    }

    private PrivacyNotice effectiveNotice() {
        LocalDate today = LocalDate.now();
        return notices.findByAgencyIdAndStatusOrderByDefinitionVersionDesc(agency(), PrivacyNotice.Status.ACTIVE)
                .stream().filter(notice -> notice.effective(today)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No effective privacy notice is configured"));
    }

    private void requireDraftAndVersion(PositionApplication application, long version) {
        if (application.getStatus() != PositionApplication.Status.DRAFT) {
            throw new ApplicationConflictException("Only draft applications can be changed");
        }
        requireVersion(application, version);
    }

    private void requireVersion(PositionApplication application, long version) {
        if (application.getVersion() != version) throw new OptimisticConflictException(
                "Expected recordVersion " + version + " but current version is " + application.getVersion());
    }

    private Application response(PositionApplication application) {
        VacancyPublication publication = application.getPublication();
        List<DocumentEvidence> documents = evidence
                .findByAgencyIdAndApplicationIdOrderByDisplayOrderAsc(application.getAgencyId(), application.getId())
                .stream().map(this::document).toList();
        return new Application(application.getId(), application.getApplicantId(), publication.getId(),
                publication.getJobPositionName(), publication.getPlaceOfAssignment(),
                publication.getOpeningDate(), publication.getClosingDate(), application.getApplicationVersion(),
                application.getStatus().name(), application.getSafeStatus(), application.getAcknowledgmentNumber(),
                application.getSubmittedAt(), application.getWithdrawnAt(), application.getWithdrawalReason(),
                application.getVersion(), documents);
    }

    private DocumentEvidence document(ApplicationDocumentSnapshot value) {
        return new DocumentEvidence(value.getId(), value.getApplicantDocumentId(), value.getDocumentType(),
                value.getOriginalFilename(), value.getMediaType(), value.getByteSize(), value.getChecksum(),
                value.getClassification(), value.getDisplayOrder());
    }

    private Communication communication(ApplicantCommunication value) {
        return new Communication(value.getId(), value.getDirection().name(), value.getChannel(),
                value.getSubject(), value.getBody(), value.getActor(), value.getOccurredAt(), value.getReadAt(),
                value.getCorrelationId());
    }

    private String vacancySnapshot(VacancyPublication value) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("publicationId", value.getId()); snapshot.put("jobPositionId", value.getJobPositionId());
        snapshot.put("jobPositionName", value.getJobPositionName()); snapshot.put("plantillaId", value.getPlantillaId());
        snapshot.put("plantillaName", value.getPlantillaName()); snapshot.put("businessUnitId", value.getBusinessUnitId());
        snapshot.put("businessUnitCode", value.getBusinessUnitCode()); snapshot.put("businessUnitName", value.getBusinessUnitName());
        snapshot.put("placeOfAssignment", value.getPlaceOfAssignment()); snapshot.put("salaryGrade", value.getSalaryGrade());
        snapshot.put("salaryStep", value.getSalaryStep()); snapshot.put("openingDate", value.getOpeningDate());
        snapshot.put("closingDate", value.getClosingDate()); snapshot.put("noticeText", value.getNoticeText());
        snapshot.put("instructions", value.getInstructions()); snapshot.put("contactGuidance", value.getContactGuidance());
        return json(snapshot);
    }

    private String qualificationSnapshot(VacancyPublication value) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("qualificationStandardId", value.getQualificationStandardId());
        snapshot.put("qualificationStandardVersion", value.getQualificationStandardVersion());
        snapshot.put("education", value.getEducationRequirement()); snapshot.put("training", value.getTrainingRequirement());
        snapshot.put("experience", value.getExperienceRequirement()); snapshot.put("eligibility", value.getEligibilityRequirement());
        snapshot.put("license", value.getLicenseRequirement()); snapshot.put("sourceBasis", value.getQualificationSourceBasis());
        snapshot.put("positionProfileId", value.getPositionProfileId());
        snapshot.put("positionProfileDefinitionVersion", value.getPositionProfileDefinitionVersion());
        snapshot.put("positionProfileRecordRevision", value.getPositionProfileRecordRevision());
        return json(snapshot);
    }

    private String competencySnapshot(VacancyPublication publication) {
        return json(publicationRequirements
                .findByPublicationIdAndAgencyIdOrderByDisplayOrderAscIdAsc(publication.getId(), agency())
                .stream().map(value -> Map.of(
                        "competencyVersionId", value.getCompetencyVersionId(),
                        "competencyCode", value.getCompetencyCode(),
                        "competencyName", value.getCompetencyName(),
                        "definitionVersion", value.getCompetencyDefinitionVersion(),
                        "requiredLevelId", value.getRequiredLevelId(),
                        "requiredLevelCode", value.getRequiredLevelCode(),
                        "requiredLevelLabel", value.getRequiredLevelLabel(),
                        "classification", value.getClassification(),
                        "displayOrder", value.getDisplayOrder())).toList());
    }

    private String profileSnapshot(ApplicantProfile profile) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        ApplicantAccount account = accounts.findByIdAndAgencyId(profile.getApplicantId(), agency())
                .orElseThrow(() -> new ResourceNotFoundException("Applicant account was not found"));
        snapshot.put("email", account.getEmail()); snapshot.put("displayName", account.getDisplayName());
        snapshot.put("profileId", profile.getId()); snapshot.put("recordVersion", profile.getVersion());
        snapshot.put("givenName", profile.getGivenName()); snapshot.put("middleName", profile.getMiddleName());
        snapshot.put("familyName", profile.getFamilyName()); snapshot.put("suffix", profile.getSuffix());
        snapshot.put("birthDate", profile.getBirthDate()); snapshot.put("mobileNumber", profile.getMobileNumber());
        snapshot.put("addressLine", profile.getAddressLine()); snapshot.put("city", profile.getCity());
        snapshot.put("province", profile.getProvince()); snapshot.put("postalCode", profile.getPostalCode());
        snapshot.put("citizenship", profile.getCitizenship()); snapshot.put("declarationAccepted", profile.isDeclarationAccepted());
        snapshot.put("entries", profileEntries.findByAgencyIdAndProfileIdOrderByTypeAscDisplayOrderAsc(
                agency(), profile.getId()).stream().map(value -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("type", value.getType()); entry.put("title", value.getTitle());
            entry.put("organizationName", value.getOrganizationName()); entry.put("dateFrom", value.getDateFrom());
            entry.put("dateTo", value.getDateTo()); entry.put("details", value.getDetails());
            entry.put("displayOrder", value.getDisplayOrder()); return entry;
        }).toList());
        return json(snapshot);
    }

    private String json(Object value) {
        try { return json.writeValueAsString(value); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("Submission snapshot could not be serialized", exception); }
    }

    private static PageRequest page(int page, int size) {
        return PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
    }

    private String agency() { return properties.agency().id(); }
    private void requireAgency(String agencyId) {
        if (!agency().equals(agencyId)) throw new ResourceNotFoundException("Application was not found");
    }
    private static String acknowledgment() {
        return "APP-" + Year.now().getValue() + "-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
    }
}
