package com.primehr.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.*;
import java.time.*;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Explicit real-provider populated V5-to-V6 upgrade gate. */
@SpringBootTest(properties = {"spring.flyway.target=5", "spring.jpa.hibernate.ddl-auto=none"})
class PrimeHrV5ToV6UpgradeIT {
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbc;
    @Autowired Flyway initialFlyway;
    @Value("${spring.flyway.default-schema}") String schema;
    @Value("${spring.flyway.locations}") String migrationLocation;

    @Test
    void populatedV5ProfileUpgradesToV6WithoutChangingHistory() throws Exception {
        assertThat(initialFlyway.info().current().getVersion().getVersion()).isEqualTo("5");
        boolean sqlServer;
        try (var connection = dataSource.getConnection()) {
            sqlServer = connection.getMetaData().getDatabaseProductName().toLowerCase().contains("microsoft");
        }
        String prefix = sqlServer ? "[" + schema + "]." : "\"" + schema + "\".";
        String id = UUID.randomUUID().toString();
        Timestamp now = Timestamp.from(Instant.now());
        jdbc.update("INSERT INTO " + prefix + "prime_position_profile " +
                        "(id,agency_id,target_type,target_key,job_position_id,plantilla_id,name,description,status," +
                        "definition_version,supersedes_id,source_job_position_name,source_salary_grade," +
                        "source_salary_step,source_plantilla_name,source_fingerprint,source_snapshot_at," +
                        "content_revision,active,display_order,effective_from,effective_to,record_version," +
                        "created_by,created_at,updated_by,updated_at,submitted_by,submitted_at,approved_by,approved_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                id, "UPGRADE-AGENCY", "JOB_POSITION", "JOB_POSITION:3000", 3000L, null,
                "Existing V5 profile", null, "DRAFT", 1, null, "Existing position", 15L, 1L,
                null, "existing-v5-fingerprint", now, 1L, false, 0,
                Date.valueOf(LocalDate.of(2028, 1, 1)), null, 0L,
                "upgrade-test", now, "upgrade-test", now, null, null, null, null);

        Flyway upgraded = Flyway.configure().dataSource(dataSource).locations(migrationLocation)
                .schemas(schema).defaultSchema(schema).placeholders(Map.of("primehrSchema", schema))
                .target(MigrationVersion.fromVersion("6")).load();
        upgraded.migrate();

        assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("6");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix
                + "prime_position_profile WHERE id = ?", Long.class, id)).isEqualTo(1L);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix
                + "prime_assessment_cycle", Long.class)).isZero();
    }
}
