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

/** Explicit real-provider populated V6-to-V7 upgrade gate. */
@SpringBootTest(properties = {"spring.flyway.target=6", "spring.jpa.hibernate.ddl-auto=none"})
class PrimeHrV6ToV7UpgradeIT {
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbc;
    @Autowired Flyway initialFlyway;
    @Value("${spring.flyway.default-schema}") String schema;
    @Value("${spring.flyway.locations}") String migrationLocation;

    @Test
    void populatedV6CycleUpgradesToV7WithoutChangingHistory() throws Exception {
        assertThat(initialFlyway.info().current().getVersion().getVersion()).isEqualTo("6");
        boolean sqlServer;
        try (var connection = dataSource.getConnection()) {
            sqlServer = connection.getMetaData().getDatabaseProductName().toLowerCase().contains("microsoft");
        }
        String prefix = sqlServer ? "[" + schema + "]." : "\"" + schema + "\".";
        String id = UUID.randomUUID().toString();
        Timestamp now = Timestamp.from(Instant.now());
        jdbc.update("INSERT INTO " + prefix + "prime_assessment_cycle " +
                        "(id,agency_id,code,name,description,status,active,display_order,effective_from,effective_to," +
                        "record_version,created_by,created_at,updated_by,updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                id, "UPGRADE-AGENCY", "V6-CYCLE", "Existing V6 cycle", null, "DRAFT", true, 0,
                Date.valueOf(LocalDate.of(2028, 1, 1)), Date.valueOf(LocalDate.of(2028, 12, 31)),
                0L, "upgrade-test", now, "upgrade-test", now);

        Flyway upgraded = Flyway.configure().dataSource(dataSource).locations(migrationLocation)
                .schemas(schema).defaultSchema(schema).placeholders(Map.of("primehrSchema", schema))
                .target(MigrationVersion.fromVersion("7")).load();
        upgraded.migrate();

        assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("7");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix
                + "prime_assessment_cycle WHERE id = ?", Long.class, id)).isEqualTo(1L);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix
                + "prime_assessment_rating", Long.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix
                + "prime_assessment_evidence", Long.class)).isZero();
    }
}
