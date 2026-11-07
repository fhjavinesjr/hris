package com.primehr.migration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EvidenceV55MigrationParityTest {
    @Test
    void pairedMigrationsHaveMatchingImmutableSnapshotStructures() throws Exception {
        String postgres = read("db/migration/postgresql/V55__evidence_readiness_reporting.sql").toLowerCase();
        String sqlServer = read("db/migration/sqlserver/V55__evidence_readiness_reporting.sql").toLowerCase();
        Set<String> names = Set.of("evidence_readiness_snapshot", "uk_evidence_snapshot_revision",
                "fk_evidence_snapshot_case", "fk_evidence_snapshot_cycle", "fk_evidence_snapshot_framework",
                "fk_evidence_snapshot_prior", "ix_evidence_snapshot_case", "ix_evidence_snapshot_signed",
                "facts_fingerprint", "supersedes_snapshot_id", "disclaimer");
        for (String migration : Set.of(postgres, sqlServer)) {
            assertThat(migration).contains(names.toArray(String[]::new));
            assertThat(migration).doesNotContain("insert into", "delete from", "drop table",
                    "employeeappointment", "payroll_transaction");
        }
    }

    private String read(String path) throws IOException {
        try (var input = getClass().getClassLoader().getResourceAsStream(path)) {
            if (input == null) throw new IOException(path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
