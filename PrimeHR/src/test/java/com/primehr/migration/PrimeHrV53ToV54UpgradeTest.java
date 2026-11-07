package com.primehr.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PrimeHrV53ToV54UpgradeTest {

    @Test
    void populatedEvidenceFrameworkSurvivesAssessmentWorkflowMigration() throws Exception {
        String database = "primehr_v54_" + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:h2:mem:" + database + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Map<String, String> placeholders = Map.of("primehrSchema", "PUBLIC");
        Flyway initial = flyway(url, placeholders, "53");
        initial.migrate();
        try {
            try (var connection = DriverManager.getConnection(url, "sa", "");
                 var statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO PUBLIC.evidence_framework" +
                        "(id,agency_id,code,normalized_code,record_version,created_by,created_at,updated_by,updated_at) " +
                        "VALUES('framework','A','PRIME-HRM','PRIME-HRM',0,'test',CURRENT_TIMESTAMP,'test',CURRENT_TIMESTAMP)");
            }

            Flyway upgraded = flyway(url, placeholders, "54");
            upgraded.migrate();
            assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("54");
            try (var connection = DriverManager.getConnection(url, "sa", "");
                 var result = connection.createStatement().executeQuery(
                         "SELECT COUNT(*) FROM PUBLIC.evidence_framework WHERE id='framework'")) {
                result.next();
                assertThat(result.getLong(1)).isOne();
            }
            try (var connection = DriverManager.getConnection(url, "sa", "");
                 var result = connection.createStatement().executeQuery(
                         "SELECT COUNT(*) FROM PUBLIC.evidence_assessment_case")) {
                result.next();
                assertThat(result.getLong(1)).isZero();
            }
        } finally {
            initial.clean();
        }
    }

    private Flyway flyway(String url, Map<String, String> placeholders, String target) {
        return Flyway.configure().dataSource(url, "sa", "")
                .locations("classpath:db/migration/postgresql")
                .schemas("PUBLIC").defaultSchema("PUBLIC").placeholders(placeholders)
                .target(MigrationVersion.fromVersion(target)).cleanDisabled(false).load();
    }
}
