package com.primehr.migration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RrV44MigrationParityTest {
    @Test
    void pairedMigrationsRetainTheSameTablesConstraintsAndIndexes() throws IOException {
        String postgres = read("db/migration/postgresql/V44__rr_cycles_nominations_evidence.sql").toLowerCase();
        String sqlServer = read("db/migration/sqlserver/V44__rr_cycles_nominations_evidence.sql").toLowerCase();
        Set<String> tables = Set.of("rr_award_cycle", "rr_nomination", "rr_nomination_member",
                "rr_nomination_source", "rr_nomination_evidence");
        Set<String> contracts = Set.of("uk_rr_cycle_code", "ck_rr_cycle_windows",
                "ck_rr_nomination_subject", "uk_rr_nomination_member", "uk_rr_nomination_source",
                "ix_rr_cycle_window", "ix_rr_nomination_cycle", "ix_rr_nomination_owner",
                "ix_rr_nomination_member_nomination", "ix_rr_nomination_source_nomination",
                "ix_rr_nomination_evidence_nomination");
        for (String migration : Set.of(postgres, sqlServer)) {
            assertThat(migration).contains(tables.toArray(String[]::new));
            assertThat(migration).contains(contracts.toArray(String[]::new));
            assertThat(migration).doesNotContain("insert into", "delete from", "drop table",
                    "employeeappointment", "manage_personnel");
        }
    }

    private String read(String path) throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (stream == null) throw new IOException("Missing migration " + path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
