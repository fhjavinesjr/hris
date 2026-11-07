package com.primehr.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PrimeHrV54ToV55UpgradeTest {
    @Test
    void populatedAssessmentCaseSurvivesReadinessSnapshotMigration() throws Exception {
        String database = "primehr_v55_" + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:h2:mem:" + database + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Map<String, String> placeholders = Map.of("primehrSchema", "PUBLIC");
        Flyway initial = flyway(url, placeholders, "54");
        initial.migrate();
        try {
            try (var connection = DriverManager.getConnection(url, "sa", "");
                 var statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO PUBLIC.evidence_framework"
                        + "(id,agency_id,code,normalized_code,record_version,created_by,created_at,updated_by,updated_at) "
                        + "VALUES('framework','A','ESG','ESG',0,'test',CURRENT_TIMESTAMP,'test',CURRENT_TIMESTAMP)");
                statement.executeUpdate("INSERT INTO PUBLIC.evidence_framework_version"
                        + "(id,agency_id,framework_id,definition_version,content_revision,title,issuing_body,legal_basis,authority_reference,internal_use_disclaimer,status,effective_from,published_by,published_at,record_version,created_by,created_at,updated_by,updated_at) "
                        + "VALUES('version','A','framework',1,1,'Framework','CSC','Law','Ref','Internal','PUBLISHED',CURRENT_DATE,'test',CURRENT_TIMESTAMP,0,'test',CURRENT_TIMESTAMP,'test',CURRENT_TIMESTAMP)");
                statement.executeUpdate("INSERT INTO PUBLIC.evidence_assessment_cycle"
                        + "(id,agency_id,code,normalized_code,title,framework_version_id,framework_definition_version,framework_content_revision,framework_fingerprint,period_start,period_end,due_date,status,record_version,created_by,created_at,updated_by,updated_at) "
                        + "VALUES('cycle','A','C','C','Cycle','version',1,1,'0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef',CURRENT_DATE,CURRENT_DATE,CURRENT_DATE,'OPEN',0,'test',CURRENT_TIMESTAMP,'test',CURRENT_TIMESTAMP)");
                statement.executeUpdate("INSERT INTO PUBLIC.evidence_assessment_case"
                        + "(id,agency_id,cycle_id,framework_version_id,code,normalized_code,title,organization_type,organization_id,organization_name,organization_fingerprint,owner_employee_id,owner_employee_no,owner_name,owner_appointment_id,owner_fingerprint,validator_employee_id,validator_employee_no,validator_name,validator_appointment_id,validator_fingerprint,validator_recused,status,content_revision,submission_revision,record_version,created_by,created_at,updated_by,updated_at) "
                        + "VALUES('case','A','cycle','version','CASE','CASE','Case','AREA',1,'Area','0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef',1,'OWNER','Owner',1,'1123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef',2,'VALIDATOR','Validator',2,'2123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef',FALSE,'DRAFT',0,0,0,'test',CURRENT_TIMESTAMP,'test',CURRENT_TIMESTAMP)");
            }
            Flyway upgraded = flyway(url, placeholders, "55");
            upgraded.migrate();
            assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("55");
            try (var connection = DriverManager.getConnection(url, "sa", "");
                 var result = connection.createStatement().executeQuery(
                         "SELECT COUNT(*) FROM PUBLIC.evidence_assessment_case WHERE id='case'")) {
                result.next();
                assertThat(result.getLong(1)).isOne();
            }
            try (var connection = DriverManager.getConnection(url, "sa", "");
                 var result = connection.createStatement().executeQuery(
                         "SELECT COUNT(*) FROM PUBLIC.evidence_readiness_snapshot")) {
                result.next();
                assertThat(result.getLong(1)).isZero();
            }
        } finally { initial.clean(); }
    }

    private Flyway flyway(String url, Map<String, String> placeholders, String target) {
        return Flyway.configure().dataSource(url, "sa", "")
                .locations("classpath:db/migration/postgresql")
                .schemas("PUBLIC").defaultSchema("PUBLIC").placeholders(placeholders)
                .target(MigrationVersion.fromVersion(target)).cleanDisabled(false).load();
    }
}
