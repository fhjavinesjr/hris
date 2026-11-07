package com.primehr.evidence;

import com.primehr.evidence.domain.EvidenceAssessmentCase;
import com.primehr.evidence.domain.EvidenceAssessmentCycle;
import com.primehr.evidence.domain.EvidenceCorrectiveAction;
import com.primehr.evidence.domain.EvidenceFinding;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvidenceAssessmentDomainTest {

    private static final EvidenceAssessmentCase.Person OWNER =
            new EvidenceAssessmentCase.Person(1L, "OWNER-1", "Evidence Owner", 11L, "owner-fingerprint");
    private static final EvidenceAssessmentCase.Person VALIDATOR =
            new EvidenceAssessmentCase.Person(2L, "VALIDATOR-1", "Independent Validator", 22L, "validator-fingerprint");

    @Test
    void cycleFreezesPublishedFrameworkSnapshotAndEnforcesLifecycle() {
        EvidenceAssessmentCycle cycle = new EvidenceAssessmentCycle("AGENCY", "2026", "2026 Assessment", null,
                "framework-version", 3, 7, "framework-fingerprint",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), LocalDate.of(2027, 1, 31));

        cycle.open("ADMIN", "Assessment window approved", Instant.parse("2026-01-01T00:00:00Z"));

        assertThat(cycle.getStatus()).isEqualTo(EvidenceAssessmentCycle.Status.OPEN);
        assertThat(cycle.getFrameworkDefinitionVersion()).isEqualTo(3);
        assertThat(cycle.getFrameworkContentRevision()).isEqualTo(7);
        assertThat(cycle.getFrameworkFingerprint()).isEqualTo("framework-fingerprint");
        assertThatThrownBy(() -> cycle.update("Changed", null, cycle.getPeriodStart(), cycle.getPeriodEnd(), cycle.getDueDate()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ownerAndValidatorMustBeDifferentAndOnlyAssignedActorsMayTransition() {
        assertThatThrownBy(() -> assessmentCase(OWNER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("different employees");

        EvidenceAssessmentCase assessment = assessmentCase(VALIDATOR);
        assertThatThrownBy(() -> assessment.submit("OTHER", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
        assessment.submit("OWNER-1", Instant.parse("2026-02-01T00:00:00Z"));
        assertThat(assessment.getStatus()).isEqualTo(EvidenceAssessmentCase.Status.SUBMITTED);
        assertThat(assessment.getSubmissionRevision()).isOne();
        assertThatThrownBy(() -> assessment.finalizeCase("OWNER-1", "Self approval", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
        assessment.returnForRework("VALIDATOR-1", "Evidence date needs correction", Instant.now());
        assertThat(assessment.getStatus()).isEqualTo(EvidenceAssessmentCase.Status.RETURNED);
    }

    @Test
    void notApplicableFindingRequiresHumanAuthorityAndFindingHistoryIsSupersedable() {
        assertThatThrownBy(() -> new EvidenceFinding("AGENCY", "case", "requirement", "REQ-1", 1, 1,
                null, EvidenceFinding.Outcome.NOT_APPLICABLE, "Not in agency mandate", null,
                "VALIDATOR-1", Instant.now())).isInstanceOf(IllegalArgumentException.class);

        EvidenceFinding finding = new EvidenceFinding("AGENCY", "case", "requirement", "REQ-1", 1, 1,
                null, EvidenceFinding.Outcome.NOT_APPLICABLE, "Not in agency mandate", "CSC rule reference",
                "VALIDATOR-1", Instant.now());
        finding.supersede();
        assertThat(finding.isCurrentRecord()).isFalse();
    }

    @Test
    void correctiveActionRequiresResponsibleEmployeeBeforeIndependentVerification() {
        EvidenceCorrectiveAction action = new EvidenceCorrectiveAction("AGENCY", "gap", "case",
                "Complete missing records", "OWNER-1", "Evidence Owner", LocalDate.of(2026, 6, 30));
        assertThatThrownBy(() -> action.start("OTHER", "Started"))
                .isInstanceOf(IllegalArgumentException.class);
        action.start("OWNER-1", "Records requested");
        action.submit("OWNER-1", "Records compiled", "evidence-reference", Instant.now());
        action.verify("VALIDATOR-1", "Evidence independently verified", Instant.now());
        assertThat(action.getStatus()).isEqualTo(EvidenceCorrectiveAction.Status.VERIFIED);
        assertThat(action.getVerifiedBy()).isEqualTo("VALIDATOR-1");
    }

    private EvidenceAssessmentCase assessmentCase(EvidenceAssessmentCase.Person validator) {
        return new EvidenceAssessmentCase("AGENCY", "cycle", "framework-version", "CASE-1", "Case",
                "BUSINESS_UNIT", 10L, "Human Resource", "organization-fingerprint", OWNER, validator);
    }
}
