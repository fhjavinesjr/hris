package com.primehr.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PrimeHrV42ToV43UpgradeTest {
    @Test
    void populatedLearningDataSurvivesRewardsFoundationMigration() throws Exception {
        String database = "primehr_v43_" + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:h2:mem:" + database + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Map<String, String> placeholders = Map.of("primehrSchema", "PUBLIC");
        Flyway initial = flyway(url, placeholders, "42");
        initial.migrate();
        try {
            try (var connection = DriverManager.getConnection(url, "sa", "");
                 var result = connection.createStatement().executeQuery("SELECT COUNT(*) FROM PUBLIC.ld_governance_policy")) {
                result.next();
                assertThat(result.getLong(1)).isZero();
            }
            Flyway upgraded = flyway(url, placeholders, "43");
            upgraded.migrate();
            assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("43");
            try (var connection = DriverManager.getConnection(url, "sa", "");
                 var result = connection.createStatement().executeQuery("SELECT COUNT(*) FROM PUBLIC.rr_governance_policy")) {
                result.next();
                assertThat(result.getLong(1)).isZero();
            }
        } finally {
            initial.clean();
        }
    }

    private Flyway flyway(String url, Map<String, String> placeholders, String target) {
        return Flyway.configure().dataSource(url, "sa", "")
                .locations("classpath:db/migration/postgresql").schemas("PUBLIC").defaultSchema("PUBLIC")
                .placeholders(placeholders).target(MigrationVersion.fromVersion(target))
                .cleanDisabled(false).load();
    }
}
