package com.primehr.rsp.screening;

import com.primehr.rsp.screening.api.ScreeningPolicyDtos.*;
import com.primehr.rsp.screening.application.ScreeningPolicyService;
import com.primehr.rsp.screening.domain.*;
import com.primehr.rsp.screening.infrastructure.ScreeningPolicyRepository;
import com.primehr.shared.exception.OptimisticConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ScreeningPolicyServiceIntegrationTest {
    @Autowired ScreeningPolicyService service;
    @Autowired ScreeningPolicyRepository policies;

    @Test
    void draftPublishSuccessorLifecycleIsVersionedOrderedAndNonOverlapping() {
        PolicyResponse draft = service.create("AGENCY", request("POL-1", null), "create");
        assertThat(draft.status()).isEqualTo("DRAFT");
        assertThat(draft.criteria()).extracting(CriterionResponse::displayOrder).containsExactly(0, 1);

        PolicyResponse published = service.publish("AGENCY", draft.id(),
                new Publish(draft.recordVersion(), LocalDate.of(2026, 1, 1), null), "publish");
        assertThat(published.status()).isEqualTo("PUBLISHED");
        assertThatThrownBy(() -> service.update("AGENCY", draft.id(), request("POL-1", published.recordVersion()), "x"))
                .hasMessageContaining("immutable");

        PolicyResponse successor = service.successor("AGENCY", draft.id(),
                new Transition(published.recordVersion()), "successor");
        assertThat(successor.definitionVersion()).isEqualTo(2);
        PolicyResponse successorPublished = service.publish("AGENCY", successor.id(),
                new Publish(successor.recordVersion(), LocalDate.of(2027, 1, 1), null), "publish-2");

        assertThat(successorPublished.status()).isEqualTo("PUBLISHED");
        ScreeningPolicy prior = policies.findByIdAndAgencyId(draft.id(), "AGENCY").orElseThrow();
        assertThat(prior.getStatus()).isEqualTo(ScreeningPolicy.Status.SUPERSEDED);
        assertThat(prior.getEffectiveTo()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void duplicateOrderDuplicateCodeAndStaleVersionFailBeforeMutation() {
        CriterionInput duplicate = criterion("REQ", 0, ScreeningCriterion.EvaluationMode.PRESENCE);
        SavePolicy invalid = new SavePolicy("POL", "Policy", null, null, null,
                List.of(duplicate, duplicate), reasons(), null);
        assertThatThrownBy(() -> service.create("AGENCY", invalid, null)).hasMessageContaining("Duplicate");

        PolicyResponse draft = service.create("AGENCY", request("POL-2", null), null);
        assertThatThrownBy(() -> service.update("AGENCY", draft.id(), request("POL-2", 99L), null))
                .isInstanceOf(OptimisticConflictException.class);
        assertThat(service.get("AGENCY", draft.id()).name()).isEqualTo("Policy POL-2");
    }

    private static SavePolicy request(String code, Long version) {
        return new SavePolicy(code, "Policy " + code, "Generic screening policy", null, null,
                List.of(criterion("PDS", 0, ScreeningCriterion.EvaluationMode.PRESENCE),
                        criterion("EDU", 1, ScreeningCriterion.EvaluationMode.MANUAL_REVIEW)),
                reasons(), version);
    }

    private static CriterionInput criterion(String code, int order, ScreeningCriterion.EvaluationMode mode) {
        boolean manual = mode == ScreeningCriterion.EvaluationMode.MANUAL_REVIEW;
        return new CriterionInput(code, code + " check", "Review exact evidence", "Evidence will be reviewed",
                ScreeningCriterion.Category.EDUCATION, mode, manual ? null : code,
                null, null, true, true, false, true, true, order);
    }

    private static List<ReasonInput> reasons() {
        return List.of(new ReasonInput("NOT_MET", "Requirement not met",
                "A published requirement was not met.", ScreeningReasonCode.Outcome.DISQUALIFIED, true, 0));
    }
}
