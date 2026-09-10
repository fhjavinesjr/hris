package com.primehr.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PrimeHrV27ToV28UpgradeTest {
    @Test
    void populatedTemplateSurvivesPerformancePlanningFoundationMigration() throws Exception {
        String database = "primehr_v28_" + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:h2:mem:" + database + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Map<String, String> placeholders = Map.of("primehrSchema", "PUBLIC");
        Flyway initial = Flyway.configure().dataSource(url, "sa", "")
                .locations("classpath:db/migration/postgresql").schemas("PUBLIC").defaultSchema("PUBLIC")
                .placeholders(placeholders).target(MigrationVersion.fromVersion("27")).cleanDisabled(false).load();
        initial.migrate();

        String templateId = UUID.randomUUID().toString();
        try (var connection = DriverManager.getConnection(url, "sa", "")) {
            connection.prepareStatement("INSERT INTO PUBLIC.spms_template " +
                    "(id,agency_id,code,normalized_code,record_version,created_by,created_at,updated_by,updated_at) " +
                    "VALUES ('" + templateId + "','UPGRADE-28','OPCR','OPCR',0,'test',CURRENT_TIMESTAMP,'test',CURRENT_TIMESTAMP)").executeUpdate();
        }

        try {
            Flyway upgraded = Flyway.configure().dataSource(url, "sa", "")
                    .locations("classpath:db/migration/postgresql").schemas("PUBLIC").defaultSchema("PUBLIC")
                    .placeholders(placeholders).target(MigrationVersion.fromVersion("28")).cleanDisabled(false).load();
            upgraded.migrate();
            assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("28");
            try (var connection = DriverManager.getConnection(url, "sa", "");
                 var result = connection.createStatement().executeQuery(
                         "SELECT COUNT(*) FROM PUBLIC.spms_template WHERE id='" + templateId + "'")) {
                result.next();
                assertThat(result.getLong(1)).isEqualTo(1L);
                for (String table : java.util.List.of("spms_objective", "spms_objective_version",
                        "spms_plan_assignment", "spms_plan_assignment_objective")) {
                    try (var empty = connection.createStatement().executeQuery("SELECT COUNT(*) FROM PUBLIC." + table)) {
                        empty.next();
                        assertThat(empty.getLong(1)).isZero();
                    }
                }
            }
        } finally {
            initial.clean();
        }
    }
}
