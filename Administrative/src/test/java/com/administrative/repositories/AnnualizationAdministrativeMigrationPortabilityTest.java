package com.administrative.repositories;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnualizationAdministrativeMigrationPortabilityTest {
    @Test
    void benefitProfilesUseDistinctAutomaticMigrationsForBothProviders() throws IOException {
        Path root = Path.of("src", "main", "resources", "db", "administrative-migration");
        Path sqlServer = root.resolve(Path.of("sqlserver", "automatic", "V1007__benefit_tax_profiles.sql"));
        Path postgresql = root.resolve(Path.of("postgresql", "automatic", "V1007__benefit_tax_profiles.sql"));
        assertTrue(Files.isRegularFile(sqlServer));
        assertTrue(Files.isRegularFile(postgresql));
        assertTrue(Files.readString(sqlServer).contains("COL_LENGTH"));
        assertTrue(Files.readString(postgresql).contains("ADD COLUMN IF NOT EXISTS"));
        assertFalse(Files.readString(sqlServer).contains("ON CONFLICT"));
        assertFalse(Files.readString(postgresql).contains("COL_LENGTH"));
    }
    @Test
    void birEmploymentStatusMigrationIsPackagedForBothDatabaseProviders() throws IOException {
        Path sqlServer = Path.of("src", "main", "resources", "db", "administrative-migration",
                "sqlserver", "automatic", "V1005__nature_of_appointment_bir_status.sql");
        Path postgresql = Path.of("src", "main", "resources", "db", "administrative-migration",
                "postgresql", "automatic", "V1005__nature_of_appointment_bir_status.sql");

        assertTrue(Files.isRegularFile(sqlServer));
        assertTrue(Files.isRegularFile(postgresql));
        assertTrue(Files.readString(sqlServer).contains("COL_LENGTH"));
        assertFalse(Files.readString(sqlServer).contains("ADD COLUMN IF NOT EXISTS"));
        assertTrue(Files.readString(postgresql).contains("ADD COLUMN IF NOT EXISTS"));
        assertFalse(Files.readString(postgresql).contains("COL_LENGTH"));
    }
}
