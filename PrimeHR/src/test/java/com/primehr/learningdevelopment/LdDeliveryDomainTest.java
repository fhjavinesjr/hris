package com.primehr.learningdevelopment;

import com.primehr.learningdevelopment.delivery.domain.*;
import com.primehr.learningdevelopment.domain.LdInterventionVersion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;

import static org.assertj.core.api.Assertions.*;

class LdDeliveryDomainTest {
    @Test
    void publishedPlanIsImmutableAndRevisionPreservesItsSeries() {
        LdConsolidatedPlan plan = plan();
        plan.submit("planner", Instant.parse("2027-01-02T00:00:00Z"));
        plan.approve("approver", Instant.parse("2027-01-03T00:00:00Z"));
        assertThatThrownBy(plan::successor).isInstanceOf(IllegalStateException.class);
        plan.publish("publisher", Instant.parse("2027-01-04T00:00:00Z"));

        assertThat(plan.getStatus()).isEqualTo(LdConsolidatedPlan.Status.PUBLISHED);
        assertThat(plan.getPublishedBy()).isEqualTo("publisher");
        assertThatThrownBy(() -> plan.update("Changed", "Changed", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 12, 31), BigDecimal.ZERO, "GAA"))
                .isInstanceOf(IllegalStateException.class);
        LdConsolidatedPlan successor = plan.successor();
        assertThat(successor.getSeriesId()).isEqualTo(plan.getSeriesId());
        assertThat(successor.getPlanVersion()).isEqualTo(2);
        assertThat(successor.getStatus()).isEqualTo(LdConsolidatedPlan.Status.DRAFT);
    }

    @Test
    void approvedProgramCreatesForwardDraftWithoutChangingItsSourceSnapshots() {
        LdProgram program = program();
        program.submit("planner", Instant.parse("2027-01-02T00:00:00Z"));
        program.approve("approver", Instant.parse("2027-01-03T00:00:00Z"));

        LdProgram successor = program.successor();
        assertThat(successor.getSeriesId()).isEqualTo(program.getSeriesId());
        assertThat(successor.getProgramVersion()).isEqualTo(2);
        assertThat(successor.getPlanFingerprint()).isEqualTo("plan-fingerprint");
        assertThat(successor.getInterventionFingerprint()).isEqualTo("intervention-fingerprint");
        assertThat(successor.getStatus()).isEqualTo(LdProgram.Status.DRAFT);
    }

    @Test
    void offeringEnforcesRegistrationSessionAndPublicationLifecycle() {
        assertThatThrownBy(() -> new LdOfferingSession("AGENCY", "OFF-1", "Session",
                OffsetDateTime.parse("2027-05-02T12:00:00+08:00"),
                OffsetDateTime.parse("2027-05-02T08:00:00+08:00"), "Venue", BigDecimal.ONE, 1))
                .isInstanceOf(IllegalArgumentException.class);

        LdOffering offering = offering();
        offering.submit("coordinator", Instant.parse("2027-01-02T00:00:00Z"));
        assertThatThrownBy(() -> offering.publish("publisher", Instant.now()))
                .isInstanceOf(IllegalStateException.class);
        offering.approve("approver", Instant.parse("2027-01-03T00:00:00Z"));
        offering.publish("publisher", Instant.parse("2027-01-04T00:00:00Z"));

        assertThat(offering.getStatus()).isEqualTo(LdOffering.Status.PUBLISHED);
        LdOffering successor = offering.successor();
        assertThat(successor.getOfferingVersion()).isEqualTo(2);
        assertThat(successor.getSeriesId()).isEqualTo(offering.getSeriesId());
        assertThat(successor.getProviderFingerprint()).isEqualTo("provider-fingerprint");
        offering.cancel();
        assertThat(offering.getStatus()).isEqualTo(LdOffering.Status.CANCELLED);
        assertThatThrownBy(offering::cancel).isInstanceOf(IllegalStateException.class);
    }

    private LdConsolidatedPlan plan() {
        return new LdConsolidatedPlan("AGENCY", LdConsolidatedPlan.ScopeType.OFFICE,
                "BUSINESS_UNIT", 10L, "BU-10", "Office", 1L, "Area", "organization-fingerprint",
                "POLICY-1", "policy-fingerprint", 2027, "Office L&D Plan", "Develop priority capabilities",
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31), new BigDecimal("100000"), "GAA",
                20L, "EMP-20", "Accountable Officer", 30L, "accountable-fingerprint");
    }

    private LdProgram program() {
        return new LdProgram("AGENCY", "PLAN-1", 1, "plan-fingerprint", "PROGRAM-1", "Program",
                "INTERVENTION-1", "intervention-fingerprint", "EVALUATION-1", "evaluation-fingerprint",
                "Apply capability", "Blended learning", LocalDate.of(2027, 5, 1), LocalDate.of(2027, 5, 31),
                new BigDecimal("16"), new BigDecimal("50000"), "GAA", 20L, "EMP-20", "Accountable Officer",
                30L, "accountable-fingerprint");
    }

    private LdOffering offering() {
        return new LdOffering("AGENCY", "PROGRAM-1", 1, "program-fingerprint", "OFFERING-1", "Offering",
                LdInterventionVersion.Modality.BLENDED, "Training Room and LMS", 25, "PROVIDER-1", "Provider",
                "provider-fingerprint", "EVALUATION-1", "evaluation-fingerprint", new BigDecimal("50000"),
                "GAA", "PHP", Instant.parse("2027-03-01T00:00:00Z"), Instant.parse("2027-04-01T00:00:00Z"));
    }
}
