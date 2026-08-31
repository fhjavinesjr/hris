package com.primehr.rsp.screening;

import com.primehr.rsp.screening.api.ScreeningPolicyDtos.EvidenceFacts;
import com.primehr.rsp.screening.application.ScreeningEvidenceEvaluator;
import com.primehr.rsp.screening.domain.ScreeningCriterion;
import com.primehr.rsp.screening.domain.ScreeningPolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class ScreeningPolicyDomainTest {
    private final ScreeningEvidenceEvaluator evaluator = new ScreeningEvidenceEvaluator();

    @Test
    void publishedPolicyIsImmutableAndSuccessorClosesPredecessorWithoutOverlap() {
        ScreeningPolicy policy = new ScreeningPolicy("AGENCY", "GENERIC", "Generic", null, 1, null);
        policy.publish(LocalDate.of(2026, 1, 1), null, "publisher", Instant.now());

        assertThatThrownBy(() -> policy.update("CHANGED", "Changed", null, null, null))
                .hasMessageContaining("immutable");

        policy.supersede(LocalDate.of(2027, 1, 1));
        assertThat(policy.getStatus()).isEqualTo(ScreeningPolicy.Status.SUPERSEDED);
        assertThat(policy.getEffectiveTo()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void invalidCriterionCombinationsAreRejected() {
        assertThatThrownBy(() -> criterion(ScreeningCriterion.EvaluationMode.NUMERIC_THRESHOLD,
                null, null, "years", true, true)).hasMessageContaining("threshold");
        assertThatThrownBy(() -> criterion(ScreeningCriterion.EvaluationMode.MANUAL_REVIEW,
                "resume.text", null, null, true, true)).hasMessageContaining("automatic source");
        assertThatThrownBy(() -> criterion(ScreeningCriterion.EvaluationMode.PRESENCE,
                "PDS", null, null, false, true)).hasMessageContaining("mandatory");
    }

    @Test
    void evaluatorUsesOnlyStructuredFactsAndAlwaysRequiresHumanConfirmation() {
        ScreeningCriterion presence = criterion(ScreeningCriterion.EvaluationMode.PRESENCE,
                "PDS", null, null, true, true);
        ScreeningCriterion numeric = criterion(ScreeningCriterion.EvaluationMode.NUMERIC_THRESHOLD,
                "training.hours", new BigDecimal("8"), "HOURS", true, true);
        ScreeningCriterion manual = criterion(ScreeningCriterion.EvaluationMode.MANUAL_REVIEW,
                null, null, null, true, true);
        EvidenceFacts facts = new EvidenceFacts(Set.of("PDS"), Map.of("training.hours", new BigDecimal("10")),
                Map.of(), Set.of());

        assertThat(evaluator.evaluate(presence, facts)).extracting("result", "humanConfirmationRequired")
                .containsExactly("MET", true);
        assertThat(evaluator.evaluate(numeric, facts).result()).isEqualTo("MET");
        assertThat(evaluator.evaluate(manual, facts)).satisfies(result -> {
            assertThat(result.result()).isEqualTo("NEEDS_REVIEW");
            assertThat(result.explanation()).contains("free text is not interpreted");
            assertThat(result.humanConfirmationRequired()).isTrue();
        });
    }

    private static ScreeningCriterion criterion(ScreeningCriterion.EvaluationMode mode, String source,
                                                  BigDecimal threshold, String unit,
                                                  boolean mandatory, boolean disqualifying) {
        return new ScreeningCriterion("AGENCY", "policy", "C1", "Criterion", null, "Public guidance",
                ScreeningCriterion.Category.EDUCATION, mode, source, threshold, unit,
                mandatory, disqualifying, false, true, true, 0);
    }
}
