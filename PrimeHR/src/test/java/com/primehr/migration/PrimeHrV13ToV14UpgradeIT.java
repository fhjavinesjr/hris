package com.primehr.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.flyway.target=13", "spring.jpa.hibernate.ddl-auto=none",
        "primehr.applicant.enabled=false"})
class PrimeHrV13ToV14UpgradeIT {
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbc;
    @Autowired Flyway initial;
    @Value("${spring.flyway.default-schema}") String schema;
    @Value("${spring.flyway.locations}") String location;

    @Test
    void populatedApplicantFoundationSurvivesApplicationIntakeMigration() throws Exception {
        assertThat(initial.info().current().getVersion().getVersion()).isEqualTo("13");
        boolean sqlServer;
        try (var connection = dataSource.getConnection()) {
            sqlServer = connection.getMetaData().getDatabaseProductName().toLowerCase().contains("microsoft");
        }
        String prefix = sqlServer ? "[" + schema + "]." : "\"" + schema + "\".";
        String applicantId = UUID.randomUUID().toString();
        Timestamp now = Timestamp.from(Instant.now());
        jdbc.update("INSERT INTO " + prefix + "rsp_applicant_account "
                        + "(id,agency_id,normalized_email,email,password_hash,display_name,status,failed_attempts,"
                        + "record_version,created_by,created_at,updated_by,updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                applicantId, "UPGRADE-AGENCY", "existing@example.com", "existing@example.com",
                "test-hash", "Existing Applicant", "ACTIVE", 0, 0L, "test", now, "test", now);

        Flyway upgraded = Flyway.configure().dataSource(dataSource).locations(location).schemas(schema)
                .defaultSchema(schema).placeholders(Map.of("primehrSchema", schema))
                .target(MigrationVersion.fromVersion("14")).load();
        upgraded.migrate();

        assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("14");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix
                + "rsp_applicant_account WHERE id=?", Long.class, applicantId)).isEqualTo(1L);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix
                + "rsp_position_application", Long.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix
                + "rsp_application_document_snapshot", Long.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix
                + "rsp_applicant_communication", Long.class)).isZero();
    }
}
