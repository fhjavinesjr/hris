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

/** Explicit real-provider populated V3-to-V4 upgrade gate. Ordinary test discovery excludes this IT class. */
@SpringBootTest(properties = {
        "spring.flyway.target=3",
        "spring.jpa.hibernate.ddl-auto=none"
})
class PrimeHrV3ToV4UpgradeIT {
    @Autowired private DataSource dataSource;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private Flyway initialFlyway;
    @Value("${spring.flyway.default-schema}") private String schema;
    @Value("${spring.flyway.locations}") private String migrationLocation;

    @Test
    void populatedV3SchemaUpgradesToV4WithoutChangingCompetencyHistory() throws Exception {
        assertThat(initialFlyway.info().current().getVersion().getVersion()).isEqualTo("3");
        assertThat(schema).matches("[A-Za-z0-9_]+");
        boolean sqlServer;
        try (var connection = dataSource.getConnection()) {
            sqlServer = connection.getMetaData().getDatabaseProductName().toLowerCase().contains("microsoft");
        }
        String prefix = sqlServer ? "[" + schema + "]." : "\"" + schema + "\".";
        String id = UUID.randomUUID().toString();
        Timestamp now = Timestamp.from(Instant.now());
        jdbcTemplate.update("INSERT INTO " + prefix + "prime_competency_category " +
                        "(id, agency_id, code, name, active, display_order, effective_from, effective_to, " +
                        "record_version, created_by, created_at, updated_by, updated_at, status, " +
                        "definition_version, supersedes_id, published_at, published_by) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, "UPGRADE-AGENCY", "V3HISTORY", "Existing V3 history", true, 1, null, null,
                0L, "upgrade-test", now, "upgrade-test", now, "ACTIVE", 1, null, now, "publisher");

        Flyway upgraded = Flyway.configure().dataSource(dataSource).locations(migrationLocation)
                .schemas(schema).defaultSchema(schema).placeholders(Map.of("primehrSchema", schema))
                .target(MigrationVersion.fromVersion("4")).load();
        upgraded.migrate();

        assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("4");
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + prefix
                + "prime_competency_category WHERE id = ?", Long.class, id)).isEqualTo(1L);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + prefix
                + "prime_position_profile", Long.class)).isZero();
    }
}
