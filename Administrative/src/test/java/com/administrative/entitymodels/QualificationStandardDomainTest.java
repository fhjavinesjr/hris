package com.administrative.entitymodels;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QualificationStandardDomainTest {

    @Test
    void draftCanBePublishedAndBecomesImmutable() {
        QualificationStandard standard = draft(LocalDate.of(2026, 1, 1), null);

        standard.publish("publisher", Instant.parse("2026-01-01T00:00:00Z"));

        assertThat(standard.getStatus()).isEqualTo(QualificationStandardStatus.ACTIVE);
        assertThat(standard.getPublishedBy()).isEqualTo("publisher");
        assertThatThrownBy(() -> standard.update(
                "Changed", "Training", "Experience", "Eligibility", null, null,
                LocalDate.of(2026, 1, 1), null, "editor"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    void invalidEffectivityIsRejectedBeforePersistence() {
        assertThatThrownBy(() -> draft(
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 31)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("effectiveTo");
    }

    @Test
    void activeVersionCanBeClosedForAControlledSuccessor() {
        QualificationStandard standard = draft(LocalDate.of(2026, 1, 1), null);
        standard.publish("publisher", Instant.parse("2026-01-01T00:00:00Z"));

        standard.close(LocalDate.of(2026, 6, 30), "publisher");

        assertThat(standard.getEffectiveTo()).isEqualTo(LocalDate.of(2026, 6, 30));
        assertThat(standard.getStatus()).isEqualTo(QualificationStandardStatus.ACTIVE);
    }

    @Test
    void requiredQualificationTextCannotBeBlank() {
        assertThatThrownBy(() -> QualificationStandard.draft(
                10L, 1, null, " ", "Training", "Experience", "Eligibility",
                null, null, LocalDate.of(2026, 1, 1), null, "creator"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("education");
    }

    private static QualificationStandard draft(LocalDate effectiveFrom, LocalDate effectiveTo) {
        return QualificationStandard.draft(
                10L, 1, null, "Bachelor's degree", "Eight hours",
                "One year", "Career service eligibility", null, "Agency policy",
                effectiveFrom, effectiveTo, "creator");
    }
}
