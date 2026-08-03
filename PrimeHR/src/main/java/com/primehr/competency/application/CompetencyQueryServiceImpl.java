package com.primehr.competency.application;

import com.primehr.competency.api.BehavioralIndicatorResponse;
import com.primehr.competency.api.CompetencyCategoryResponse;
import com.primehr.competency.api.CompetencyDetailResponse;
import com.primehr.competency.api.CompetencySummaryResponse;
import com.primehr.competency.api.ProficiencyLevelResponse;
import com.primehr.competency.api.ProficiencyScaleResponse;
import com.primehr.competency.domain.BehavioralIndicator;
import com.primehr.competency.domain.Competency;
import com.primehr.competency.domain.CompetencyCategory;
import com.primehr.competency.domain.ProficiencyLevel;
import com.primehr.competency.domain.ProficiencyScale;
import com.primehr.competency.infrastructure.BehavioralIndicatorRepository;
import com.primehr.competency.infrastructure.CompetencyCategoryRepository;
import com.primehr.competency.infrastructure.CompetencyRepository;
import com.primehr.competency.infrastructure.CompetencySpecifications;
import com.primehr.competency.infrastructure.ProficiencyScaleRepository;
import com.primehr.shared.api.PageResponse;
import com.primehr.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CompetencyQueryServiceImpl implements CompetencyQueryService {

    private static final Sort DISPLAY_ORDER = Sort.by("displayOrder").ascending().and(Sort.by("code").ascending());

    private final CompetencyCategoryRepository categoryRepository;
    private final CompetencyRepository competencyRepository;
    private final ProficiencyScaleRepository scaleRepository;
    private final BehavioralIndicatorRepository indicatorRepository;

    public CompetencyQueryServiceImpl(CompetencyCategoryRepository categoryRepository,
                                      CompetencyRepository competencyRepository,
                                      ProficiencyScaleRepository scaleRepository,
                                      BehavioralIndicatorRepository indicatorRepository) {
        this.categoryRepository = categoryRepository;
        this.competencyRepository = competencyRepository;
        this.scaleRepository = scaleRepository;
        this.indicatorRepository = indicatorRepository;
    }

    @Override
    public List<CompetencyCategoryResponse> listCategories(String agencyId, Boolean active, LocalDate asOf) {
        String agency = requireAgency(agencyId);
        LocalDate effectiveDate = effectiveDate(asOf);
        return categoryRepository.findAll(CompetencySpecifications.scoped(agency, active, effectiveDate), DISPLAY_ORDER)
                .stream().map(category -> toCategory(category, effectiveDate)).toList();
    }

    @Override
    public PageResponse<CompetencySummaryResponse> listCompetencies(String agencyId, String categoryId,
                                                                    Boolean active, String search, LocalDate asOf,
                                                                    int page, int size) {
        String agency = requireAgency(agencyId);
        if (page < 0) {
            throw new IllegalArgumentException("page cannot be negative");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        LocalDate effectiveDate = effectiveDate(asOf);
        var result = competencyRepository.findAll(
                CompetencySpecifications.competencyFilter(agency, categoryId, active, search, effectiveDate),
                PageRequest.of(page, size, DISPLAY_ORDER));
        return PageResponse.from(result, competency -> toSummary(competency, effectiveDate));
    }

    @Override
    public CompetencyDetailResponse getCompetency(String agencyId, String competencyId,
                                                   boolean includeInactive, LocalDate asOf) {
        String agency = requireAgency(agencyId);
        LocalDate effectiveDate = effectiveDate(asOf);
        Competency competency = competencyRepository.findByIdAndAgencyId(competencyId, agency)
                .orElseThrow(() -> new ResourceNotFoundException("Competency was not found"));
        if (!includeInactive && !competency.isEffectiveOn(effectiveDate)) {
            throw new ResourceNotFoundException("Competency was not found");
        }
        List<BehavioralIndicatorResponse> indicators = indicatorRepository
                .findByCompetencyIdAndAgencyIdOrderByProficiencyLevelLevelOrderAscDisplayOrderAsc(
                        competency.getId(), agency)
                .stream()
                .filter(indicator -> includeInactive || indicator.isEffectiveOn(effectiveDate))
                .map(indicator -> toIndicator(indicator, effectiveDate))
                .toList();
        return new CompetencyDetailResponse(toSummary(competency, effectiveDate),
                toScale(competency.getProficiencyScale(), effectiveDate, includeInactive), indicators);
    }

    @Override
    public List<ProficiencyScaleResponse> listScales(String agencyId, Boolean active, LocalDate asOf) {
        String agency = requireAgency(agencyId);
        LocalDate effectiveDate = effectiveDate(asOf);
        return scaleRepository.findAll(CompetencySpecifications.scoped(agency, active, effectiveDate), DISPLAY_ORDER)
                .stream().map(scale -> toScale(scale, effectiveDate, active == null || !active)).toList();
    }

    private static CompetencyCategoryResponse toCategory(CompetencyCategory category, LocalDate asOf) {
        return new CompetencyCategoryResponse(category.getId(), category.getAgencyId(), category.getCode(),
                category.getName(), category.getDescription(), category.isActive(), category.isEffectiveOn(asOf),
                category.getDisplayOrder(), category.getEffectiveFrom(), category.getEffectiveTo(),
                category.getVersion());
    }

    private static CompetencySummaryResponse toSummary(Competency competency, LocalDate asOf) {
        return new CompetencySummaryResponse(competency.getId(), competency.getAgencyId(), competency.getCode(),
                competency.getName(), competency.getDefinition(), competency.getStatus(),
                competency.getCategory().getId(), competency.getCategory().getCode(), competency.getCategory().getName(),
                competency.getProficiencyScale().getId(), competency.getProficiencyScale().getCode(),
                competency.getProficiencyScale().getName(), competency.isActive(), competency.isEffectiveOn(asOf),
                competency.getDisplayOrder(), competency.getEffectiveFrom(), competency.getEffectiveTo(),
                competency.getVersion());
    }

    private static ProficiencyScaleResponse toScale(ProficiencyScale scale, LocalDate asOf,
                                                    boolean includeInactiveLevels) {
        List<ProficiencyLevelResponse> levels = scale.getLevels().stream()
                .filter(level -> includeInactiveLevels || level.isEffectiveOn(asOf))
                .map(level -> toLevel(level, asOf)).toList();
        return new ProficiencyScaleResponse(scale.getId(), scale.getAgencyId(), scale.getCode(), scale.getName(),
                scale.getDescription(), scale.isActive(), scale.isEffectiveOn(asOf), scale.getDisplayOrder(),
                scale.getEffectiveFrom(), scale.getEffectiveTo(), scale.getVersion(), levels);
    }

    private static ProficiencyLevelResponse toLevel(ProficiencyLevel level, LocalDate asOf) {
        return new ProficiencyLevelResponse(level.getId(), level.getCode(), level.getLabel(), level.getLevelOrder(),
                level.getDescription(), level.isActive(), level.isEffectiveOn(asOf), level.getEffectiveFrom(),
                level.getEffectiveTo(), level.getVersion());
    }

    private static BehavioralIndicatorResponse toIndicator(BehavioralIndicator indicator, LocalDate asOf) {
        ProficiencyLevel level = indicator.getProficiencyLevel();
        return new BehavioralIndicatorResponse(indicator.getId(), level.getId(), level.getCode(), level.getLabel(),
                level.getLevelOrder(), indicator.getBehaviorDescription(), indicator.getEvidenceGuidance(),
                indicator.isActive(), indicator.isEffectiveOn(asOf), indicator.getDisplayOrder(),
                indicator.getEffectiveFrom(), indicator.getEffectiveTo(), indicator.getVersion());
    }

    private static String requireAgency(String agencyId) {
        if (agencyId == null || agencyId.isBlank()) {
            throw new IllegalArgumentException("agencyId is required");
        }
        return agencyId.trim();
    }

    private static LocalDate effectiveDate(LocalDate asOf) {
        return asOf == null ? LocalDate.now() : asOf;
    }
}
