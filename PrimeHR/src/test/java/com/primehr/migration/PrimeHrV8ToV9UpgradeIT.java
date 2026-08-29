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

/** Explicit real-provider populated V8-to-V9 upgrade gate. */
@SpringBootTest(properties = {"spring.flyway.target=8", "spring.jpa.hibernate.ddl-auto=none"})
class PrimeHrV8ToV9UpgradeIT {
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbc;
    @Autowired Flyway initialFlyway;
    @Value("${spring.flyway.default-schema}") String schema;
    @Value("${spring.flyway.locations}") String migrationLocation;

    @Test
    void populatedV8HistoryUpgradesToV9WithoutMutation() throws Exception {
        assertThat(initialFlyway.info().current().getVersion().getVersion()).isEqualTo("8");
        boolean sqlServer;
        try (var connection = dataSource.getConnection()) {
            sqlServer = connection.getMetaData().getDatabaseProductName().toLowerCase().contains("microsoft");
        }
        String prefix = sqlServer ? "[" + schema + "]." : "\"" + schema + "\".";
        String id = UUID.randomUUID().toString();
        Timestamp now = Timestamp.from(Instant.now());
        jdbc.update("INSERT INTO " + prefix + "prime_assessment_cycle "
                        + "(id,agency_id,code,name,description,status,active,display_order,effective_from,effective_to,"
                        + "record_version,created_by,created_at,updated_by,updated_at) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                id, "UPGRADE-AGENCY", "V8-CYCLE", "Existing V8 cycle", null, "DRAFT", true, 0,
                Date.valueOf(LocalDate.of(2028, 1, 1)), Date.valueOf(LocalDate.of(2028, 12, 31)),
                0L, "upgrade-test", now, "upgrade-test", now);

        Flyway upgraded = Flyway.configure().dataSource(dataSource).locations(migrationLocation)
                .schemas(schema).defaultSchema(schema).placeholders(Map.of("primehrSchema", schema))
                .target(MigrationVersion.fromVersion("9")).load();
        upgraded.migrate();

        assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("9");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix
                + "prime_assessment_cycle WHERE id = ?", Long.class, id)).isEqualTo(1L);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix
                + "prime_gap_priority_scheme", Long.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix
                + "prime_competency_gap_analysis", Long.class)).isZero();
    }
}
