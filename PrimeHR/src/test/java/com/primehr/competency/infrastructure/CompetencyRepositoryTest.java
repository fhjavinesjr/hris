package com.primehr.competency.infrastructure;

import com.primehr.competency.domain.BehavioralIndicator;
import com.primehr.competency.domain.Competency;
import com.primehr.competency.domain.CompetencyCategory;
import com.primehr.competency.domain.ProficiencyLevel;
import com.primehr.competency.domain.ProficiencyScale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import(CompetencyRepositoryTest.JpaAuditTestConfiguration.class)
class CompetencyRepositoryTest {

    @TestConfiguration
    @EnableJpaAuditing
    static class JpaAuditTestConfiguration {
        @Bean
        AuditorAware<String> auditorAware() {
            return () -> Optional.of("repository-test");
        }
    }

    private final CompetencyCategoryRepository categoryRepository;
    private final ProficiencyScaleRepository scaleRepository;
    private final CompetencyRepository competencyRepository;
    private final BehavioralIndicatorRepository indicatorRepository;

    @Autowired
    CompetencyRepositoryTest(CompetencyCategoryRepository categoryRepository,
                             ProficiencyScaleRepository scaleRepository,
                             CompetencyRepository competencyRepository,
                             BehavioralIndicatorRepository indicatorRepository) {
        this.categoryRepository = categoryRepository;
        this.scaleRepository = scaleRepository;
        this.competencyRepository = competencyRepository;
        this.indicatorRepository = indicatorRepository;
    }

    @Test
    void filtersByAgencyCategorySearchAndCurrentEffectivity() {
        Fixture active = fixture("A", "CORE", "COMM", "Communication", true,
                LocalDate.of(2026, 1, 1), null);
        fixture("A", "CORE", "OLD", "Old Communication", true,
                LocalDate.of(2020, 1, 1), LocalDate.of(2025, 12, 31));
        fixture("B", "CORE", "COMM", "Communication", true, null, null);

        var result = competencyRepository.findAll(CompetencySpecifications.competencyFilter(
                "A", active.category().getId(), true, "commun", LocalDate.of(2026, 8, 3)),
                PageRequest.of(0, 20, Sort.by("displayOrder")));

        assertThat(result.getContent()).extracting(Competency::getCode).containsExactly("COMM");
        assertThat(active.competency().getCreatedBy()).isEqualTo("repository-test");
        assertThat(active.competency().getCreatedAt()).isNotNull();
        assertThat(active.competency().getUpdatedAt()).isNotNull();
    }

    @Test
    void databaseRejectsDuplicateCodeWithinAgencyButAllowsSameCodeForAnotherAgency() {
        fixture("A", "CORE", "COMM", "Communication", true, null, null);
        fixture("B", "CORE", "COMM", "Communication", true, null, null);

        assertThatThrownBy(() -> fixture("A", "OTHER", "comm", "Duplicate", true, null, null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void returnsBehavioralIndicatorsByLevelThenDisplayOrder() {
        Fixture fixture = fixture("A", "CORE", "COMM", "Communication", true, null, null);
        ProficiencyLevel first = fixture.scale().getLevels().get(0);
        ProficiencyLevel second = fixture.scale().getLevels().get(1);
        indicatorRepository.save(new BehavioralIndicator("A", fixture.competency(), second,
                "Second level", null, true, 1, null, null));
        indicatorRepository.save(new BehavioralIndicator("A", fixture.competency(), first,
                "First level second", null, true, 2, null, null));
        indicatorRepository.save(new BehavioralIndicator("A", fixture.competency(), first,
                "First level first", null, true, 1, null, null));
        indicatorRepository.flush();

        List<BehavioralIndicator> indicators = indicatorRepository
                .findByCompetencyIdAndAgencyIdOrderByProficiencyLevelLevelOrderAscDisplayOrderAsc(
                        fixture.competency().getId(), "A");

        assertThat(indicators).extracting(BehavioralIndicator::getBehaviorDescription)
                .containsExactly("First level first", "First level second", "Second level");
    }

    private Fixture fixture(String agency, String categoryCode, String competencyCode, String name,
                            boolean active, LocalDate from, LocalDate to) {
        CompetencyCategory category = categoryRepository.saveAndFlush(new CompetencyCategory(
                agency, categoryCode + competencyCode, categoryCode, null, true, 1, null, null));
        ProficiencyScale scale = new ProficiencyScale(agency, "S" + categoryCode + competencyCode, "Scale", null,
                true, 1, null, null);
        scale.addLevel(new ProficiencyLevel(agency, "L1", "Level 1", 1, null, true, null, null));
        scale.addLevel(new ProficiencyLevel(agency, "L2", "Level 2", 2, null, true, null, null));
        scale = scaleRepository.saveAndFlush(scale);
        Competency competency = competencyRepository.saveAndFlush(new Competency(agency, competencyCode, name,
                "Definition for " + name, "ACTIVE", category, scale, active, 1, from, to));
        return new Fixture(category, scale, competency);
    }

    private record Fixture(CompetencyCategory category, ProficiencyScale scale, Competency competency) {
    }
}
