package com.primehr.competency.application;

import com.primehr.competency.api.*;
import com.primehr.competency.domain.*;
import com.primehr.competency.infrastructure.*;
import com.primehr.shared.api.PageResponse;
import com.primehr.shared.audit.*;
import com.primehr.shared.exception.OptimisticConflictException;
import com.primehr.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDate;

@Service
@Transactional
public class CompetencyAdminServiceImpl implements CompetencyAdminService {
    private static final Sort ORDER = Sort.by("displayOrder").ascending().and(Sort.by("code").ascending());

    private final CompetencyCategoryRepository categories;
    private final ProficiencyScaleRepository scales;
    private final ProficiencyLevelRepository levels;
    private final CompetencyRepository competencies;
    private final BehavioralIndicatorRepository indicators;
    private final PrimeHrAuditEventRepository auditEvents;
    private final PrimeHrAuditService audit;

    public CompetencyAdminServiceImpl(CompetencyCategoryRepository categories,
                                      ProficiencyScaleRepository scales,
                                      ProficiencyLevelRepository levels,
                                      CompetencyRepository competencies,
                                      BehavioralIndicatorRepository indicators,
                                      PrimeHrAuditEventRepository auditEvents,
                                      PrimeHrAuditService audit) {
        this.categories = categories;
        this.scales = scales;
        this.levels = levels;
        this.competencies = competencies;
        this.indicators = indicators;
        this.auditEvents = auditEvents;
        this.audit = audit;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminCategoryResponse> listCategories(String agency, DefinitionStatus status, String search,
                                                              LocalDate asOf, int page, int size) {
        validatePage(page, size);
        return PageResponse.from(categories.findAll(CompetencySpecifications.adminFilter(agency, status, search, asOf),
                PageRequest.of(page, size, ORDER)), this::categoryResponse);
    }

    @Override
    public AdminCategoryResponse createCategory(String agency, DraftCategoryRequest request, String correlationId) {
        rejectVersionOnCreate(request.recordVersion());
        if (categories.existsByAgencyIdAndCodeIgnoreCase(agency, request.code())) {
            throw new IllegalArgumentException("A competency category with this code already exists");
        }
        CompetencyCategory entity = CompetencyCategory.draft(agency, request.code(), request.name(),
                request.description(), request.displayOrder(), request.effectiveFrom(), request.effectiveTo());
        entity = categories.saveAndFlush(entity);
        AdminCategoryResponse after = categoryResponse(entity);
        audit.record(agency, "CREATE_DRAFT", "COMPETENCY_CATEGORY", entity.getId(),
                entity.getDefinitionVersion(), entity.getVersion(), null, after, null, correlationId);
        return after;
    }

    @Override
    public AdminCategoryResponse updateCategory(String agency, String id, DraftCategoryRequest request,
                                                String correlationId) {
        CompetencyCategory entity = category(agency, id);
        requireVersion(entity.getVersion(), request.recordVersion());
        if (!entity.getCode().equalsIgnoreCase(request.code())
                && categories.existsByAgencyIdAndCodeIgnoreCase(agency, request.code())) {
            throw new IllegalArgumentException("A competency category with this code already exists");
        }
        AdminCategoryResponse before = categoryResponse(entity);
        entity.updateDraft(request.code(), request.name(), request.description(), request.displayOrder(),
                request.effectiveFrom(), request.effectiveTo());
        entity = categories.saveAndFlush(entity);
        AdminCategoryResponse after = categoryResponse(entity);
        audit.record(agency, "UPDATE_DRAFT", "COMPETENCY_CATEGORY", id, entity.getDefinitionVersion(),
                entity.getVersion(), before, after, null, correlationId);
        return after;
    }

    @Override
    public AdminCategoryResponse versionCategory(String agency, String id, DraftTransitionRequest request,
                                                 String correlationId) {
        CompetencyCategory source = category(agency, id);
        requireVersion(source.getVersion(), request.recordVersion());
        ensureNoDraft(categories.existsByAgencyIdAndCodeIgnoreCaseAndStatus(
                agency, source.getCode(), DefinitionStatus.DRAFT.name()));
        CompetencyCategory successor = categories.saveAndFlush(source.successorDraft());
        AdminCategoryResponse after = categoryResponse(successor);
        audit.record(agency, "CREATE_SUCCESSOR_DRAFT", "COMPETENCY_CATEGORY", successor.getId(),
                successor.getDefinitionVersion(), successor.getVersion(), categoryResponse(source), after,
                request.reason(), correlationId);
        return after;
    }

    @Override
    public AdminCategoryResponse archiveCategory(String agency, String id, DraftTransitionRequest request,
                                                 String correlationId) {
        CompetencyCategory entity = category(agency, id);
        requireVersion(entity.getVersion(), request.recordVersion());
        AdminCategoryResponse before = categoryResponse(entity);
        entity.archiveDraft();
        entity = categories.saveAndFlush(entity);
        AdminCategoryResponse after = categoryResponse(entity);
        audit.record(agency, "ARCHIVE_DRAFT", "COMPETENCY_CATEGORY", id, entity.getDefinitionVersion(),
                entity.getVersion(), before, after, request.reason(), correlationId);
        return after;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminScaleResponse> listScales(String agency, DefinitionStatus status, String search,
                                                       LocalDate asOf, int page, int size) {
        validatePage(page, size);
        return PageResponse.from(scales.findAll(CompetencySpecifications.adminFilter(agency, status, search, asOf),
                PageRequest.of(page, size, ORDER)), this::scaleResponse);
    }

    @Override
    public AdminScaleResponse createScale(String agency, DraftScaleRequest request, String correlationId) {
        rejectVersionOnCreate(request.recordVersion());
        if (scales.existsByAgencyIdAndCodeIgnoreCase(agency, request.code())) {
            throw new IllegalArgumentException("A proficiency scale with this code already exists");
        }
        ProficiencyScale entity = ProficiencyScale.draft(agency, request.code(), request.name(), request.description(),
                request.displayOrder(), request.effectiveFrom(), request.effectiveTo());
        entity = scales.saveAndFlush(entity);
        AdminScaleResponse after = scaleResponse(entity);
        audit.record(agency, "CREATE_DRAFT", "PROFICIENCY_SCALE", entity.getId(), entity.getDefinitionVersion(),
                entity.getVersion(), null, after, null, correlationId);
        return after;
    }

    @Override
    public AdminScaleResponse updateScale(String agency, String id, DraftScaleRequest request, String correlationId) {
        ProficiencyScale entity = scale(agency, id);
        requireVersion(entity.getVersion(), request.recordVersion());
        if (!entity.getCode().equalsIgnoreCase(request.code())
                && scales.existsByAgencyIdAndCodeIgnoreCase(agency, request.code())) {
            throw new IllegalArgumentException("A proficiency scale with this code already exists");
        }
        AdminScaleResponse before = scaleResponse(entity);
        entity.updateDraft(request.code(), request.name(), request.description(), request.displayOrder(),
                request.effectiveFrom(), request.effectiveTo());
        entity = scales.saveAndFlush(entity);
        AdminScaleResponse after = scaleResponse(entity);
        audit.record(agency, "UPDATE_DRAFT", "PROFICIENCY_SCALE", id, entity.getDefinitionVersion(),
                entity.getVersion(), before, after, null, correlationId);
        return after;
    }

    @Override
    public AdminScaleResponse versionScale(String agency, String id, DraftTransitionRequest request,
                                           String correlationId) {
        ProficiencyScale source = scale(agency, id);
        requireVersion(source.getVersion(), request.recordVersion());
        ensureNoDraft(scales.existsByAgencyIdAndCodeIgnoreCaseAndStatus(
                agency, source.getCode(), DefinitionStatus.DRAFT.name()));
        ProficiencyScale successor = scales.saveAndFlush(source.successorDraft());
        AdminScaleResponse after = scaleResponse(successor);
        audit.record(agency, "CREATE_SUCCESSOR_DRAFT", "PROFICIENCY_SCALE", successor.getId(),
                successor.getDefinitionVersion(), successor.getVersion(), scaleResponse(source), after,
                request.reason(), correlationId);
        return after;
    }

    @Override
    public AdminScaleResponse archiveScale(String agency, String id, DraftTransitionRequest request,
                                           String correlationId) {
        ProficiencyScale entity = scale(agency, id);
        requireVersion(entity.getVersion(), request.recordVersion());
        AdminScaleResponse before = scaleResponse(entity);
        entity.archiveDraft();
        entity = scales.saveAndFlush(entity);
        AdminScaleResponse after = scaleResponse(entity);
        audit.record(agency, "ARCHIVE_DRAFT", "PROFICIENCY_SCALE", id, entity.getDefinitionVersion(),
                entity.getVersion(), before, after, request.reason(), correlationId);
        return after;
    }

    @Override
    public AdminLevelResponse createLevel(String agency, String scaleId, DraftLevelRequest request,
                                          String correlationId) {
        rejectVersionOnCreate(request.recordVersion());
        ProficiencyScale scale = scale(agency, scaleId);
        if (!scale.isDraft()) throw new com.primehr.shared.exception.IllegalLifecycleTransitionException(
                "Proficiency levels may be added only to a draft scale");
        ProficiencyLevel level = new ProficiencyLevel(agency, request.code(), request.label(), request.levelOrder(),
                request.description(), true, request.effectiveFrom(), request.effectiveTo());
        scale.addLevel(level);
        scales.saveAndFlush(scale);
        AdminLevelResponse after = levelResponse(level);
        audit.record(agency, "CREATE_DRAFT_CHILD", "PROFICIENCY_LEVEL", level.getId(),
                scale.getDefinitionVersion(), level.getVersion(), null, after, null, correlationId);
        return after;
    }

    @Override
    public AdminLevelResponse updateLevel(String agency, String scaleId, String levelId,
                                          DraftLevelRequest request, String correlationId) {
        ProficiencyLevel level = level(agency, scaleId, levelId);
        requireVersion(level.getVersion(), request.recordVersion());
        AdminLevelResponse before = levelResponse(level);
        level.updateDraft(request.code(), request.label(), request.levelOrder(), request.description(),
                request.effectiveFrom(), request.effectiveTo());
        level = levels.saveAndFlush(level);
        AdminLevelResponse after = levelResponse(level);
        audit.record(agency, "UPDATE_DRAFT_CHILD", "PROFICIENCY_LEVEL", levelId,
                level.getScale().getDefinitionVersion(), level.getVersion(), before, after, null, correlationId);
        return after;
    }

    @Override
    public AdminLevelResponse archiveLevel(String agency, String scaleId, String levelId,
                                           DraftTransitionRequest request, String correlationId) {
        ProficiencyLevel level = level(agency, scaleId, levelId);
        requireVersion(level.getVersion(), request.recordVersion());
        AdminLevelResponse before = levelResponse(level);
        level.archiveDraft();
        level = levels.saveAndFlush(level);
        AdminLevelResponse after = levelResponse(level);
        audit.record(agency, "ARCHIVE_DRAFT_CHILD", "PROFICIENCY_LEVEL", levelId,
                level.getScale().getDefinitionVersion(), level.getVersion(), before, after,
                request.reason(), correlationId);
        return after;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminCompetencyResponse> listCompetencies(String agency, DefinitionStatus status,
                                                                  String categoryId, String search, LocalDate asOf,
                                                                  int page, int size) {
        validatePage(page, size);
        return PageResponse.from(competencies.findAll(CompetencySpecifications.adminCompetencyFilter(
                        agency, status, categoryId, search, asOf),
                PageRequest.of(page, size, ORDER)), this::competencyResponse);
    }

    @Override
    public AdminCompetencyResponse createCompetency(String agency, DraftCompetencyRequest request,
                                                     String correlationId) {
        rejectVersionOnCreate(request.recordVersion());
        if (competencies.existsByAgencyIdAndCodeIgnoreCase(agency, request.code())) {
            throw new IllegalArgumentException("A competency with this code already exists");
        }
        Competency entity = Competency.draft(agency, request.code(), request.name(), request.definition(),
                availableCategory(agency, request.categoryId()), availableScale(agency, request.proficiencyScaleId()),
                request.displayOrder(), request.effectiveFrom(), request.effectiveTo());
        entity = competencies.saveAndFlush(entity);
        AdminCompetencyResponse after = competencyResponse(entity);
        audit.record(agency, "CREATE_DRAFT", "COMPETENCY", entity.getId(), entity.getDefinitionVersion(),
                entity.getVersion(), null, after, null, correlationId);
        return after;
    }

    @Override
    public AdminCompetencyResponse updateCompetency(String agency, String id, DraftCompetencyRequest request,
                                                     String correlationId) {
        Competency entity = competency(agency, id);
        requireVersion(entity.getVersion(), request.recordVersion());
        if (!entity.getCode().equalsIgnoreCase(request.code())
                && competencies.existsByAgencyIdAndCodeIgnoreCase(agency, request.code())) {
            throw new IllegalArgumentException("A competency with this code already exists");
        }
        AdminCompetencyResponse before = competencyResponse(entity);
        entity.updateDraft(request.code(), request.name(), request.definition(),
                availableCategory(agency, request.categoryId()), availableScale(agency, request.proficiencyScaleId()),
                request.displayOrder(), request.effectiveFrom(),
                request.effectiveTo());
        entity = competencies.saveAndFlush(entity);
        AdminCompetencyResponse after = competencyResponse(entity);
        audit.record(agency, "UPDATE_DRAFT", "COMPETENCY", id, entity.getDefinitionVersion(), entity.getVersion(),
                before, after, null, correlationId);
        return after;
    }

    @Override
    public AdminCompetencyResponse versionCompetency(String agency, String id, DraftTransitionRequest request,
                                                      String correlationId) {
        Competency source = competency(agency, id);
        requireVersion(source.getVersion(), request.recordVersion());
        ensureNoDraft(competencies.existsByAgencyIdAndCodeIgnoreCaseAndStatus(
                agency, source.getCode(), DefinitionStatus.DRAFT.name()));
        Competency successor = competencies.saveAndFlush(source.successorDraft());
        List<BehavioralIndicator> sourceIndicators = indicators
                .findByCompetencyIdAndAgencyIdOrderByProficiencyLevelLevelOrderAscDisplayOrderAsc(id, agency);
        indicators.saveAllAndFlush(sourceIndicators.stream().map(item -> item.copyForDraft(successor)).toList());
        AdminCompetencyResponse after = competencyResponse(successor);
        audit.record(agency, "CREATE_SUCCESSOR_DRAFT", "COMPETENCY", successor.getId(),
                successor.getDefinitionVersion(), successor.getVersion(), competencyResponse(source), after,
                request.reason(), correlationId);
        return after;
    }

    @Override
    public AdminCompetencyResponse archiveCompetency(String agency, String id, DraftTransitionRequest request,
                                                      String correlationId) {
        Competency entity = competency(agency, id);
        requireVersion(entity.getVersion(), request.recordVersion());
        AdminCompetencyResponse before = competencyResponse(entity);
        entity.archiveDraft();
        entity = competencies.saveAndFlush(entity);
        AdminCompetencyResponse after = competencyResponse(entity);
        audit.record(agency, "ARCHIVE_DRAFT", "COMPETENCY", id, entity.getDefinitionVersion(), entity.getVersion(),
                before, after, request.reason(), correlationId);
        return after;
    }

    @Override
    public AdminIndicatorResponse createIndicator(String agency, String competencyId, DraftIndicatorRequest request,
                                                   String correlationId) {
        rejectVersionOnCreate(request.recordVersion());
        Competency competency = competency(agency, competencyId);
        if (!competency.isDraft()) throw new com.primehr.shared.exception.IllegalLifecycleTransitionException(
                "Behavioral indicators may be added only to a draft competency");
        ProficiencyLevel level = levels.findByIdAndAgencyId(request.proficiencyLevelId(), agency)
                .orElseThrow(() -> new ResourceNotFoundException("Proficiency level was not found"));
        requireAvailableLevel(level);
        BehavioralIndicator entity = new BehavioralIndicator(agency, competency, level,
                request.behaviorDescription(), request.evidenceGuidance(), true, request.displayOrder(),
                request.effectiveFrom(), request.effectiveTo());
        entity = indicators.saveAndFlush(entity);
        AdminIndicatorResponse after = indicatorResponse(entity);
        audit.record(agency, "CREATE_DRAFT_CHILD", "BEHAVIORAL_INDICATOR", entity.getId(),
                competency.getDefinitionVersion(), entity.getVersion(), null, after, null, correlationId);
        return after;
    }

    @Override
    public AdminIndicatorResponse updateIndicator(String agency, String competencyId, String indicatorId,
                                                   DraftIndicatorRequest request, String correlationId) {
        BehavioralIndicator entity = indicator(agency, competencyId, indicatorId);
        requireVersion(entity.getVersion(), request.recordVersion());
        ProficiencyLevel level = levels.findByIdAndAgencyId(request.proficiencyLevelId(), agency)
                .orElseThrow(() -> new ResourceNotFoundException("Proficiency level was not found"));
        requireAvailableLevel(level);
        AdminIndicatorResponse before = indicatorResponse(entity);
        entity.updateDraft(level, request.behaviorDescription(), request.evidenceGuidance(), request.displayOrder(),
                request.effectiveFrom(), request.effectiveTo());
        entity = indicators.saveAndFlush(entity);
        AdminIndicatorResponse after = indicatorResponse(entity);
        audit.record(agency, "UPDATE_DRAFT_CHILD", "BEHAVIORAL_INDICATOR", indicatorId,
                entity.getCompetency().getDefinitionVersion(), entity.getVersion(), before, after, null, correlationId);
        return after;
    }

    @Override
    public AdminIndicatorResponse archiveIndicator(String agency, String competencyId, String indicatorId,
                                                    DraftTransitionRequest request, String correlationId) {
        BehavioralIndicator entity = indicator(agency, competencyId, indicatorId);
        requireVersion(entity.getVersion(), request.recordVersion());
        AdminIndicatorResponse before = indicatorResponse(entity);
        entity.archiveDraft();
        entity = indicators.saveAndFlush(entity);
        AdminIndicatorResponse after = indicatorResponse(entity);
        audit.record(agency, "ARCHIVE_DRAFT_CHILD", "BEHAVIORAL_INDICATOR", indicatorId,
                entity.getCompetency().getDefinitionVersion(), entity.getVersion(), before, after,
                request.reason(), correlationId);
        return after;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditEventResponse> listAuditEvents(String agency, String aggregateType, String aggregateId,
                                                            int page, int size) {
        validatePage(page, size);
        if (aggregateType == null || aggregateType.isBlank() || aggregateId == null || aggregateId.isBlank()) {
            throw new IllegalArgumentException("aggregateType and aggregateId are required");
        }
        return PageResponse.from(auditEvents.findByAgencyIdAndAggregateTypeAndAggregateId(agency,
                aggregateType.trim().toUpperCase(), aggregateId.trim(),
                PageRequest.of(page, size, Sort.by("occurredAt").descending())), AuditEventResponse::from);
    }

    private CompetencyCategory category(String agency, String id) {
        return categories.findByIdAndAgencyId(id, agency)
                .orElseThrow(() -> new ResourceNotFoundException("Competency category was not found"));
    }

    private ProficiencyScale scale(String agency, String id) {
        return scales.findByIdAndAgencyId(id, agency)
                .orElseThrow(() -> new ResourceNotFoundException("Proficiency scale was not found"));
    }

    private CompetencyCategory availableCategory(String agency, String id) {
        CompetencyCategory category = category(agency, id);
        if (category.getStatus() == DefinitionStatus.ARCHIVED) {
            throw new IllegalArgumentException("An archived competency category cannot be assigned");
        }
        return category;
    }

    private ProficiencyScale availableScale(String agency, String id) {
        ProficiencyScale scale = scale(agency, id);
        if (scale.getStatus() == DefinitionStatus.ARCHIVED) {
            throw new IllegalArgumentException("An archived proficiency scale cannot be assigned");
        }
        return scale;
    }

    private static void requireAvailableLevel(ProficiencyLevel level) {
        if (!level.isActive()) {
            throw new IllegalArgumentException("An archived proficiency level cannot be assigned");
        }
    }

    private ProficiencyLevel level(String agency, String scaleId, String id) {
        return levels.findByIdAndScaleIdAndAgencyId(id, scaleId, agency)
                .orElseThrow(() -> new ResourceNotFoundException("Proficiency level was not found"));
    }

    private Competency competency(String agency, String id) {
        return competencies.findByIdAndAgencyId(id, agency)
                .orElseThrow(() -> new ResourceNotFoundException("Competency was not found"));
    }

    private BehavioralIndicator indicator(String agency, String competencyId, String id) {
        return indicators.findByIdAndCompetencyIdAndAgencyId(id, competencyId, agency)
                .orElseThrow(() -> new ResourceNotFoundException("Behavioral indicator was not found"));
    }

    private AdminCategoryResponse categoryResponse(CompetencyCategory item) {
        return new AdminCategoryResponse(item.getId(), item.getCode(), item.getName(), item.getDescription(),
                item.getStatus().name(), item.getDefinitionVersion(), item.getSupersedesId(), item.getDisplayOrder(),
                item.getEffectiveFrom(), item.getEffectiveTo(), item.getVersion());
    }

    private AdminScaleResponse scaleResponse(ProficiencyScale item) {
        return new AdminScaleResponse(item.getId(), item.getCode(), item.getName(), item.getDescription(),
                item.getStatus().name(), item.getDefinitionVersion(), item.getSupersedesId(), item.getDisplayOrder(),
                item.getEffectiveFrom(), item.getEffectiveTo(), item.getVersion(),
                item.getLevels().stream().map(this::levelResponse).toList());
    }

    private AdminLevelResponse levelResponse(ProficiencyLevel item) {
        return new AdminLevelResponse(item.getId(), item.getCode(), item.getLabel(), item.getLevelOrder(),
                item.getDescription(), item.isActive(), item.getEffectiveFrom(), item.getEffectiveTo(), item.getVersion());
    }

    private AdminCompetencyResponse competencyResponse(Competency item) {
        List<AdminIndicatorResponse> children = item.getId() == null ? List.of() : indicators
                .findByCompetencyIdAndAgencyIdOrderByProficiencyLevelLevelOrderAscDisplayOrderAsc(
                        item.getId(), item.getAgencyId()).stream().map(this::indicatorResponse).toList();
        return new AdminCompetencyResponse(item.getId(), item.getCode(), item.getName(), item.getDefinition(),
                item.getStatus(), item.getDefinitionVersion(), item.getSupersedesId(), item.getCategory().getId(),
                item.getCategory().getName(), item.getProficiencyScale().getId(), item.getProficiencyScale().getName(),
                item.getDisplayOrder(), item.getEffectiveFrom(), item.getEffectiveTo(), item.getVersion(), children);
    }

    private AdminIndicatorResponse indicatorResponse(BehavioralIndicator item) {
        return new AdminIndicatorResponse(item.getId(), item.getProficiencyLevel().getId(),
                item.getProficiencyLevel().getCode(), item.getBehaviorDescription(), item.getEvidenceGuidance(),
                item.isActive(), item.getDisplayOrder(), item.getEffectiveFrom(), item.getEffectiveTo(), item.getVersion());
    }

    private static void requireVersion(long actual, Long supplied) {
        if (supplied == null) throw new IllegalArgumentException("recordVersion is required");
        if (actual != supplied) throw new OptimisticConflictException(
                "Expected recordVersion " + supplied + " but current version is " + actual);
    }

    private static void rejectVersionOnCreate(Long supplied) {
        if (supplied != null) throw new IllegalArgumentException("recordVersion must not be supplied when creating");
    }

    private static void ensureNoDraft(boolean exists) {
        if (exists) throw new IllegalArgumentException("A draft already exists for this code");
    }

    private static void validatePage(int page, int size) {
        if (page < 0) throw new IllegalArgumentException("page cannot be negative");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");
    }
}
