package com.primehr.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PrimeHrV35ToV36UpgradeTest {
    @Test
    void populatedPerformanceFoundationSurvivesReportSnapshotMigration() throws Exception {
        String database = "primehr_v36_" + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:h2:mem:" + database + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Map<String, String> placeholders = Map.of("primehrSchema", "PUBLIC");
        Flyway initial = Flyway.configure().dataSource(url, "sa", "")
                .locations("classpath:db/migration/postgresql").schemas("PUBLIC").defaultSchema("PUBLIC")
                .placeholders(placeholders).target(MigrationVersion.fromVersion("35")).cleanDisabled(false).load();
        initial.migrate();
        String id = UUID.randomUUID().toString();
        try {
            try (var connection = DriverManager.getConnection(url, "sa", ""); var statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO PUBLIC.spms_objective (id,agency_id,code,normalized_code,record_version,created_by,created_at,updated_by,updated_at) VALUES ('"
                        + id + "','UPGRADE-36','OBJ','OBJ',0,'test',CURRENT_TIMESTAMP,'test',CURRENT_TIMESTAMP)");
            }
            Flyway upgraded = Flyway.configure().dataSource(url, "sa", "")
                    .locations("classpath:db/migration/postgresql").schemas("PUBLIC").defaultSchema("PUBLIC")
                    .placeholders(placeholders).target(MigrationVersion.fromVersion("36")).cleanDisabled(false).load();
            upgraded.migrate();
            assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("36");
            try (var connection = DriverManager.getConnection(url, "sa", "");
                 var existing = connection.createStatement().executeQuery("SELECT COUNT(*) FROM PUBLIC.spms_objective WHERE id='" + id + "'")) {
                existing.next();
                assertThat(existing.getLong(1)).isOne();
            }
            try (var connection = DriverManager.getConnection(url, "sa", "");
                 var snapshots = connection.createStatement().executeQuery("SELECT COUNT(*) FROM PUBLIC.spms_report_metadata_snapshot")) {
                snapshots.next();
                assertThat(snapshots.getLong(1)).isZero();
            }
        } finally {
            initial.clean();
        }
    }
}
