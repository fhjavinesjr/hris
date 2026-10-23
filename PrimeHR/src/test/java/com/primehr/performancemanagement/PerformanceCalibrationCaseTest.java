package com.primehr.performancemanagement;

import com.primehr.performancemanagement.domain.PerformanceCalibrationCase;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerformanceCalibrationCaseTest {

    @Test
    void quorumUsesStrictMajorityAndEachDecisionChangesTheAggregate() {
        PerformanceCalibrationCase calibration = caseWithVoters(4);

        assertThat(calibration.getQuorumRequired()).isEqualTo(3);
        calibration.proposalSubmitted("assessment-1", 4);
        calibration.decisionRecorded();
        calibration.decisionRecorded();

        assertThat(calibration.getDecisionCount()).isEqualTo(2);
        assertThat(calibration.getStatus()).isEqualTo(PerformanceCalibrationCase.Status.PROPOSAL_SUBMITTED);
    }

    @Test
    void proposalCanSnapshotAnIndependentElectorateThatExcludesItsAuthor() {
        PerformanceCalibrationCase calibration = caseWithVoters(2);

        calibration.proposalSubmitted("assessment-independent", 1);

        assertThat(calibration.getEligibleVoters()).isEqualTo(1);
        assertThat(calibration.getQuorumRequired()).isEqualTo(1);
    }

    @Test
    void approvedAndRejectedOutcomesAreTerminalForFurtherDecisions() {
        PerformanceCalibrationCase approved = caseWithVoters(3);
        approved.proposalSubmitted("assessment-approved", 3);
        approved.decisionRecorded();
        approved.approve();

        assertThatThrownBy(approved::decisionRecorded)
                .hasMessageContaining("not awaiting decision");

        PerformanceCalibrationCase rejected = caseWithVoters(3);
        rejected.proposalSubmitted("assessment-rejected", 3);
        rejected.decisionRecorded();
        rejected.reject();

        assertThat(rejected.getStatus()).isEqualTo(PerformanceCalibrationCase.Status.REJECTED);
        assertThatThrownBy(rejected::finalized)
                .hasMessageContaining("Only approved calibration");
    }

    @Test
    void finalizationRequiresAnApprovedCalibration() {
        PerformanceCalibrationCase calibration = caseWithVoters(1);
        calibration.proposalSubmitted("assessment-final", 1);

        assertThatThrownBy(calibration::finalized)
                .hasMessageContaining("Only approved calibration");

        calibration.approve();
        calibration.finalized();
        assertThat(calibration.getStatus()).isEqualTo(PerformanceCalibrationCase.Status.FINALIZED);
    }

    private static PerformanceCalibrationCase caseWithVoters(int voters) {
        return new PerformanceCalibrationCase(
                "AGENCY", "rating-1", "supervisor-1", "pmt-1", 2,
                voters, "EMP-001", Instant.parse("2026-09-25T00:00:00Z")
        );
    }
}
