package com.primehr.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Explicit real-provider populated V4-to-V5 upgrade gate. Ordinary test discovery excludes this IT class. */
@SpringBootTest(properties = {
        "spring.flyway.target=4",
        "spring.jpa.hibernate.ddl-auto=none"
})
class PrimeHrV4ToV5UpgradeIT {
    @Autowired private DataSource dataSource;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private Flyway initialFlyway;
    @Value("${spring.flyway.default-schema}") private String schema;
    @Value("${spring.flyway.locations}") private String migrationLocation;

    @Test
    void populatedV4DraftUpgradesToV5WithoutChangingProfileHistory() throws Exception {
        assertThat(initialFlyway.info().current().getVersion().getVersion()).isEqualTo("4");
        assertThat(schema).matches("[A-Za-z0-9_]+");
        boolean sqlServer;
        try (var connection = dataSource.getConnection()) {
            sqlServer = connection.getMetaData().getDatabaseProductName().toLowerCase().contains("microsoft");
        }
        String prefix = sqlServer ? "[" + schema + "]." : "\"" + schema + "\".";
        String id = UUID.randomUUID().toString();
        Timestamp now = Timestamp.from(Instant.now());
        jdbcTemplate.update("INSERT INTO " + prefix + "prime_position_profile " +
                        "(id, agency_id, target_type, target_key, job_position_id, plantilla_id, name, description, " +
                        "status, definition_version, supersedes_id, source_job_position_name, source_salary_grade, " +
                        "source_salary_step, source_plantilla_name, source_fingerprint, source_snapshot_at, " +
                        "content_revision, active, display_order, effective_from, effective_to, record_version, " +
                        "created_by, created_at, updated_by, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, "UPGRADE-AGENCY", "JOB_POSITION", "JOB_POSITION:1400", 1400L, null,
                "Existing V4 draft", null, "DRAFT", 1, null, "Existing position", 15L, 1L,
                null, "existing-v4-fingerprint", now, 1L, false, 0,
                Date.valueOf(LocalDate.of(2028, 1, 1)), null, 0L,
                "upgrade-test", now, "upgrade-test", now);

        Flyway upgraded = Flyway.configure().dataSource(dataSource).locations(migrationLocation)
                .schemas(schema).defaultSchema(schema).placeholders(Map.of("primehrSchema", schema))
                .target(MigrationVersion.fromVersion("5")).load();
        upgraded.migrate();

        assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("5");
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + prefix
                + "prime_position_profile WHERE id = ? AND status = 'DRAFT'", Long.class, id)).isEqualTo(1L);
        Map<String, Object> metadata = jdbcTemplate.queryForMap("SELECT submitted_by, submitted_at, " +
                "approved_by, approved_at FROM " + prefix + "prime_position_profile WHERE id = ?", id);
        assertThat(metadata.values()).containsOnlyNulls();
    }
}
