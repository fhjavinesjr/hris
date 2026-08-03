package com.primehr.competency.application;

import com.primehr.competency.domain.BehavioralIndicator;
import com.primehr.competency.domain.Competency;
import com.primehr.competency.domain.CompetencyCategory;
import com.primehr.competency.domain.ProficiencyLevel;
import com.primehr.competency.domain.ProficiencyScale;
import com.primehr.competency.infrastructure.BehavioralIndicatorRepository;
import com.primehr.competency.infrastructure.CompetencyCategoryRepository;
import com.primehr.competency.infrastructure.CompetencyRepository;
import com.primehr.competency.infrastructure.ProficiencyScaleRepository;
import com.primehr.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompetencyQueryServiceImplTest {

    private CompetencyRepository competencyRepository;
    private BehavioralIndicatorRepository indicatorRepository;
    private CompetencyQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        competencyRepository = mock(CompetencyRepository.class);
        indicatorRepository = mock(BehavioralIndicatorRepository.class);
        service = new CompetencyQueryServiceImpl(mock(CompetencyCategoryRepository.class), competencyRepository,
                mock(ProficiencyScaleRepository.class), indicatorRepository);
    }

    @Test
    void hidesInactiveCompetencyUnlessExplicitlyRequested() {
        Competency competency = competency(false, null, null);
        when(competencyRepository.findByIdAndAgencyId("id", "A")).thenReturn(Optional.of(competency));

        assertThatThrownBy(() -> service.getCompetency("A", "id", false, LocalDate.of(2026, 8, 3)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void filtersExpiredIndicatorsAndPreservesRepositoryOrdering() {
        Competency competency = competency(true, null, null);
        ProficiencyLevel level = competency.getProficiencyScale().getLevels().get(0);
        BehavioralIndicator current = new BehavioralIndicator("A", competency, level, "Current", null,
                true, 1, null, null);
        BehavioralIndicator expired = new BehavioralIndicator("A", competency, level, "Expired", null,
                true, 2, LocalDate.of(2020, 1, 1), LocalDate.of(2025, 12, 31));
        when(competencyRepository.findByIdAndAgencyId("id", "A")).thenReturn(Optional.of(competency));
        when(indicatorRepository.findByCompetencyIdAndAgencyIdOrderByProficiencyLevelLevelOrderAscDisplayOrderAsc(
                competency.getId(), "A")).thenReturn(List.of(current, expired));

        var result = service.getCompetency("A", "id", false, LocalDate.of(2026, 8, 3));

        assertThat(result.behavioralIndicators()).extracting(i -> i.behaviorDescription())
                .containsExactly("Current");
    }

    @Test
    void validatesPaginationBounds() {
        assertThatThrownBy(() -> service.listCompetencies("A", null, null, null, null, 0, 101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 100");
    }

    private static Competency competency(boolean active, LocalDate from, LocalDate to) {
        CompetencyCategory category = new CompetencyCategory("A", "CORE", "Core", null,
                true, 1, null, null);
        ProficiencyScale scale = new ProficiencyScale("A", "SCALE", "Scale", null,
                true, 1, null, null);
        scale.addLevel(new ProficiencyLevel("A", "L1", "Level 1", 1, null, true, null, null));
        return new Competency("A", "COMM", "Communication", "Definition", "ACTIVE", category, scale,
                active, 1, from, to);
    }
}
