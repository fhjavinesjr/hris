package com.primehr.migration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RrV43MigrationParityTest {
    private static final String POSTGRES = "db/migration/postgresql/V43__rr_governance_categories_committee.sql";
    private static final String SQL_SERVER = "db/migration/sqlserver/V43__rr_governance_categories_committee.sql";

    @Test
    void pairedMigrationsRetainTheSameTablesConstraintsAndIndexes() throws IOException {
        String postgres = read(POSTGRES).toLowerCase();
        String sqlServer = read(SQL_SERVER).toLowerCase();
        Set<String> tables = Set.of("rr_governance_policy", "rr_governance_policy_version",
                "rr_award_category", "rr_award_category_version", "rr_award_criterion",
                "rr_committee_charter", "rr_committee_charter_version", "rr_committee_member");
        Set<String> contracts = Set.of("uk_rr_policy_code", "uk_rr_policy_version", "uk_rr_category_code",
                "uk_rr_category_version", "uk_rr_criterion_code", "uk_rr_criterion_order",
                "uk_rr_charter_code", "uk_rr_charter_version", "uk_rr_member_employee",
                "ix_rr_policy_effective", "ix_rr_category_effective", "ix_rr_criterion_category",
                "ix_rr_charter_effective", "ix_rr_member_charter");
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
