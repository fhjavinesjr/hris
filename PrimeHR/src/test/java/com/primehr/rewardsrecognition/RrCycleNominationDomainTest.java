package com.primehr.rewardsrecognition;

import com.primehr.rewardsrecognition.domain.RrAwardCategoryVersion;
import com.primehr.rewardsrecognition.domain.RrAwardCycle;
import com.primehr.rewardsrecognition.domain.RrNomination;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RrCycleNominationDomainTest {
    @Test
    void cycleRequiresChronologicalWindowsAndAcceptsOnlyInsideOpenWindow() {
        assertThatThrownBy(() -> cycle(LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 1)))
                .hasMessageContaining("chronological");

        RrAwardCycle cycle = cycle(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 10));
        cycle.open("EMP-1", "Call for nominations approved", Instant.parse("2026-02-28T00:00:00Z"));

        assertThat(cycle.acceptsNominations(LocalDate.of(2026, 3, 1))).isTrue();
        assertThat(cycle.acceptsNominations(LocalDate.of(2026, 3, 10))).isTrue();
        assertThat(cycle.acceptsNominations(LocalDate.of(2026, 3, 11))).isFalse();
        assertThatThrownBy(() -> cycle.update("Changed", null, "policy", 1, "fp",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 20),
                LocalDate.of(2026, 3, 21), null)).isInstanceOf(IllegalLifecycleTransitionException.class);
    }

    @Test
    void submittedNominationIsImmutableAndCanOnlyBeWithdrawnOnce() {
        RrNomination nomination = new RrNomination("A", "cycle", "category", 1, "category-fp",
                RrAwardCategoryVersion.NomineeType.INDIVIDUAL, 1L, "EMP-1", "Nominator",
                10L, 20L, "nominator-fp");
        nomination.narratives("Documented contribution", null, null);
        nomination.individual(2L, "EMP-2", "Nominee", 11L, 20L, "nominee-fp");
        nomination.submit("EMP-1", "submission-fp", Instant.parse("2026-03-05T00:00:00Z"));

        assertThat(nomination.getStatus()).isEqualTo(RrNomination.Status.SUBMITTED);
        assertThatThrownBy(() -> nomination.narratives("Changed", null, null))
                .isInstanceOf(IllegalLifecycleTransitionException.class);
        nomination.withdraw("EMP-1", "Withdrawn by nominator", Instant.parse("2026-03-06T00:00:00Z"));
        assertThatThrownBy(() -> nomination.withdraw("EMP-1", "again", Instant.now()))
                .isInstanceOf(IllegalLifecycleTransitionException.class);
    }

    private static RrAwardCycle cycle(LocalDate opens, LocalDate closes) {
        return new RrAwardCycle("A", "CY-2026", "2026 PRAISE", null, "policy", 1, "fp",
                opens, closes, closes, closes.plusDays(10), closes.plusDays(11), closes.plusDays(20));
    }
}
