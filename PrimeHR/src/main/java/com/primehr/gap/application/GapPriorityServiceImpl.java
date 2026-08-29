package com.primehr.gap.application;

import com.primehr.gap.api.GapPriorityDtos.*;
import com.primehr.gap.domain.*;
import com.primehr.gap.infrastructure.*;
import com.primehr.shared.api.PageResponse;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.OptimisticConflictException;
import com.primehr.shared.exception.PublicationConflictException;
import com.primehr.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class GapPriorityServiceImpl implements GapPriorityService {
    private static final Sort ORDER = Sort.by("displayOrder").ascending()
            .and(Sort.by("code").ascending()).and(Sort.by("definitionVersion").descending());

    private final GapPrioritySchemeRepository schemes;
    private final GapPriorityLevelRepository levels;
    private final GapPriorityRuleRepository rules;
    private final PrimeHrAuditService audit;

    public GapPriorityServiceImpl(GapPrioritySchemeRepository schemes, GapPriorityLevelRepository levels,
                                  GapPriorityRuleRepository rules, PrimeHrAuditService audit) {
        this.schemes = schemes;
        this.levels = levels;
        this.rules = rules;
        this.audit = audit;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SchemeSummaryResponse> list(String agencyId, GapPrioritySchemeStatus status,
                                                    int page, int size) {
        validatePage(page, size);
        Page<GapPriorityScheme> result = status == null
                ? schemes.findByAgencyId(agencyId, PageRequest.of(page, size, ORDER))
                : schemes.findByAgencyIdAndStatus(agencyId, status.name(), PageRequest.of(page, size, ORDER));
        return PageResponse.from(result, this::summary);
    }

    @Override @Transactional(readOnly = true)
    public SchemeResponse get(String agencyId, String id) { return response(scheme(agencyId, id)); }

    @Override
    public SchemeResponse create(String agencyId, CreateSchemeRequest request, String correlationId) {
        if (schemes.existsByAgencyIdAndCodeIgnoreCase(agencyId, request.code())) {
            throw new IllegalArgumentException("A gap priority scheme with this code already exists");
        }
        GapPriorityScheme entity = schemes.saveAndFlush(GapPriorityScheme.draft(agencyId, request.code(),
                request.name(), request.description(), request.displayOrder(), request.effectiveFrom(),
                request.effectiveTo()));
        SchemeResponse after = response(entity);
        audit.record(agencyId, "CREATE_DRAFT", "GAP_PRIORITY_SCHEME", entity.getId(),
                entity.getDefinitionVersion(), entity.getVersion(), null, after, null, correlationId);
        return after;
    }

    @Override
    public SchemeResponse update(String agencyId, String id, UpdateSchemeRequest request, String correlationId) {
        GapPriorityScheme entity = scheme(agencyId, id);
        requireVersion(entity.getVersion(), request.recordVersion());
        boolean duplicateCode = schemes.findChainForUpdate(agencyId, request.code()).stream()
                .anyMatch(candidate -> !candidate.getId().equals(id));
        if (!entity.getCode().equalsIgnoreCase(request.code()) && duplicateCode) {
            throw new IllegalArgumentException("A gap priority scheme with this code already exists");
        }
        SchemeResponse before = response(entity);
        entity.updateDraft(request.code(), request.name(), request.description(), request.displayOrder(),
                request.effectiveFrom(), request.effectiveTo());
        entity = schemes.saveAndFlush(entity);
        SchemeResponse after = response(entity);
        audit.record(agencyId, "UPDATE_DRAFT", "GAP_PRIORITY_SCHEME", id, entity.getDefinitionVersion(),
                entity.getVersion(), before, after, null, correlationId);
        return after;
    }

    @Override
    public SchemeResponse archive(String agencyId, String id, TransitionRequest request, String correlationId) {
        GapPriorityScheme entity = scheme(agencyId, id);
        requireVersion(entity.getVersion(), request.recordVersion());
        SchemeResponse before = response(entity);
        entity.archiveDraft();
        entity = schemes.saveAndFlush(entity);
        SchemeResponse after = response(entity);
        audit.record(agencyId, "ARCHIVE_DRAFT", "GAP_PRIORITY_SCHEME", id, entity.getDefinitionVersion(),
                entity.getVersion(), before, after, reason(request.reason()), correlationId);
        return after;
    }

    @Override
    public SchemeResponse createSuccessor(String agencyId, String id, TransitionRequest request,
                                          String correlationId) {
        GapPriorityScheme source = scheme(agencyId, id);
        requireVersion(source.getVersion(), request.recordVersion());
        if (schemes.existsByAgencyIdAndCodeIgnoreCaseAndStatus(
                agencyId, source.getCode(), GapPrioritySchemeStatus.DRAFT.name())) {
            throw new IllegalArgumentException("An unfinished priority scheme version already exists");
        }
        GapPriorityScheme successor = schemes.saveAndFlush(source.successorDraft());
        Map<String, GapPriorityLevel> copiedLevels = new HashMap<>();
        for (GapPriorityLevel level : activeLevels(source)) {
            GapPriorityLevel copied = levels.saveAndFlush(level.copyTo(successor));
            copiedLevels.put(level.getId(), copied);
        }
        for (GapPriorityRule rule : activeRules(source)) {
            GapPriorityLevel copied = copiedLevels.get(rule.getPriorityLevel().getId());
            if (copied == null) throw new PublicationConflictException("A priority rule references a missing level");
            rules.save(rule.copyTo(successor, copied));
        }
        rules.flush();
        SchemeResponse after = response(successor);
        audit.record(agencyId, "CREATE_SUCCESSOR_DRAFT", "GAP_PRIORITY_SCHEME", successor.getId(),
                successor.getDefinitionVersion(), successor.getVersion(), response(source), after,
                reason(request.reason()), correlationId);
        return after;
    }

    @Override
    public SchemeResponse publish(String agencyId, String id, PublishRequest request, String correlationId) {
        GapPriorityScheme initial = scheme(agencyId, id);
        List<GapPriorityScheme> chain = schemes.findChainForUpdate(agencyId, initial.getCode());
        GapPriorityScheme draft = chain.stream().filter(candidate -> candidate.getId().equals(id)).findFirst()
                .orElseThrow(() -> new PublicationConflictException("The priority scheme changed before publication"));
        requirePublicationVersion(draft.getVersion(), request.recordVersion());
        validateChain(chain, draft);
        validateCompleteness(draft);
        GapPriorityScheme predecessor = predecessor(chain, draft);
        List<GapPriorityScheme> active = schemes
                .findByAgencyIdAndStatusOrderByEffectiveFromAscDefinitionVersionAsc(
                        agencyId, GapPrioritySchemeStatus.ACTIVE.name());
        if (predecessor != null && (predecessor.getEffectiveTo() == null
                || !predecessor.getEffectiveTo().isBefore(draft.getEffectiveFrom()))) {
            SchemeResponse beforePredecessor = response(predecessor);
            predecessor.closeEffectivePeriodBefore(draft.getEffectiveFrom());
            predecessor = schemes.saveAndFlush(predecessor);
            audit.record(agencyId, "CLOSE_ACTIVE_EFFECTIVITY", "GAP_PRIORITY_SCHEME", predecessor.getId(),
                    predecessor.getDefinitionVersion(), predecessor.getVersion(), beforePredecessor,
                    response(predecessor), request.reason(), correlationId);
        }
        for (GapPriorityScheme current : active) {
            if (predecessor != null && current.getId().equals(predecessor.getId())) continue;
            if (rangesOverlap(current.getEffectiveFrom(), current.getEffectiveTo(),
                    draft.getEffectiveFrom(), draft.getEffectiveTo())) {
                throw new PublicationConflictException("Only one gap priority scheme may be effective for an agency");
            }
        }
        SchemeResponse before = response(draft);
        draft.publish(audit.currentActor(), Instant.now());
        draft = schemes.saveAndFlush(draft);
        SchemeResponse after = response(draft);
        audit.record(agencyId, "PUBLISH_DRAFT", "GAP_PRIORITY_SCHEME", id, draft.getDefinitionVersion(),
                draft.getVersion(), before, after, request.reason().trim(), correlationId);
        return after;
    }

    @Override
    public SchemeResponse addLevel(String agencyId, String schemeId, CreateLevelRequest request,
                                   String correlationId) {
        GapPriorityScheme scheme = scheme(agencyId, schemeId);
        requireDraft(scheme);
        ensureUniqueLevel(scheme, null, request.code(), request.priorityRank());
        GapPriorityLevel level = levels.saveAndFlush(new GapPriorityLevel(agencyId, scheme, request.code(),
                request.label(), request.description(), request.priorityRank(), request.displayOrder()));
        audit.record(agencyId, "CREATE_DRAFT_CHILD", "GAP_PRIORITY_LEVEL", level.getId(),
                scheme.getDefinitionVersion(), level.getVersion(), null, levelResponse(level), null, correlationId);
        return response(scheme);
    }

    @Override
    public SchemeResponse updateLevel(String agencyId, String schemeId, String levelId,
                                      UpdateLevelRequest request, String correlationId) {
        GapPriorityScheme scheme = scheme(agencyId, schemeId);
        GapPriorityLevel level = level(agencyId, schemeId, levelId);
        requireVersion(level.getVersion(), request.recordVersion());
        ensureUniqueLevel(scheme, levelId, request.code(), request.priorityRank());
        LevelResponse before = levelResponse(level);
        level.updateDraft(request.code(), request.label(), request.description(), request.priorityRank(),
                request.displayOrder());
        level = levels.saveAndFlush(level);
        audit.record(agencyId, "UPDATE_DRAFT_CHILD", "GAP_PRIORITY_LEVEL", levelId,
                scheme.getDefinitionVersion(), level.getVersion(), before, levelResponse(level), null, correlationId);
        return response(scheme);
    }

    @Override
    public SchemeResponse archiveLevel(String agencyId, String schemeId, String levelId,
                                       TransitionRequest request, String correlationId) {
        GapPriorityScheme scheme = scheme(agencyId, schemeId);
        GapPriorityLevel level = level(agencyId, schemeId, levelId);
        requireVersion(level.getVersion(), request.recordVersion());
        if (activeRules(scheme).stream().anyMatch(rule -> rule.getPriorityLevel().getId().equals(levelId))) {
            throw new IllegalArgumentException("Archive priority rules that use this level first");
        }
        LevelResponse before = levelResponse(level);
        level.archiveDraft();
        level = levels.saveAndFlush(level);
        audit.record(agencyId, "ARCHIVE_DRAFT_CHILD", "GAP_PRIORITY_LEVEL", levelId,
                scheme.getDefinitionVersion(), level.getVersion(), before, levelResponse(level),
                reason(request.reason()), correlationId);
        return response(scheme);
    }

    @Override
    public SchemeResponse addRule(String agencyId, String schemeId, CreateRuleRequest request,
                                  String correlationId) {
        GapPriorityScheme scheme = scheme(agencyId, schemeId);
        requireDraft(scheme);
        ensureUniqueRuleOrder(scheme, null, request.displayOrder());
        GapPriorityLevel level = level(agencyId, schemeId, request.priorityLevelId());
        GapPriorityRule rule = rules.saveAndFlush(new GapPriorityRule(agencyId, scheme,
                request.gapClassification(), request.minimumGap(), request.maximumGap(),
                request.requirementClassification(), request.criticalityCode(), level,
                request.explanation(), request.displayOrder()));
        audit.record(agencyId, "CREATE_DRAFT_CHILD", "GAP_PRIORITY_RULE", rule.getId(),
                scheme.getDefinitionVersion(), rule.getVersion(), null, ruleResponse(rule), null, correlationId);
        return response(scheme);
    }

    @Override
    public SchemeResponse updateRule(String agencyId, String schemeId, String ruleId,
                                     UpdateRuleRequest request, String correlationId) {
        GapPriorityScheme scheme = scheme(agencyId, schemeId);
        GapPriorityRule rule = rule(agencyId, schemeId, ruleId);
        requireVersion(rule.getVersion(), request.recordVersion());
        ensureUniqueRuleOrder(scheme, ruleId, request.displayOrder());
        GapPriorityLevel level = level(agencyId, schemeId, request.priorityLevelId());
        RuleResponse before = ruleResponse(rule);
        rule.updateDraft(request.gapClassification(), request.minimumGap(), request.maximumGap(),
                request.requirementClassification(), request.criticalityCode(), level,
                request.explanation(), request.displayOrder());
        rule = rules.saveAndFlush(rule);
        audit.record(agencyId, "UPDATE_DRAFT_CHILD", "GAP_PRIORITY_RULE", ruleId,
                scheme.getDefinitionVersion(), rule.getVersion(), before, ruleResponse(rule), null, correlationId);
        return response(scheme);
    }

    @Override
    public SchemeResponse archiveRule(String agencyId, String schemeId, String ruleId,
                                      TransitionRequest request, String correlationId) {
        GapPriorityScheme scheme = scheme(agencyId, schemeId);
        GapPriorityRule rule = rule(agencyId, schemeId, ruleId);
        requireVersion(rule.getVersion(), request.recordVersion());
        RuleResponse before = ruleResponse(rule);
        rule.archiveDraft();
        rule = rules.saveAndFlush(rule);
        audit.record(agencyId, "ARCHIVE_DRAFT_CHILD", "GAP_PRIORITY_RULE", ruleId,
                scheme.getDefinitionVersion(), rule.getVersion(), before, ruleResponse(rule),
                reason(request.reason()), correlationId);
        return response(scheme);
    }

    private void validateCompleteness(GapPriorityScheme scheme) {
        if (scheme.getEffectiveFrom() == null) throw new IllegalArgumentException("effectiveFrom is required before publication");
        List<GapPriorityLevel> activeLevels = activeLevels(scheme);
        List<GapPriorityRule> activeRules = activeRules(scheme);
        if (activeLevels.isEmpty()) throw new IllegalArgumentException("At least one active priority level is required");
        if (activeRules.isEmpty()) throw new IllegalArgumentException("At least one active priority rule is required");
        if (activeRules.stream().noneMatch(rule -> rule.isFallbackFor(GapClassification.BELOW))) {
            throw new IllegalArgumentException("A fallback BELOW priority rule is required");
        }
        if (activeRules.stream().noneMatch(rule -> rule.isFallbackFor(GapClassification.NOT_ASSESSED))) {
            throw new IllegalArgumentException("A fallback NOT_ASSESSED priority rule is required");
        }
        Set<String> activeLevelIds = activeLevels.stream().map(GapPriorityLevel::getId).collect(Collectors.toSet());
        Set<String> signatures = new HashSet<>();
        for (GapPriorityRule rule : activeRules) {
            if (rule.getPriorityLevel() == null || !activeLevelIds.contains(rule.getPriorityLevel().getId())) {
                throw new IllegalArgumentException("Every active priority rule must use an active scheme level");
            }
            String signature = java.util.stream.Stream.of(rule.getGapClassification(), rule.getMinimumGap(),
                            rule.getMaximumGap(), rule.getRequirementClassification(), normalize(rule.getCriticalityCode()))
                    .map(value -> java.util.Objects.toString(value, ""))
                    .collect(java.util.stream.Collectors.joining("|"));
            if (!signatures.add(signature)) throw new IllegalArgumentException("Ambiguous duplicate priority rules are not allowed");
        }
    }

    private static void validateChain(List<GapPriorityScheme> chain, GapPriorityScheme draft) {
        if (!draft.isDraft()) throw new com.primehr.shared.exception.IllegalLifecycleTransitionException(
                "Only DRAFT gap priority schemes may be published");
        if (chain.stream().filter(GapPriorityScheme::isDraft).count() != 1) {
            throw new PublicationConflictException("Exactly one draft priority scheme version must exist");
        }
        GapPriorityScheme latest = chain.stream().filter(item -> item.getStatus() == GapPrioritySchemeStatus.ACTIVE)
                .max(Comparator.comparingInt(GapPriorityScheme::getDefinitionVersion)).orElse(null);
        if (draft.getSupersedesId() == null) {
            if (draft.getDefinitionVersion() != 1 || latest != null) throw new PublicationConflictException(
                    "A first priority scheme version cannot bypass active history");
        } else if (latest == null || !latest.getId().equals(draft.getSupersedesId())
                || draft.getDefinitionVersion() != latest.getDefinitionVersion() + 1) {
            throw new PublicationConflictException("The draft does not supersede the latest active version");
        } else if (!draft.getEffectiveFrom().isAfter(latest.getEffectiveFrom())) {
            throw new IllegalArgumentException("A successor effectiveFrom must be after its predecessor effectiveFrom");
        }
    }

    private static GapPriorityScheme predecessor(List<GapPriorityScheme> chain, GapPriorityScheme draft) {
        if (draft.getSupersedesId() == null) return null;
        return chain.stream().filter(item -> item.getId().equals(draft.getSupersedesId())
                && item.getStatus() == GapPrioritySchemeStatus.ACTIVE).findFirst()
                .orElseThrow(() -> new PublicationConflictException("The active predecessor was not found"));
    }

    private void ensureUniqueLevel(GapPriorityScheme scheme, String currentId, String code, int rank) {
        for (GapPriorityLevel candidate : levels.findBySchemeIdAndAgencyIdOrderByDisplayOrderAscPriorityRankAsc(
                scheme.getId(), scheme.getAgencyId())) {
            if (candidate.getId().equals(currentId)) continue;
            if (candidate.getCode().equalsIgnoreCase(code)) throw new IllegalArgumentException("Duplicate priority level code");
            if (candidate.getPriorityRank() == rank) throw new IllegalArgumentException("Duplicate priority rank");
        }
    }

    private void ensureUniqueRuleOrder(GapPriorityScheme scheme, String currentId, int displayOrder) {
        for (GapPriorityRule candidate : rules.findBySchemeIdAndAgencyIdOrderByDisplayOrderAsc(
                scheme.getId(), scheme.getAgencyId())) {
            if (!candidate.getId().equals(currentId) && candidate.getDisplayOrder() == displayOrder) {
                throw new IllegalArgumentException("Duplicate priority rule display order");
            }
        }
    }

    private GapPriorityScheme scheme(String agencyId, String id) {
        return schemes.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Gap priority scheme not found"));
    }
    private GapPriorityLevel level(String agencyId, String schemeId, String id) {
        return levels.findByIdAndSchemeIdAndAgencyId(id, schemeId, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Gap priority level not found"));
    }
    private GapPriorityRule rule(String agencyId, String schemeId, String id) {
        return rules.findByIdAndSchemeIdAndAgencyId(id, schemeId, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Gap priority rule not found"));
    }
    private List<GapPriorityLevel> activeLevels(GapPriorityScheme scheme) {
        return levels.findBySchemeIdAndAgencyIdAndActiveTrueOrderByDisplayOrderAscPriorityRankAsc(
                scheme.getId(), scheme.getAgencyId());
    }
    private List<GapPriorityRule> activeRules(GapPriorityScheme scheme) {
        return rules.findBySchemeIdAndAgencyIdAndActiveTrueOrderByDisplayOrderAsc(
                scheme.getId(), scheme.getAgencyId());
    }
    private static void requireDraft(GapPriorityScheme scheme) {
        if (!scheme.isDraft()) throw new com.primehr.shared.exception.IllegalLifecycleTransitionException(
                "Children may be changed only on a DRAFT gap priority scheme");
    }
    private static void requireVersion(long actual, Long expected) {
        if (expected == null || actual != expected) throw new OptimisticConflictException(
                "Expected recordVersion " + expected + " but current version is " + actual);
    }
    private static void requirePublicationVersion(long actual, Long expected) {
        if (expected == null || actual != expected) throw new PublicationConflictException(
                "Expected recordVersion " + expected + " but current version is " + actual);
    }
    private static boolean rangesOverlap(LocalDate leftFrom, LocalDate leftTo, LocalDate rightFrom, LocalDate rightTo) {
        return !(leftTo != null && leftTo.isBefore(rightFrom) || rightTo != null && rightTo.isBefore(leftFrom));
    }
    private static String reason(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("A reason is required");
        return value.trim();
    }
    private static String normalize(String value) { return value == null ? null : value.trim().toUpperCase(Locale.ROOT); }
    private static void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException(
                "page must be non-negative and size must be between 1 and 100");
    }

    private SchemeSummaryResponse summary(GapPriorityScheme item) {
        return new SchemeSummaryResponse(item.getId(), item.getCode(), item.getName(), item.getStatus(),
                item.getDefinitionVersion(), item.getSupersedesId(), item.getEffectiveFrom(),
                item.getEffectiveTo(), item.getVersion());
    }
    private SchemeResponse response(GapPriorityScheme item) {
        return new SchemeResponse(item.getId(), item.getCode(), item.getName(), item.getDescription(),
                item.getStatus(), item.getDefinitionVersion(), item.getSupersedesId(), item.getDisplayOrder(),
                item.getEffectiveFrom(), item.getEffectiveTo(), item.getVersion(), item.getPublishedBy(),
                item.getPublishedAt(), levels.findBySchemeIdAndAgencyIdOrderByDisplayOrderAscPriorityRankAsc(
                        item.getId(), item.getAgencyId()).stream().map(this::levelResponse).toList(),
                rules.findBySchemeIdAndAgencyIdOrderByDisplayOrderAsc(item.getId(), item.getAgencyId()).stream()
                        .map(this::ruleResponse).toList());
    }
    private LevelResponse levelResponse(GapPriorityLevel item) {
        return new LevelResponse(item.getId(), item.getCode(), item.getLabel(), item.getDescription(),
                item.getPriorityRank(), item.isActive(), item.getDisplayOrder(), item.getVersion());
    }
    private RuleResponse ruleResponse(GapPriorityRule item) {
        GapPriorityLevel level = item.getPriorityLevel();
        return new RuleResponse(item.getId(), item.getGapClassification(), item.getMinimumGap(),
                item.getMaximumGap(), item.getRequirementClassification(), item.getCriticalityCode(),
                level.getId(), level.getCode(), level.getLabel(), level.getPriorityRank(), item.getExplanation(),
                item.isActive(), item.getDisplayOrder(), item.getVersion());
    }
}
