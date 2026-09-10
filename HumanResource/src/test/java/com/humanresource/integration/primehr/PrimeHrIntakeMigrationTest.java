package com.humanresource.integration.primehr;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrimeHrIntakeMigrationTest {
    @Test
    void pairedProviderMigrationsExposeTheSameReceiptContract() throws Exception {
        Path root = Path.of("src/main/resources/db/migration/primehr-intake");
        String postgres = Files.readString(root.resolve("postgresql/V1__primehr_appointment_handoff_receipt.sql"));
        String sqlServer = Files.readString(root.resolve("sqlserver/V1__primehr_appointment_handoff_receipt.sql"));

        for (String contract : new String[]{"rsp_appointment_handoff_receipt", "source_fingerprint",
                "payload_snapshot", "uk_hrm_rsp_handoff", "uk_hrm_rsp_selection", "ix_hrm_rsp_handoff_application"}) {
            assertTrue(postgres.contains(contract), contract + " missing from PostgreSQL migration");
            assertTrue(sqlServer.contains(contract), contract + " missing from SQL Server migration");
        }
        assertTrue(postgres.contains("TIMESTAMP WITH TIME ZONE"));
        assertTrue(sqlServer.contains("DATETIMEOFFSET"));
    }

    @Test
    void baselineZeroPreservesAPopulatedLegacySchemaAndAppliesReceiptV1() throws Exception {
        String url = "jdbc:h2:mem:hrm_primehr_intake;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        try (var connection = DriverManager.getConnection(url, "sa", ""); var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE legacy_employee_marker(id INTEGER PRIMARY KEY)");
            statement.execute("INSERT INTO legacy_employee_marker(id) VALUES (1)");
        }

        Flyway flyway = Flyway.configure().dataSource(url, "sa", "").baselineOnMigrate(true).baselineVersion("0")
                .locations("classpath:db/migration/primehr-intake/postgresql")
                .target("1")
                .placeholders(java.util.Map.of("hrmSchema", "PUBLIC")).load();
        flyway.migrate();

        try (var connection = DriverManager.getConnection(url, "sa", ""); var statement = connection.createStatement()) {
            assertTrue(statement.executeQuery("SELECT 1 FROM legacy_employee_marker").next());
            assertTrue(statement.executeQuery("SELECT 1 FROM rsp_appointment_handoff_receipt WHERE 1=0") != null);
        }
        assertEquals("1", flyway.info().current().getVersion().getVersion());
    }

    @Test
    void pairedV2MigrationsContainTheOnboardingAtomicityAndDuplicatePreflightContract() throws Exception {
        Path root=Path.of("src/main/resources/db/migration/primehr-intake");
        String pg=Files.readString(root.resolve("postgresql/V2__onboarding_employee_appointment_activation.sql"));
        String sql=Files.readString(root.resolve("sqlserver/V2__onboarding_employee_appointment_activation.sql"));
        for(String contract:new String[]{"hrm_onboarding_template","hrm_onboarding_template_item","hrm_onboarding_case",
                "hrm_onboarding_item","hrm_appointment_provenance","employee_activation_invitation",
                "hrm_onboarding_audit_event","uk_hrm_active_appointment_employee","uk_hrm_active_appointment_plantilla",
                "duplicate active Plantilla occupants"}){assertTrue(pg.contains(contract),contract+" missing from PostgreSQL V2");assertTrue(sql.contains(contract),contract+" missing from SQL Server V2");}
        assertTrue(pg.contains("WHERE activeAppointment=TRUE"));assertTrue(sql.contains("WHERE activeAppointment=1"));
    }

    @Test
    void pairedV3MigrationsContainEquivalentImmutableLegalDocumentContracts() throws Exception {
        Path root=Path.of("src/main/resources/db/migration/primehr-intake");
        String pg=Files.readString(root.resolve("postgresql/V3__appointment_legal_documents.sql"));
        String sql=Files.readString(root.resolve("sqlserver/V3__appointment_legal_documents.sql"));
        for(String contract:new String[]{"hrm_appointment_document","hrm_appointment_document_audit_event",
                "official_template_code","official_template_version","official_template_checksum",
                "source_snapshot","source_fingerprint","administering_employee_id",
                "certifying_employee_id","attesting_employee_id","supersedes_id",
                "OATH_OF_OFFICE","ASSUMPTION_TO_DUTY","FINALIZED","SUPERSEDED",
                "uk_hrm_appointment_document_current"}) {
            assertTrue(pg.contains(contract),contract+" missing from PostgreSQL V3");
            assertTrue(sql.contains(contract),contract+" missing from SQL Server V3");
        }
        assertTrue(pg.contains("TIMESTAMP WITH TIME ZONE"));
        assertTrue(sql.contains("DATETIMEOFFSET"));
        assertTrue(pg.contains("REFERENCES \"${hrmSchema}\".employee(employeeId)"));
        assertTrue(sql.contains("REFERENCES [${hrmSchema}].employee(employeeId)"));
    }
}
