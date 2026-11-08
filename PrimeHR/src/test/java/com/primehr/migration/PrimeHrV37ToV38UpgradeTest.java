package com.primehr.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PrimeHrV37ToV38UpgradeTest {
    @Test
    void populatedLearningFoundationDataSurvivesNeedsAndIdpMigration() throws Exception {
        String database = "primehr_v38_" + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:h2:mem:" + database + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Map<String, String> placeholders = Map.of("primehrSchema", "PUBLIC");
        Flyway initial = flyway(url, placeholders, "37");
        initial.migrate();
        String providerId = UUID.randomUUID().toString();
        try {
            try (var connection = DriverManager.getConnection(url, "sa", "");
                 var statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO PUBLIC.ld_provider "
                        + "(id,agency_id,code,normalized_code,name,provider_type,status,record_version,created_by,created_at,updated_by,updated_at) "
                        + "VALUES ('" + providerId + "','UPGRADE-38','P-1','P-1','Provider','PRIVATE','ACTIVE',0,'test',CURRENT_TIMESTAMP,'test',CURRENT_TIMESTAMP)");
            }
            Flyway upgraded = flyway(url, placeholders, "38");
            upgraded.migrate();
            assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("38");
            try (var connection = DriverManager.getConnection(url, "sa", "");
                 var existing = connection.createStatement().executeQuery(
                         "SELECT COUNT(*) FROM PUBLIC.ld_provider WHERE id='" + providerId + "'")) {
                existing.next();
                assertThat(existing.getLong(1)).isOne();
            }
            try (var connection = DriverManager.getConnection(url, "sa", "");
                 var fresh = connection.createStatement().executeQuery(
                         "SELECT COUNT(*) FROM PUBLIC.ld_learning_need")) {
                fresh.next();
                assertThat(fresh.getLong(1)).isZero();
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
