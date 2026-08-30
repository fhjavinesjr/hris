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

/** Explicit real-provider populated V11-to-V12 upgrade gate. */
@SpringBootTest(properties = {"spring.flyway.target=11", "spring.jpa.hibernate.ddl-auto=none"})
class PrimeHrV11ToV12UpgradeIT {
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbc;
    @Autowired Flyway initialFlyway;
    @Value("${spring.flyway.default-schema}") String schema;
    @Value("${spring.flyway.locations}") String migrationLocation;

    @Test
    void populatedV11PlanningDataUpgradesToV12WithoutMutation() throws Exception {
        assertThat(initialFlyway.info().current().getVersion().getVersion()).isEqualTo("11");
        boolean sqlServer;
        try (var connection = dataSource.getConnection()) {
            sqlServer = connection.getMetaData().getDatabaseProductName().toLowerCase().contains("microsoft");
        }
        String prefix = sqlServer ? "[" + schema + "]." : "\"" + schema + "\".";
        String planId = UUID.randomUUID().toString();
        Timestamp now = Timestamp.from(Instant.now());
        jdbc.update("INSERT INTO " + prefix + "rsp_recruitment_plan " +
                        "(id,agency_id,code,title,period_start,period_end,description,status,record_version," +
                        "created_by,created_at,updated_by,updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                planId, "UPGRADE-AGENCY", "V11-PLAN", "Existing V11 plan",
                Date.valueOf(LocalDate.of(2028, 1, 1)), Date.valueOf(LocalDate.of(2028, 12, 31)),
                null, "DRAFT", 0L, "upgrade-test", now, "upgrade-test", now);

        Flyway upgraded = Flyway.configure().dataSource(dataSource).locations(migrationLocation)
                .schemas(schema).defaultSchema(schema).placeholders(Map.of("primehrSchema", schema))
                .target(MigrationVersion.fromVersion("12")).load();
        upgraded.migrate();

        assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("12");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix +
                "rsp_recruitment_plan WHERE id=?", Long.class, planId)).isEqualTo(1L);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix +
                "rsp_vacancy_publication", Long.class)).isZero();
    }
}
