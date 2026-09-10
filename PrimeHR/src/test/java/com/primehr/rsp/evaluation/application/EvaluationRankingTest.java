package com.primehr.rsp.evaluation.application;

import com.primehr.rsp.evaluation.domain.EvaluationPolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EvaluationRankingTest {
    @Test
    void competitionAndDenseRankingAreDeterministicForTiesAndExclusions() {
        List<EvaluationExecutionServiceImpl.CalculatedCandidate> competition = candidates();
        EvaluationExecutionServiceImpl.assignRanks(competition, EvaluationPolicy.TieRule.COMPETITION);
        assertThat(competition).extracting(EvaluationExecutionServiceImpl.CalculatedCandidate::rankNumber)
                .containsExactly(1, 1, 3, 4);
        assertThat(competition).extracting(EvaluationExecutionServiceImpl.CalculatedCandidate::tieGroup)
                .containsExactly(1, 1, 2, 4);

        List<EvaluationExecutionServiceImpl.CalculatedCandidate> dense = candidates();
        EvaluationExecutionServiceImpl.assignRanks(dense, EvaluationPolicy.TieRule.DENSE);
        assertThat(dense).extracting(EvaluationExecutionServiceImpl.CalculatedCandidate::rankNumber)
                .containsExactly(1, 1, 2, 4);
    }

    private static List<EvaluationExecutionServiceImpl.CalculatedCandidate> candidates() {
        return new ArrayList<>(List.of(
                candidate("A", "90.00", false),
                candidate("B", "90.0", false),
                candidate("C", "80.00", false),
                candidate("D", "99.00", true)
        ));
    }

    private static EvaluationExecutionServiceImpl.CalculatedCandidate candidate(
            String id, String score, boolean excluded) {
        return new EvaluationExecutionServiceImpl.CalculatedCandidate(
                id, new BigDecimal(score), 0, 0, excluded,
                excluded ? "FAILED_GATE" : null, List.of());
    }
}
