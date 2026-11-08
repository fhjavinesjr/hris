package com.primehr.learningdevelopment;

import com.primehr.learningdevelopment.plan.domain.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class LdPlanDomainTest {
    @Test
    void learningNeedCanOnlyBeChangedWhileOpen() {
        LdLearningNeed need = need();
        assertThat(need.getStatus()).isEqualTo(LdLearningNeed.Status.OPEN);
        need.link();
        assertThatThrownBy(() -> need.update("Changed", "Current", "Target", "Measure",
                LocalDate.of(2027, 1, 1), "HIGH")).isInstanceOf(IllegalStateException.class);
        need.reopen();
        need.cancel();
        assertThatThrownBy(need::link).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void approvedPlanIsImmutableAndCreatesAForwardSuccessor() {
        LdIndividualDevelopmentPlan plan = plan();
        plan.submit("EMP-1", Instant.parse("2026-01-02T00:00:00Z"));
        plan.recommend();
        plan.recommend();
        plan.approve("APR-1", Instant.parse("2026-01-03T00:00:00Z"));
        assertThat(plan.getStatus()).isEqualTo(LdIndividualDevelopmentPlan.Status.APPROVED);
        assertThatThrownBy(() -> plan.updateGoal("Changed")).isInstanceOf(IllegalStateException.class);
        LdIndividualDevelopmentPlan successor = plan.successor("Successor goal");
        assertThat(successor.getPlanVersion()).isEqualTo(2);
        assertThat(successor.getSeriesId()).isEqualTo(plan.getSeriesId());
        assertThat(successor.getStatus()).isEqualTo(LdIndividualDevelopmentPlan.Status.DRAFT);
    }

    @Test
    void referralDispositionAndRouteStepsEnforceTheirLifecycle() {
        assertThatThrownBy(() -> new LdReferralDisposition("AGENCY", "REF-1", 0, "fingerprint",
                "EMP-1", LdReferralDisposition.Decision.ACCEPTED, null, null, "HR-1", Instant.now(), "Accepted"))
                .isInstanceOf(IllegalArgumentException.class);
        LdReferralDisposition disposition = new LdReferralDisposition("AGENCY", "REF-1", 0,
                "fingerprint", "EMP-1", LdReferralDisposition.Decision.RETURNED, null, null,
                "HR-1", Instant.now(), "Clarify the target outcome");
        assertThat(disposition.getDecision()).isEqualTo(LdReferralDisposition.Decision.RETURNED);
        assertThatThrownBy(() -> disposition.linkIdpItem("ITEM-1"))
                .isInstanceOf(IllegalStateException.class);

        LdReferralDisposition accepted = new LdReferralDisposition("AGENCY", "REF-1", 1,
                "fingerprint", "EMP-1", LdReferralDisposition.Decision.ACCEPTED, "NEED-1", null,
                "HR-1", Instant.now(), "Accepted");
        accepted.linkIdpItem("ITEM-1");
        assertThat(accepted.getLinkedIdpItemId()).isEqualTo("ITEM-1");
        assertThatThrownBy(() -> accepted.linkIdpItem("ITEM-2"))
                .isInstanceOf(IllegalStateException.class);
        accepted.unlinkIdpItem("ITEM-1");
        assertThat(accepted.getLinkedIdpItemId()).isNull();

        LdIdpRouteStep step = new LdIdpRouteStep("AGENCY", "ROUTE-1", 1,
                LdIdpRouteStep.Action.RECOMMEND, 2L, "SUP-1", "Supervisor", 22L,
                "participant", "organization");
        step.decide(LdIdpRouteStep.Status.RECOMMENDED, "SUP-1", "Recommended", Instant.now());
        assertThatThrownBy(() -> step.decide(LdIdpRouteStep.Status.RECOMMENDED, "SUP-1", null, Instant.now()))
                .isInstanceOf(IllegalStateException.class);
    }

    private LdLearningNeed need() {
        return new LdLearningNeed("AGENCY", 1L, "EMP-1", "Employee", 11L, "employee-fingerprint",
                LearningNeedSourceType.OTHER, null, null, null, null, null, null,
                "Develop supervision skills", "Requires guidance", "Independent application",
                "Successful supervised assignment", LocalDate.of(2027, 1, 1), "HIGH");
    }

    private LdIndividualDevelopmentPlan plan() {
        return new LdIndividualDevelopmentPlan("AGENCY", 1L, "EMP-1", "Employee", 11L, 21L, 31L,
                "employee-fingerprint", 41L, "Business Unit", 51L, "Area", "organization-fingerprint",
                2027, "Build role-critical capability");
    }
}
