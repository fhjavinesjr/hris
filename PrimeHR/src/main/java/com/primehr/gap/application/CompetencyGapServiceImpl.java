package com.primehr.gap.application;

import com.primehr.assessment.domain.PersonCompetencyProfile;
import com.primehr.assessment.domain.PersonCompetencyResult;
import com.primehr.assessment.infrastructure.PersonCompetencyProfileRepository;
import com.primehr.assessment.infrastructure.PersonCompetencyResultRepository;
import com.primehr.gap.api.CompetencyGapDtos.*;
import com.primehr.gap.domain.*;
import com.primehr.gap.infrastructure.*;
import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.integration.humanresource.HumanResourceAssessmentSubject;
import com.primehr.integration.humanresource.HumanResourceAssessmentSubjectClient;
import com.primehr.positionprofile.domain.*;
import com.primehr.positionprofile.infrastructure.*;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class CompetencyGapServiceImpl implements CompetencyGapService {
    private final HumanResourceAssessmentSubjectClient subjects;
    private final PositionProfileRepository positionProfiles;
    private final PositionProfileRequirementRepository requirements;
    private final PersonCompetencyProfileRepository personProfiles;
    private final PersonCompetencyResultRepository personResults;
    private final GapPrioritySchemeRepository schemes;
    private final GapPriorityRuleRepository rules;
    private final CompetencyGapAnalysisRepository analyses;
    private final CompetencyGapItemRepository items;
    private final PrimeHrAuditService audit;

    public CompetencyGapServiceImpl(HumanResourceAssessmentSubjectClient subjects,
                                    PositionProfileRepository positionProfiles,
                                    PositionProfileRequirementRepository requirements,
                                    PersonCompetencyProfileRepository personProfiles,
                                    PersonCompetencyResultRepository personResults,
                                    GapPrioritySchemeRepository schemes, GapPriorityRuleRepository rules,
                                    CompetencyGapAnalysisRepository analyses,
                                    CompetencyGapItemRepository items, PrimeHrAuditService audit) {
        this.subjects = subjects;
        this.positionProfiles = positionProfiles;
        this.requirements = requirements;
        this.personProfiles = personProfiles;
        this.personResults = personResults;
        this.schemes = schemes;
        this.rules = rules;
        this.analyses = analyses;
        this.items = items;
        this.audit = audit;
    }

    @Override
    public AnalysisResponse generate(String agencyId, GenerateRequest request, String authorizationHeader,
                                     String actor, String correlationId) {
        String normalizedActor = actor(actor);
        Optional<CompetencyGapAnalysis> sameRequest = analyses.findByAgencyIdAndRequestKey(
                agencyId, request.requestKey().trim());
        if (sameRequest.isPresent()) {
            CompetencyGapAnalysis existing = sameRequest.get();
            if (!existing.getSubjectEmployeeId().equals(request.employeeId())) {
                throw new OptimisticConflictException("The requestKey is already used for another employee");
            }
            return response(existing);
        }

        LocalDate analysisDate = LocalDate.now();
        HumanResourceAssessmentSubject subject = subjects.get(request.employeeId(), authorizationHeader);
        if (subject == null || !subject.eligible()) {
            throw new ResourceNotFoundException("The employee has no eligible current appointment");
        }
        if (!Objects.equals(subject.sourceFingerprint(), request.expectedHrmSourceFingerprint().trim())) {
            throw new OptimisticConflictException("HumanResource appointment data changed; reload before generating");
        }
        PositionProfile positionProfile = resolvePositionProfile(agencyId, subject, analysisDate);
        PersonCompetencyProfile personProfile = personProfiles.findEffective(
                        agencyId, subject.employeeNo(), analysisDate, PageRequest.of(0, 2))
                .stream().findFirst().orElseThrow(() -> new ResourceNotFoundException(
                        "No valid person competency profile was found for the current date"));
        GapPriorityScheme scheme = effectiveScheme(agencyId, analysisDate);

        Optional<CompetencyGapAnalysis> sameSource = analyses
                .findByAgencyIdAndSubjectEmployeeIdAndAnalysisDateAndPositionProfileIdAndPersonProfileIdAndPrioritySchemeId(
                        agencyId, subject.employeeId(), analysisDate, positionProfile.getId(),
                        personProfile.getId(), scheme.getId());
        if (sameSource.isPresent()) return response(sameSource.get());

        CompetencyGapAnalysis analysis = analyses.saveAndFlush(new CompetencyGapAnalysis(agencyId, subject,
                positionProfile, personProfile, scheme, analysisDate, request.requestKey(), normalizedActor,
                Instant.now()));
        List<GapPriorityRule> priorityRules = rules
                .findBySchemeIdAndAgencyIdAndActiveTrueOrderByDisplayOrderAsc(scheme.getId(), agencyId);
        if (priorityRules.isEmpty()) throw new PublicationConflictException("The active priority scheme has no rules");

        List<PersonCompetencyResult> profileResults = personResults
                .findByPersonProfileIdOrderByCompetencyCode(personProfile.getId());
        Map<String, PersonCompetencyResult> resultByVersion = profileResults.stream()
                .collect(Collectors.toMap(result -> result.getCompetency().getId(), Function.identity()));
        Map<String, List<PersonCompetencyResult>> resultsByCode = profileResults.stream()
                .collect(Collectors.groupingBy(result -> result.getCompetency().getCode().toUpperCase(Locale.ROOT)));

        List<CompetencyGapItem> generatedItems = new ArrayList<>();
        for (PositionProfileRequirement requirement : requirements
                .findByProfileIdAndAgencyIdAndActiveTrueOrderByDisplayOrderAscIdAsc(positionProfile.getId(), agencyId)) {
            PersonCompetencyResult result = resultByVersion.get(requirement.getCompetency().getId());
            GapClassification classification;
            NotAssessedReason notAssessedReason = null;
            Integer gap = null;
            if (result == null || result.getAttainedLevel() == null) {
                classification = GapClassification.NOT_ASSESSED;
                notAssessedReason = result == null
                        && resultsByCode.containsKey(requirement.getCompetency().getCode().toUpperCase(Locale.ROOT))
                        ? NotAssessedReason.VERSION_NOT_COMPARABLE : NotAssessedReason.NO_RESULT;
                result = null;
            } else if (!requirement.getCompetency().getProficiencyScale().getId()
                    .equals(result.getAttainedLevel().getScale().getId())) {
                classification = GapClassification.NOT_ASSESSED;
                notAssessedReason = NotAssessedReason.VERSION_NOT_COMPARABLE;
                result = null;
            } else {
                gap = requirement.getRequiredProficiencyLevel().getLevelOrder()
                        - result.getAttainedLevel().getLevelOrder();
                classification = gap > 0 ? GapClassification.BELOW
                        : gap == 0 ? GapClassification.MEETS : GapClassification.EXCEEDS;
            }
            GapPriorityRule matchedRule = null;
            if (classification == GapClassification.BELOW || classification == GapClassification.NOT_ASSESSED) {
                Integer calculatedGap = gap;
                matchedRule = priorityRules.stream().filter(rule -> rule.matches(classification, calculatedGap,
                                requirement.getClassification(), requirement.getCriticalityCode()))
                        .findFirst().orElseThrow(() -> new PublicationConflictException(
                                "The active priority scheme does not cover " + classification));
            }
            generatedItems.add(new CompetencyGapItem(agencyId, analysis, requirement, result,
                    classification, notAssessedReason, gap, matchedRule));
        }
        if (generatedItems.isEmpty()) throw new PublicationConflictException(
                "The effective position profile has no active competency requirements");
        items.saveAll(generatedItems);
        items.flush();
        AnalysisResponse after = response(analysis);
        audit.record(agencyId, "GENERATE_GAP_ANALYSIS", "COMPETENCY_GAP_ANALYSIS", analysis.getId(),
                1, analysis.getVersion(), null, after, null, correlationId);
        return after;
    }

    @Override @Transactional(readOnly = true)
    public PageResponse<AnalysisSummaryResponse> list(String agencyId, String employeeNo,
                                                      GapClassification classification, String priorityCode,
                                                      int page, int size, String actor,
                                                      PermissionDataScope scope) {
        validatePage(page, size);
        String employeeFilter = employeeFilter(employeeNo, actor, scope);
        return PageResponse.from(analyses.search(agencyId, employeeFilter,
                        classification == null ? null : classification.name(), normalize(priorityCode),
                        PageRequest.of(page, size, Sort.by(Sort.Order.desc("analysisDate"),
                                Sort.Order.desc("generatedAt")))), this::summary);
    }

    @Override @Transactional(readOnly = true)
    public AnalysisResponse get(String agencyId, String id, String actor, PermissionDataScope scope) {
        CompetencyGapAnalysis analysis = analysis(agencyId, id);
        authorize(analysis.getSubjectEmployeeNo(), actor, scope);
        return response(analysis);
    }

    @Override @Transactional(readOnly = true)
    public AnalysisResponse latest(String agencyId, String employeeNo, String actor, PermissionDataScope scope) {
        authorize(employeeNo, actor, scope);
        return analyses.findFirstByAgencyIdAndSubjectEmployeeNoIgnoreCaseOrderByAnalysisDateDescGeneratedAtDesc(
                        agencyId, employeeNo)
                .map(this::response).orElseThrow(() -> new ResourceNotFoundException("No competency gap analysis was found"));
    }

    @Override @Transactional(readOnly = true)
    public PageResponse<AnalysisSummaryResponse> history(String agencyId, String employeeNo, int page, int size,
                                                         String actor, PermissionDataScope scope) {
        authorize(employeeNo, actor, scope);
        return list(agencyId, employeeNo, null, null, page, size, actor, scope);
    }

    private PositionProfile resolvePositionProfile(String agencyId, HumanResourceAssessmentSubject subject,
                                                   LocalDate date) {
        if (subject.plantillaId() != null) {
            PositionProfile plantilla = effectivePositionProfile(agencyId, PositionTargetType.PLANTILLA,
                    subject.jobPositionId(), subject.plantillaId(), date);
            if (plantilla != null) return plantilla;
        }
        PositionProfile position = effectivePositionProfile(agencyId, PositionTargetType.JOB_POSITION,
                subject.jobPositionId(), null, date);
        if (position == null) throw new ResourceNotFoundException(
                "No effective approved position profile was found for the current appointment");
        return position;
    }

    private PositionProfile effectivePositionProfile(String agencyId, PositionTargetType type,
                                                     Long jobPositionId, Long plantillaId, LocalDate date) {
        var page = positionProfiles.findAll(PositionProfileSpecifications.effective(
                agencyId, type, jobPositionId, plantillaId, date),
                PageRequest.of(0, 2, Sort.by("definitionVersion").descending()));
        if (page.getTotalElements() > 1) throw new PublicationConflictException(
                "More than one effective approved position profile was found");
        return page.isEmpty() ? null : page.getContent().get(0);
    }

    private GapPriorityScheme effectiveScheme(String agencyId, LocalDate date) {
        List<GapPriorityScheme> found = schemes.findEffective(agencyId, date, PageRequest.of(0, 2));
        if (found.isEmpty()) throw new ResourceNotFoundException("No active development priority scheme was found");
        if (found.size() > 1) throw new PublicationConflictException(
                "More than one development priority scheme is effective");
        return found.get(0);
    }

    private CompetencyGapAnalysis analysis(String agencyId, String id) {
        return analyses.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Competency gap analysis not found"));
    }

    private String employeeFilter(String requested, String actor, PermissionDataScope scope) {
        String normalizedActor = actor(actor);
        if (scope == PermissionDataScope.AGENCY_WIDE) return normalize(requested);
        if (scope == PermissionDataScope.OWN_RECORDS) {
            if (requested != null && !requested.isBlank() && !requested.equalsIgnoreCase(normalizedActor)) {
                throw new AccessDeniedException("The configured data scope permits only the authenticated employee");
            }
            return normalizedActor;
        }
        throw new AccessDeniedException("The configured data scope does not permit competency gap records");
    }

    private static void authorize(String employeeNo, String actor, PermissionDataScope scope) {
        String normalizedActor = actor(actor);
        if (scope == PermissionDataScope.AGENCY_WIDE || scope == PermissionDataScope.OWN_RECORDS
                && employeeNo.equalsIgnoreCase(normalizedActor)) return;
        throw new AccessDeniedException("The configured data scope does not permit this competency gap analysis");
    }

    private AnalysisSummaryResponse summary(CompetencyGapAnalysis analysis) {
        List<CompetencyGapItem> children = items.findByAnalysisIdOrderByDisplayOrderAscCompetencyCodeAsc(analysis.getId());
        return new AnalysisSummaryResponse(analysis.getId(), analysis.getSubjectEmployeeId(),
                analysis.getSubjectEmployeeNo(), analysis.getSubjectDisplayName(), analysis.getAnalysisDate(),
                analysis.getSourceJobPositionName(), analysis.getSourcePlantillaName(),
                analysis.getPositionProfileDefinitionVersion(), analysis.getPersonProfileVersion(),
                analysis.getPriorityScheme().getCode(), analysis.getPrioritySchemeDefinitionVersion(),
                count(children, GapClassification.BELOW), count(children, GapClassification.MEETS),
                count(children, GapClassification.EXCEEDS), count(children, GapClassification.NOT_ASSESSED),
                analysis.getGeneratedAt());
    }

    private AnalysisResponse response(CompetencyGapAnalysis analysis) {
        return new AnalysisResponse(analysis.getId(), analysis.getSubjectEmployeeId(),
                analysis.getSubjectEmployeeNo(), analysis.getSubjectDisplayName(), analysis.getAppointmentId(),
                analysis.getJobPositionId(), analysis.getPlantillaId(), analysis.getHrmSourceFingerprint(),
                analysis.getSourceJobPositionName(), analysis.getSourcePlantillaName(),
                analysis.getSourceSalaryGrade(), analysis.getSourceSalaryStep(), analysis.getPositionProfile().getId(),
                analysis.getPositionProfileDefinitionVersion(), analysis.getPositionProfileContentRevision(),
                analysis.getPersonProfile().getId(), analysis.getPersonProfileVersion(),
                analysis.getPersonProfileValidFrom(), analysis.getPersonProfileValidTo(),
                analysis.getPriorityScheme().getId(), analysis.getPriorityScheme().getCode(),
                analysis.getPrioritySchemeDefinitionVersion(), analysis.getAnalysisDate(), analysis.getRequestKey(),
                analysis.getGeneratedBy(), analysis.getGeneratedAt(),
                items.findByAnalysisIdOrderByDisplayOrderAscCompetencyCodeAsc(analysis.getId()).stream()
                        .map(this::itemResponse).toList());
    }

    private GapItemResponse itemResponse(CompetencyGapItem item) {
        return new GapItemResponse(item.getId(), item.getPositionRequirement().getId(), item.getCompetency().getId(),
                item.getCompetencyCode(), item.getCompetencyName(), item.getCompetencyDefinitionVersion(),
                item.getScaleId(), item.getScaleDefinitionVersion(), item.getRequiredLevel().getId(),
                item.getRequiredLevelCode(), item.getRequiredLevelLabel(), item.getRequiredLevelOrder(),
                item.getAttainedLevel() == null ? null : item.getAttainedLevel().getId(),
                item.getAttainedLevelCode(), item.getAttainedLevelLabel(), item.getAttainedLevelOrder(),
                item.getGapValue(), item.getGapClassification(), item.getNotAssessedReason(),
                item.getRequirementClassification(), item.getCriticalityCode(),
                item.getPriorityLevel() == null ? null : item.getPriorityLevel().getId(),
                item.getPriorityCode(), item.getPriorityLabel(), item.getPriorityRank(),
                item.getMatchedRule() == null ? null : item.getMatchedRule().getId(),
                item.getPriorityExplanation(), item.getDisplayOrder());
    }

    private static long count(List<CompetencyGapItem> items, GapClassification classification) {
        return items.stream().filter(item -> item.getGapClassification() == classification).count();
    }
    private static String actor(String actor) {
        if (actor == null || actor.isBlank()) throw new AccessDeniedException("Authenticated employee identity is required");
        return actor.trim();
    }
    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private static void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException(
                "page must be non-negative and size must be between 1 and 100");
    }
}
