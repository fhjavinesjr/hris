package com.primehr.evidence;

import com.primehr.evidence.domain.EvidenceReadinessSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvidenceReadinessSnapshotDomainTest {
    @Test
    void preservesTransparentFactsHumanSignatureLineageAndNonOfficialBoundary() {
        var value = snapshot(2, "prior", 3, 1, 2, 3, 2,
                new BigDecimal("75.00"), new BigDecimal("66.67"));
        assertThat(value.getSnapshotRevision()).isEqualTo(2);
        assertThat(value.getSupersedesSnapshotId()).isEqualTo("prior");
        assertThat(value.getSignedBy()).isEqualTo("VALIDATOR");
        assertThat(value.getDisclaimer()).contains("not an official CSC PRIME-ESG score")
                .contains("Governance Level");
    }

    @Test
    void rejectsInconsistentCountsAndOutOfRangePercentages() {
        assertThatThrownBy(() -> snapshot(1, null, 3, 1, 1, 3, 0,
                new BigDecimal("101"), BigDecimal.ZERO)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> snapshot(1, null, 3, 1, 1, 4, 0,
                BigDecimal.TEN, BigDecimal.TEN)).isInstanceOf(IllegalArgumentException.class);
    }

    private static EvidenceReadinessSnapshot snapshot(int revision, String prior, int total, int excluded,
            int applicable, int assessed, int covered, BigDecimal completeness, BigDecimal coverage) {
        return new EvidenceReadinessSnapshot("A", "case", "cycle", "framework", 1, 2,
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef", 3, 1,
                revision, prior, total, excluded, applicable, assessed, covered, completeness, coverage,
                1, 1, 1, "{\"requirements\":[]}",
                "1123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                "VALIDATOR", Instant.parse("2026-11-05T08:00:00Z"), "Internal sign-off");
    }
}
