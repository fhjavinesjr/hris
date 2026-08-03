package com.primehr.competency.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompetencyDomainConstraintTest {

    @Test
    void supportsAgencyDefinedScaleWithAnyPositiveNumberOfOrderedLevels() {
        ProficiencyScale scale = scale("AGENCY-1", "CUSTOM");
        scale.addLevel(level("AGENCY-1", "ONE", 1));
        scale.addLevel(level("AGENCY-1", "TWO", 2));
        scale.addLevel(level("AGENCY-1", "THREE", 3));
        scale.addLevel(level("AGENCY-1", "FOUR", 4));
        scale.addLevel(level("AGENCY-1", "FIVE", 5));

        assertThat(scale.getLevels()).hasSize(5);
    }

    @Test
    void rejectsInvalidEffectivityAndNegativeDisplayOrder() {
        assertThatThrownBy(() -> new CompetencyCategory("A", "CORE", "Core", null, true, 0,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("effectiveTo");
        assertThatThrownBy(() -> new CompetencyCategory("A", "CORE", "Core", null, true, -1,
                null, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void normalizesCodesAndRejectsCrossAgencyRelationships() {
        CompetencyCategory category = new CompetencyCategory("A", " core ", "Core", null,
                true, 0, null, null);
        assertThat(category.getCode()).isEqualTo("CORE");

        assertThatThrownBy(() -> new Competency("A", "C1", "Name", "Definition", "ACTIVE",
                category, scale("B", "SCALE"), true, 0, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same agency");
    }

    @Test
    void indicatorLevelMustBelongToCompetencyScale() {
        CompetencyCategory category = new CompetencyCategory("A", "CORE", "Core", null,
                true, 0, null, null);
        ProficiencyScale expectedScale = scale("A", "EXPECTED");
        ProficiencyScale otherScale = scale("A", "OTHER");
        ProficiencyLevel otherLevel = level("A", "L1", 1);
        otherScale.addLevel(otherLevel);
        Competency competency = new Competency("A", "C1", "Name", "Definition", "ACTIVE",
                category, expectedScale, true, 0, null, null);

        assertThatThrownBy(() -> new BehavioralIndicator("A", competency, otherLevel,
                "Observable behavior", null, true, 1, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("competency scale");
    }

    private static ProficiencyScale scale(String agency, String code) {
        return new ProficiencyScale(agency, code, code, null, true, 0, null, null);
    }

    private static ProficiencyLevel level(String agency, String code, int order) {
        return new ProficiencyLevel(agency, code, code, order, null, true, null, null);
    }
}
