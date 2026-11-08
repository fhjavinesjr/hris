package com.primehr.migration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EvidenceV54MigrationParityTest {

    @Test
    void pairedMigrationsHaveMatchingProviderNeutralStructures() throws Exception {
        String postgres = read("db/migration/postgresql/V54__evidence_assessment_workflow.sql").toLowerCase();
        String sqlServer = read("db/migration/sqlserver/V54__evidence_assessment_workflow.sql").toLowerCase();
        Set<String> names = Set.of(
                "evidence_assessment_cycle", "evidence_assessment_case", "evidence_artifact",
                "evidence_finding", "evidence_gap", "evidence_corrective_action",
                "evidence_assessment_event", "ix_evidence_cycle_period", "ix_evidence_case_assignment",
                "ix_evidence_artifact_case", "ix_evidence_finding_case", "ix_evidence_gap_case",
                "ix_evidence_action_owner", "ix_evidence_event_case");

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
