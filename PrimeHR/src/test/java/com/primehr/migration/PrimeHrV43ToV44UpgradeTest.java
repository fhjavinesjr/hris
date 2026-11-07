package com.primehr.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PrimeHrV43ToV44UpgradeTest {
    @Test
    void populatedRewardsFoundationSurvivesCycleAndNominationMigration() throws Exception {
        String database = "primehr_v44_" + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:h2:mem:" + database + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Map<String, String> placeholders = Map.of("primehrSchema", "PUBLIC");
        Flyway initial = flyway(url, placeholders, "43");
        initial.migrate();
        try {
            try (var connection = DriverManager.getConnection(url, "sa", "")) {
                connection.createStatement().executeUpdate("INSERT INTO PUBLIC.rr_governance_policy(id,agency_id,code,normalized_code,created_by,created_at,updated_by,updated_at) VALUES('policy','A','PRAISE','PRAISE','test',CURRENT_TIMESTAMP,'test',CURRENT_TIMESTAMP)");
                connection.createStatement().executeUpdate("INSERT INTO PUBLIC.rr_governance_policy_version(id,agency_id,policy_id,definition_version,title,legal_basis,policy_reference,committee_required,allows_individual_awards,allows_group_awards,allows_organization_awards,allows_monetary_incentives,allows_nonmonetary_incentives,requires_agency_head_approval,annual_report_required,status,effective_from,published_by,published_at,record_version,created_by,created_at,updated_by,updated_at) VALUES('policy-v1','A','policy',1,'PRAISE','CSC MC','Resolution',TRUE,TRUE,TRUE,TRUE,TRUE,TRUE,TRUE,TRUE,'PUBLISHED',DATE '2026-01-01','test',CURRENT_TIMESTAMP,0,'test',CURRENT_TIMESTAMP,'test',CURRENT_TIMESTAMP)");
            }
            Flyway upgraded = flyway(url, placeholders, "44");
            upgraded.migrate();
            assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("44");
            try (var connection = DriverManager.getConnection(url, "sa", "");
                 var existing = connection.createStatement().executeQuery("SELECT COUNT(*) FROM PUBLIC.rr_governance_policy_version WHERE id='policy-v1'")) {
                existing.next();
                assertThat(existing.getLong(1)).isOne();
            }
            try (var connection = DriverManager.getConnection(url, "sa", "");
                 var result = connection.createStatement().executeQuery("SELECT COUNT(*) FROM PUBLIC.rr_award_cycle")) {
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
