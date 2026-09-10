package com.primehr.rsp.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.primehr.rsp.applicant.domain.ApplicationDocumentSnapshot;
import com.primehr.rsp.applicant.infrastructure.ApplicationDocumentSnapshotRepository;
import com.primehr.rsp.domain.RspAuditedEntity;
import com.primehr.rsp.domain.VacancyPublication;
import com.primehr.rsp.evaluation.domain.*;
import com.primehr.rsp.evaluation.infrastructure.*;
import com.primehr.rsp.handoff.domain.AppointmentHandoff;
import com.primehr.rsp.handoff.infrastructure.AppointmentHandoffRepository;
import com.primehr.rsp.report.RspFormalReportData.*;
import com.primehr.rsp.screening.domain.ScreeningEvidenceLink;
import com.primehr.rsp.screening.infrastructure.ScreeningEvidenceLinkRepository;
import com.primehr.rsp.selection.domain.*;
import com.primehr.rsp.selection.infrastructure.*;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.ApplicationConflictException;
import com.primehr.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class RspFormalReportServiceImpl implements RspFormalReportService {
    private static final String TEMPLATE_VERSION = "PHASE-5F.1-v1";
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z")
            .withZone(ZoneId.of("Asia/Manila"));

    private final EvaluationProceedingRepository proceedings;
    private final ComparativeEvaluationRepository comparisons;
    private final ComparativeEvaluationItemRepository comparisonItems;
    private final EvaluationCandidateRepository candidates;
    private final HrmpsbMeetingRepository meetings;
    private final HrmpsbResolutionRepository resolutions;
    private final EvaluationEvidenceRepository evaluationEvidence;
    private final SelectionCaseRepository selections;
    private final SelectionCandidateDecisionRepository selectionDecisions;
    private final OfferResponseRepository offers;
    private final SelectionNoticeRepository notices;
    private final AppointmentHandoffRepository handoffs;
    private final ApplicationDocumentSnapshotRepository applicationDocuments;
    private final ScreeningEvidenceLinkRepository screeningEvidence;
    private final RspFormalReportRenderer renderer;
    private final PrimeHrAuditService audit;
    private final ObjectMapper json;

    public RspFormalReportServiceImpl(
            EvaluationProceedingRepository proceedings,
            ComparativeEvaluationRepository comparisons,
            ComparativeEvaluationItemRepository comparisonItems,
            EvaluationCandidateRepository candidates,
            HrmpsbMeetingRepository meetings,
            HrmpsbResolutionRepository resolutions,
            EvaluationEvidenceRepository evaluationEvidence,
            SelectionCaseRepository selections,
            SelectionCandidateDecisionRepository selectionDecisions,
            OfferResponseRepository offers,
            SelectionNoticeRepository notices,
            AppointmentHandoffRepository handoffs,
            ApplicationDocumentSnapshotRepository applicationDocuments,
            ScreeningEvidenceLinkRepository screeningEvidence,
            RspFormalReportRenderer renderer,
            PrimeHrAuditService audit,
            ObjectMapper json) {
        this.proceedings = proceedings;
        this.comparisons = comparisons;
        this.comparisonItems = comparisonItems;
        this.candidates = candidates;
        this.meetings = meetings;
        this.resolutions = resolutions;
        this.evaluationEvidence = evaluationEvidence;
        this.selections = selections;
        this.selectionDecisions = selectionDecisions;
        this.offers = offers;
        this.notices = notices;
        this.handoffs = handoffs;
        this.applicationDocuments = applicationDocuments;
        this.screeningEvidence = screeningEvidence;
        this.renderer = renderer;
        this.audit = audit;
        this.json = json;
    }

    @Override
    public byte[] comparative(String agency, String proceedingId, String actor, String correlationId) {
        EvaluationProceeding proceeding = finalProceeding(agency, proceedingId);
        ComparativeEvaluation comparison = finalComparison(agency, proceeding);
        List<EvaluationCandidate> candidateSet = candidates
                .findByAgencyIdAndProceedingIdOrderByAdmittedAtAsc(agency, proceedingId);
        List<ComparativeEvaluationItem> items = comparisonItems
                .findByAgencyIdAndComparativeEvaluationIdOrderByRankNumberAsc(agency, comparison.getId());
        if (items.size() != candidateSet.size()) {
            throw conflict("Comparative evaluation does not cover the finalized candidate set");
        }

        Map<String, EvaluationCandidate> candidateById = candidateSet.stream()
                .collect(Collectors.toMap(RspAuditedEntity::getId, Function.identity()));
        HrmpsbMeeting meeting = finalMeeting(agency, proceedingId);
        Map<String, HrmpsbResolution> resolutionByCandidate = resolutions
                .findByAgencyIdAndMeetingId(agency, meeting.getId()).stream()
                .collect(Collectors.toMap(HrmpsbResolution::getCandidateId, Function.identity()));
        if (resolutionByCandidate.size() != candidateSet.size()) {
            throw conflict("HRMPSB resolutions do not cover the finalized candidate set");
        }

        List<ComparativeRow> rows = new ArrayList<>();
        int rowNumber = 1;
        for (ComparativeEvaluationItem item : items) {
            EvaluationCandidate candidate = Optional.ofNullable(candidateById.get(item.getCandidateId()))
                    .orElseThrow(() -> conflict("Comparative candidate snapshot is stale"));
            HrmpsbResolution resolution = Optional.ofNullable(resolutionByCandidate.get(item.getCandidateId()))
                    .orElseThrow(() -> conflict("Candidate resolution is missing"));
            rows.add(new ComparativeRow(rowNumber++, candidateName(candidate), candidate.getApplication().getId(),
                    item.getTotalScore(), item.getRankNumber(), item.getTieGroup(),
                    stageBreakdown(item.getStageBreakdown()),
                    item.isExcluded() ? "EXCLUDED - " + safe(item.getExclusionReason()) : "ELIGIBLE",
                    resolution.getRecommendation().name() + " - " + resolution.getReason()));
        }

        EvaluationPolicy policy = proceeding.getBinding().getPolicy();
        Committee committee = proceeding.getCommittee();
        VacancyPublication publication = proceeding.getPublication();
        Comparative data = new Comparative(agency, proceedingId, publication.getId(), vacancy(publication),
                publication.getPlaceOfAssignment(),
                policy.getCode() + " v" + policy.getDefinitionVersion() + "; "
                        + policy.getMeritSelectionPlanReference() + "; " + policy.getAggregationMode()
                        + "; round " + policy.getRoundingScale() + " " + policy.getRoundingMode()
                        + "; ties " + policy.getTieRule(),
                committee.getCode() + " v" + committee.getDefinitionVersion() + " - " + committee.getName()
                        + "; " + committee.getLegalBasis(),
                "Meeting v" + meeting.getMeetingRevision() + "; quorum " + meeting.getQuorumRequired()
                        + "; finalized " + time(meeting.getFinalizedAt()),
                comparison.getCalculationFingerprint(), time(comparison.getFinalizedAt()), generated(actor), rows);
        byte[] pdf = renderer.comparative(data);
        record(agency, "GENERATE_RSP_COMPARATIVE_REPORT", "RSP_COMPARATIVE_EVALUATION", comparison.getId(),
                comparison.getEvaluationRevision(), comparison.getVersion(), "COMPARATIVE_EVALUATION",
                comparison.getCalculationFingerprint(), pdf, correlationId);
        return pdf;
    }

    @Override
    public byte[] selection(String agency, String selectionId, String actor, String correlationId) {
        SelectionCase selection = terminalSelection(agency, selectionId);
        EvaluationProceeding proceeding = finalProceeding(agency, selection.getProceedingId());
        ComparativeEvaluation comparison = comparisons
                .findByIdAndAgencyId(selection.getComparativeEvaluationId(), agency)
                .orElseThrow(() -> notFound("Comparative evaluation was not found"));
        validateSelectionSource(selection, comparison);

        List<SelectionRow> rows = new ArrayList<>();
        int rowNumber = 1;
        for (SelectionCandidateDecision decision : selectionDecisions
                .findByAgencyIdAndSelectionCaseIdOrderByRankNumberAsc(agency, selectionId)) {
            EvaluationCandidate candidate = candidates.findByIdAndAgencyId(decision.getCandidateId(), agency)
                    .orElseThrow(() -> conflict("Selection candidate source is missing"));
            String disposition = decision.isSelected() ? "SELECTED"
                    : selection.getOutcome() == SelectionCase.Outcome.NO_SELECTION ? "NO SELECTION"
                    : selection.getOutcome() == SelectionCase.Outcome.DEFERRED ? "DEFERRED" : "NOT SELECTED";
            rows.add(new SelectionRow(rowNumber++, candidateName(candidate), decision.getApplicationId(),
                    decision.getTotalScore(), decision.getRankNumber(), decision.getRecommendation().name(),
                    disposition));
        }

        OfferResponse offer = offers.findByAgencyIdAndSelectionCaseId(agency, selectionId).orElse(null);
        AppointmentHandoff handoff = handoffs
                .findByAgencyIdAndSelectionCaseIdOrderByHandoffRevisionDesc(agency, selectionId).stream()
                .findFirst().orElse(null);
        String authority = safe(selection.getApprovedBy()) + "; " + safe(selection.getDecisionReason())
                + (selection.getVarianceBasis() == null ? "" : "; variance " + selection.getVarianceBasis()
                + " - " + selection.getVarianceReason());
        String offerText = offer == null ? "NOT APPLICABLE" : offer.getStatus() + "; deadline "
                + time(offer.getResponseDeadline())
                + (offer.getRespondedAt() == null ? "" : "; responded " + time(offer.getRespondedAt()));
        String handoffText = handoff == null ? "NOT CREATED" : handoff.getStatus() + "; receipt "
                + safe(handoff.getReceiptId()) + "; receipt state " + safe(handoff.getReceiptState());
        Selection data = new Selection(agency, selectionId, selection.getProceedingId(),
                selection.getPublicationId(), vacancy(proceeding.getPublication()),
                "v" + selection.getCaseRevision()
                        + (selection.getSupersedesId() == null ? "" : "; supersedes " + selection.getSupersedesId()),
                selection.getOutcome().name(), authority, offerText, handoffText,
                selection.getSourceFingerprint(), selection.getStatus() + " by "
                        + safe(selection.getFinalizedBy()) + " at " + time(selection.getFinalizedAt()),
                generated(actor), rows);
        byte[] pdf = renderer.selection(data);
        record(agency, "GENERATE_RSP_SELECTION_REPORT", "RSP_SELECTION_CASE", selectionId,
                selection.getCaseRevision(), selection.getVersion(), "SELECTION_PROCESS",
                selection.getSourceFingerprint(), pdf, correlationId);
        return pdf;
    }

    @Override
    public byte[] evidenceIndex(String agency, String selectionId, String actor, String correlationId) {
        SelectionCase selection = terminalSelection(agency, selectionId);
        EvaluationProceeding proceeding = finalProceeding(agency, selection.getProceedingId());
        ComparativeEvaluation comparison = comparisons
                .findByIdAndAgencyId(selection.getComparativeEvaluationId(), agency)
                .orElseThrow(() -> notFound("Comparative evaluation was not found"));
        validateSelectionSource(selection, comparison);
        VacancyPublication publication = proceeding.getPublication();
        List<EvidenceRow> rows = new ArrayList<>();

        add(rows, "VACANCY_PUBLICATION", publication, "snapshot", "HR_RESTRICTED", "RSP_RECORD",
                sha(publication.getAdministrativeFingerprint() + publication.getHrmFingerprint()),
                publication.getStatus().name(), publication.getSourceSnapshotAt());
        add(rows, "EVALUATION_PROCEEDING", proceeding,
                "candidate " + proceeding.getCandidateSetRevision() + " / execution "
                        + proceeding.getExecutionRevision(), "HRMPSB_RESTRICTED", "RSP_RECORD",
                sha(proceeding.getVacancyFingerprint() + proceeding.getPolicyFingerprint()
                        + proceeding.getCommitteeFingerprint()),
                proceeding.getStatus().name(), proceeding.getUpdatedAt());

        for (EvaluationCandidate candidate : candidates
                .findByAgencyIdAndProceedingIdOrderByAdmittedAtAsc(agency, proceeding.getId())) {
            add(rows, "APPLICATION_SNAPSHOT", candidate, "application v" + candidate.getApplicationVersion(),
                    "CONFIDENTIAL", "APPLICANT_RECORD", candidate.getApplicationFingerprint(),
                    candidate.getStatus().name(), candidate.getAdmittedAt());
            add(rows, "SCREENING_CASE", candidate.getScreeningCase(),
                    "case v" + candidate.getScreeningCaseRevision(), "HR_RESTRICTED", "SCREENING_RECORD",
                    candidate.getScreeningFingerprint(), candidate.getScreeningCase().getStatus().name(),
                    candidate.getScreeningCase().getUpdatedAt());
            for (ApplicationDocumentSnapshot document : applicationDocuments
                    .findByAgencyIdAndApplicationIdOrderByDisplayOrderAsc(agency,
                            candidate.getApplication().getId())) {
                add(rows, "APPLICATION_DOCUMENT", document, "snapshot", document.getClassification(),
                        "APPLICANT_DOCUMENT", document.getChecksum(), "SNAPSHOTTED", document.getCreatedAt());
            }
            for (ScreeningEvidenceLink link : screeningEvidence
                    .findByAgencyIdAndCaseId(agency, candidate.getScreeningCase().getId())) {
                add(rows, "SCREENING_EVIDENCE_LINK", link, link.getType().name(), "HR_RESTRICTED",
                        "SCREENING_RECORD", sha(link.getType() + ":" + link.getReferenceId()),
                        "LINKED", link.getCreatedAt());
            }
        }

        add(rows, "COMPARATIVE_EVALUATION", comparison,
                "evaluation v" + comparison.getEvaluationRevision(), "HRMPSB_RESTRICTED", "RSP_RECORD",
                comparison.getCalculationFingerprint(), comparison.getStatus().name(), comparison.getFinalizedAt());
        HrmpsbMeeting meeting = finalMeeting(agency, proceeding.getId());
        add(rows, "HRMPSB_MEETING", meeting, "meeting v" + meeting.getMeetingRevision(),
                "HRMPSB_RESTRICTED", "RSP_RECORD", sha(meeting.getId() + ":" + meeting.getVersion()),
                meeting.getStatus().name(), meeting.getFinalizedAt());
        for (HrmpsbResolution resolution : resolutions.findByAgencyIdAndMeetingId(agency, meeting.getId())) {
            add(rows, "HRMPSB_RESOLUTION", resolution, "record v" + resolution.getVersion(),
                    "HRMPSB_RESTRICTED", "RSP_RECORD", sha(resolution.getId() + ":" + resolution.getVersion()),
                    resolution.getRecommendation().name(), resolution.getUpdatedAt());
        }
        for (EvaluationEvidence evidence : evaluationEvidence.findByAgencyIdAndProceedingId(agency,
                proceeding.getId())) {
            add(rows, "EVALUATION_EVIDENCE", evidence,
                    evidence.getOwnerType() + ":" + evidence.getOwnerId(), evidence.getClassification().name(),
                    evidence.getRetentionTag(), evidence.getChecksum(), "INDEXED", evidence.getCreatedAt());
        }
        add(rows, "SELECTION_CASE", selection, "case v" + selection.getCaseRevision(), "HR_RESTRICTED",
                "RSP_RECORD", selection.getSourceFingerprint(), selection.getStatus().name(),
                selection.getFinalizedAt());
        for (SelectionNotice notice : notices.findByAgencyIdAndSelectionCaseId(agency, selectionId)) {
            add(rows, "SELECTION_NOTICE", notice, "record v" + notice.getVersion(), "CONFIDENTIAL",
                    "APPLICANT_COMMUNICATION", sha(notice.getId() + ":" + notice.getVersion()),
                    notice.getKind().name(), notice.getReleasedAt());
        }
        offers.findByAgencyIdAndSelectionCaseId(agency, selectionId).ifPresent(offer -> add(rows,
                "OFFER_RESPONSE", offer, "record v" + offer.getVersion(), "CONFIDENTIAL",
                "APPLICANT_COMMUNICATION", sha(offer.getId() + ":" + offer.getVersion()),
                offer.getStatus().name(), offer.getRespondedAt() == null ? offer.getCreatedAt() : offer.getRespondedAt()));
        for (AppointmentHandoff handoff : handoffs
                .findByAgencyIdAndSelectionCaseIdOrderByHandoffRevisionDesc(agency, selectionId)) {
            add(rows, "APPOINTMENT_HANDOFF", handoff, "handoff v" + handoff.getHandoffRevision(),
                    "HR_RESTRICTED", "HANDOFF_RECORD", handoff.getPayloadFingerprint(),
                    handoff.getStatus() + " / " + safe(handoff.getReceiptState()), handoff.getUpdatedAt());
        }

        List<EvidenceRow> numbered = new ArrayList<>();
        int rowNumber = 1;
        for (EvidenceRow row : rows) {
            numbered.add(new EvidenceRow(rowNumber++, row.recordType(), row.recordId(), row.revision(),
                    row.classification(), row.retentionTag(), row.checksum(), row.custodian(), row.status(),
                    row.eventAt()));
        }
        EvidenceIndex data = new EvidenceIndex(agency, selectionId, proceeding.getId(),
                selection.getPublicationId(), vacancy(publication), selection.getSourceFingerprint(),
                selection.getStatus().name(), generated(actor), numbered);
        byte[] pdf = renderer.evidenceIndex(data);
        record(agency, "GENERATE_RSP_EVIDENCE_INDEX", "RSP_SELECTION_CASE", selectionId,
                selection.getCaseRevision(), selection.getVersion(), "EVIDENCE_INDEX",
                selection.getSourceFingerprint(), pdf, correlationId);
        return pdf;
    }

    private EvaluationProceeding finalProceeding(String agency, String id) {
        EvaluationProceeding proceeding = proceedings.findByIdAndAgencyId(id, agency)
                .orElseThrow(() -> notFound("Evaluation proceeding was not found"));
        if (proceeding.getStatus() != EvaluationProceeding.Status.FINALIZED) {
            throw conflict("A formal RSP report requires a FINALIZED proceeding");
        }
        return proceeding;
    }

    private ComparativeEvaluation finalComparison(String agency, EvaluationProceeding proceeding) {
        ComparativeEvaluation comparison = comparisons.findByAgencyIdAndProceedingIdAndStatus(
                        agency, proceeding.getId(), ComparativeEvaluation.Status.FINALIZED)
                .orElseThrow(() -> conflict("A finalized comparative evaluation is required"));
        if (!comparison.getPolicyFingerprint().equals(proceeding.getPolicyFingerprint())
                || !sha(comparison.getCalculationSnapshot()).equals(comparison.getCalculationFingerprint())) {
            throw conflict("Comparative source fingerprint is stale");
        }
        return comparison;
    }

    private HrmpsbMeeting finalMeeting(String agency, String proceedingId) {
        return meetings.findByAgencyIdAndProceedingIdOrderByMeetingRevisionAsc(agency, proceedingId).stream()
                .filter(meeting -> meeting.getStatus() == HrmpsbMeeting.Status.FINALIZED)
                .reduce((left, right) -> right)
                .orElseThrow(() -> conflict("A finalized HRMPSB meeting is required"));
    }

    private SelectionCase terminalSelection(String agency, String id) {
        SelectionCase selection = selections.findByIdAndAgencyId(id, agency)
                .orElseThrow(() -> notFound("Selection case was not found"));
        if (selection.getStatus() != SelectionCase.Status.FINALIZED
                && selection.getStatus() != SelectionCase.Status.SUPERSEDED) {
            throw conflict("A formal selection report requires a FINALIZED or SUPERSEDED selection");
        }
        if (selection.getOutcome() == null) {
            throw conflict("Final selection outcome is missing");
        }
        return selection;
    }

    private void validateSelectionSource(SelectionCase selection, ComparativeEvaluation comparison) {
        if (!selection.getProceedingId().equals(comparison.getProceedingId())
                || (comparison.getStatus() != ComparativeEvaluation.Status.FINALIZED
                && comparison.getStatus() != ComparativeEvaluation.Status.SUPERSEDED)
                || !selection.getComparativeFingerprint().equals(comparison.getCalculationFingerprint())
                || !sha(selection.getSourceSnapshot()).equals(selection.getSourceFingerprint())) {
            throw conflict("Selection source fingerprint is stale");
        }
    }

    private String candidateName(EvaluationCandidate candidate) {
        try {
            JsonNode application = json.readTree(candidate.getApplicationSnapshot());
            JsonNode profile = json.readTree(application.path("profileSnapshot").asText("{}"));
            String name = profile.path("displayName").asText("").trim();
            return name.isEmpty() ? "Candidate " + candidate.getId() : name;
        } catch (Exception ignored) {
            return "Candidate " + candidate.getId();
        }
    }

    private String stageBreakdown(String value) {
        try {
            JsonNode root = json.readTree(value);
            List<String> parts = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode stage : root) {
                    String gate = stage.path("gateOutcome").isMissingNode()
                            || stage.path("gateOutcome").isNull() ? ""
                            : "; gate " + stage.path("gateOutcome").asText();
                    parts.add(stage.path("stageCode").asText("STAGE") + ": score "
                            + stage.path("score").asText("N/A") + ", contribution "
                            + stage.path("contribution").asText("N/A") + gate);
                }
            }
            return parts.isEmpty() ? "No scored stage" : String.join(" | ", parts);
        } catch (Exception exception) {
            throw conflict("Comparative stage breakdown is invalid");
        }
    }

    private static void add(List<EvidenceRow> rows, String type, RspAuditedEntity record, String revision,
                            String classification, String retention, String checksum, String status, Instant event) {
        rows.add(new EvidenceRow(0, type, record.getId(), revision, classification, retention, checksum,
                "PrimeHR", status, time(event)));
    }

    private void record(String agency, String action, String aggregate, String id, Integer businessVersion,
                        long recordVersion, String kind, String sourceFingerprint, byte[] pdf,
                        String correlationId) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("reportKind", kind);
        metadata.put("templateVersion", TEMPLATE_VERSION);
        metadata.put("sourceFingerprint", sourceFingerprint);
        metadata.put("outputSha256", sha(pdf));
        metadata.put("byteSize", pdf.length);
        audit.record(agency, action, aggregate, id, businessVersion, recordVersion,
                null, metadata, null, correlationId);
    }

    private static String vacancy(VacancyPublication publication) {
        return publication.getJobPositionName() + " / " + publication.getPlantillaName()
                + " (#" + publication.getPlantillaId() + ")";
    }

    private static String generated(String actor) {
        return safe(actor) + " at " + TIME.format(Instant.now()) + "; Asia/Manila; " + TEMPLATE_VERSION;
    }

    private static String time(Instant value) {
        return value == null ? "N/A" : TIME.format(value);
    }

    private static String safe(Object value) {
        return value == null || value.toString().isBlank() ? "N/A" : value.toString();
    }

    private static String sha(String value) {
        return sha(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha(byte[] value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static ApplicationConflictException conflict(String message) {
        return new ApplicationConflictException(message);
    }

    private static ResourceNotFoundException notFound(String message) {
        return new ResourceNotFoundException(message);
    }
}
