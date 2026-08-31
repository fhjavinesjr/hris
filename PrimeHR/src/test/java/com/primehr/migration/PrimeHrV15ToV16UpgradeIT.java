package com.primehr.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.flyway.target=15", "spring.jpa.hibernate.ddl-auto=none",
        "primehr.applicant.enabled=false"})
class PrimeHrV15ToV16UpgradeIT {
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbc;
    @Autowired Flyway initial;
    @Value("${spring.flyway.default-schema}") String schema;
    @Value("${spring.flyway.locations}") String location;

    @Test
    void populatedSubmittedApplicationSurvivesAssignedScreeningMigration() throws Exception {
        assertThat(initial.info().current().getVersion().getVersion()).isEqualTo("15");
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).containsIgnoringCase("Microsoft");
        }
        String prefix = "[" + schema + "].";
        String applicationId = UUID.randomUUID().toString();
        Timestamp now = Timestamp.from(Instant.now());

        // The disposable upgrade database isolates the altered application row from unrelated
        // V11/V12 fixture aggregates. Constraints are re-enabled before Flyway V16 executes.
        jdbc.execute("ALTER TABLE " + prefix + "rsp_position_application NOCHECK CONSTRAINT ALL");
        jdbc.update("INSERT INTO " + prefix + "rsp_position_application " +
                        "(id,agency_id,applicant_id,vacancy_publication_id,application_version,status,safe_status," +
                        "acknowledgment_number,privacy_notice_id,privacy_notice_version,vacancy_snapshot," +
                        "qualification_snapshot,competency_snapshot,profile_snapshot,draft_updated_at,submitted_at," +
                        "record_version,created_by,created_at,updated_by,updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                applicationId, "UPGRADE-16", "fixture-applicant", "fixture-publication", 1,
                "SUBMITTED", "SUBMITTED", "ACK-16", "fixture-notice", 1,
                "{}", "{}", "[]", "{}", now, now, 0L, "test", now, "test", now);
        jdbc.execute("ALTER TABLE " + prefix + "rsp_position_application CHECK CONSTRAINT ALL");

        Flyway upgraded = Flyway.configure().dataSource(dataSource).locations(location).schemas(schema)
                .defaultSchema(schema).placeholders(Map.of("primehrSchema", schema))
                .target(MigrationVersion.fromVersion("16")).load();
        upgraded.migrate();

        assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("16");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix +
                "rsp_position_application WHERE id=?", Long.class, applicationId)).isEqualTo(1L);
        assertThat(jdbc.queryForObject("SELECT status FROM " + prefix +
                "rsp_position_application WHERE id=?", String.class, applicationId)).isEqualTo("SUBMITTED");
        for (String table : java.util.List.of("rsp_screening_case", "rsp_screening_assignment",
                "rsp_screening_finding", "rsp_screening_evidence_link", "rsp_screening_decision")) {
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix + table, Long.class)).isZero();
        }
    }
}
