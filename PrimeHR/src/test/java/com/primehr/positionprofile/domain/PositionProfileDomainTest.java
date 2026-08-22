package com.primehr.positionprofile.domain;

import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PositionProfileDomainTest {
    @Test
    void plantillaIdentityRetainsItsParentJobPosition() {
        PositionTargetSnapshot target = new PositionTargetSnapshot(PositionTargetType.PLANTILLA, 25L,
                14L, "Administrative Officer IV", 15L, 2L, 25L, "HRMO-001", "fingerprint", Instant.now());

        PositionProfile profile = PositionProfile.draft("DEFAULT", target, "HRMO Profile", null,
                LocalDate.of(2026, 8, 12), null);

        assertThat(profile.getTargetKey()).isEqualTo("PLANTILLA:25");
        assertThat(profile.getJobPositionId()).isEqualTo(14L);
        assertThat(profile.getPlantillaId()).isEqualTo(25L);
        assertThat(profile.getStatus()).isEqualTo(PositionProfileStatus.DRAFT);
    }

    @Test
    void targetCannotChangeDuringDraftEditing() {
        PositionProfile profile = PositionProfile.draft("DEFAULT", job(14L), "Profile", null, null, null);

        assertThatThrownBy(() -> profile.updateDraft("Changed", null, null, null, job(15L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("target cannot change");
    }

    @Test
    void archivedDraftIsImmutable() {
        PositionProfile profile = PositionProfile.draft("DEFAULT", job(14L), "Profile", null, null, null);
        profile.archiveDraft();

        assertThatThrownBy(() -> profile.updateDraft("Changed", null, null, null, job(14L)))
                .isInstanceOf(IllegalLifecycleTransitionException.class);
    }

    private static PositionTargetSnapshot job(long id) {
        return new PositionTargetSnapshot(PositionTargetType.JOB_POSITION, id, id, "Position " + id,
                12L, 1L, null, null, "fingerprint-" + id, Instant.now());
    }
}
