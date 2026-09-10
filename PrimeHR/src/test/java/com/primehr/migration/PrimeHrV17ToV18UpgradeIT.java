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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.jpa.hibernate.ddl-auto=none", "primehr.applicant.enabled=false"})
class PrimeHrV17ToV18UpgradeIT {
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbc;
    @Value("${spring.flyway.locations}") String location;

    @Test
    void populatedScreeningPolicySurvivesEvaluationGovernanceMigration() throws Exception {
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).containsIgnoringCase("Microsoft");
        }
        String schema = "primehr_v18_" + UUID.randomUUID().toString().replace("-", "");
        Flyway initial = Flyway.configure().dataSource(dataSource).locations(location).schemas(schema)
                .defaultSchema(schema).placeholders(Map.of("primehrSchema", schema))
                .target(MigrationVersion.fromVersion("17")).cleanDisabled(false).load();
        initial.migrate();
        assertThat(initial.info().current().getVersion().getVersion()).isEqualTo("17");
        String prefix = "[" + schema + "].";
        String policyId = UUID.randomUUID().toString();
        Timestamp now = Timestamp.from(Instant.now());
        jdbc.update("INSERT INTO " + prefix + "rsp_screening_policy " +
                        "(id,agency_id,code,normalized_code,name,description,definition_version,supersedes_id,status," +
                        "effective_from,effective_to,published_by,published_at,record_version,created_by,created_at," +
                        "updated_by,updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                policyId, "UPGRADE-18", "SCREEN-18", "SCREEN-18", "Preserved policy", null, 1, null,
                "DRAFT", null, null, null, null, 0L, "test", now, "test", now);

        try {
            Flyway upgraded = Flyway.configure().dataSource(dataSource).locations(location).schemas(schema)
                    .defaultSchema(schema).placeholders(Map.of("primehrSchema", schema))
                    .target(MigrationVersion.fromVersion("18")).cleanDisabled(false).load();
            upgraded.migrate();
            assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("18");
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix +
                    "rsp_screening_policy WHERE id=?", Long.class, policyId)).isEqualTo(1L);
            for (String table : List.of("rsp_evaluation_policy", "rsp_evaluation_policy_stage",
                    "rsp_evaluation_policy_criterion", "rsp_publication_evaluation_policy", "prime_committee",
                    "prime_committee_member", "rsp_evaluation_proceeding", "rsp_evaluation_candidate")) {
                assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + prefix + table, Long.class)).isZero();
            }
        } finally {initial.clean();}
    }
}
