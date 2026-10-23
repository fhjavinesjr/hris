package com.hris.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HrisAutomaticMigrationTest {

    @Test
    void upgradesAnExistingHrisDatabaseAndIsSafeToRunAgain() throws Exception {
        String databaseName = "hris_migration_" + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:h2:mem:" + databaseName
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";

        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE wtaxcontribution (id BIGINT PRIMARY KEY)");
            statement.execute("CREATE TABLE earning_type (earningTypeId BIGINT PRIMARY KEY)");
            statement.execute("CREATE TABLE payroll_detail_earning (id BIGINT PRIMARY KEY)");
            statement.execute("CREATE TABLE payroll_adjustment_line (id BIGINT PRIMARY KEY)");
            statement.execute("CREATE TABLE settings (settingsId BIGINT PRIMARY KEY)");
            statement.execute("CREATE TABLE personaldata (personalDataId BIGINT PRIMARY KEY)");
            statement.execute("CREATE TABLE employee (employeeId BIGINT PRIMARY KEY)");
        }

        Flyway administrativeFlyway = Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/administrative-migration/postgresql/automatic")
                .table("hris_administrative_schema_history")
                .baselineOnMigrate(true)
                .baselineVersion(MigrationVersion.fromVersion("1000"))
                .cleanDisabled(true)
                .load();

        Flyway payrollFlyway = Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/payroll-migration/postgresql/automatic")
                .table("hris_payroll_schema_history")
                .baselineOnMigrate(true)
                .baselineVersion(MigrationVersion.fromVersion("1100"))
                .cleanDisabled(true)
                .load();

        Flyway humanResourceFlyway = Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/human-resource-migration/postgresql/automatic")
                .table("hris_human_resource_schema_history")
                .baselineOnMigrate(true)
                .baselineVersion(MigrationVersion.fromVersion("1200"))
                .cleanDisabled(true)
                .load();

        assertEquals(4, administrativeFlyway.migrate().migrationsExecuted);
        assertEquals(11, payrollFlyway.migrate().migrationsExecuted);
        assertEquals(2, humanResourceFlyway.migrate().migrationsExecuted);
        assertEquals(0, administrativeFlyway.migrate().migrationsExecuted);
        assertEquals(0, payrollFlyway.migrate().migrationsExecuted);
        assertEquals(0, humanResourceFlyway.migrate().migrationsExecuted);

        try (var connection = DriverManager.getConnection(url, "sa", "")) {
            assertEquals(1, count(connection,
                    "SELECT COUNT(*) FROM information_schema.columns "
                            + "WHERE lower(table_name)='wtaxcontribution' AND lower(column_name)='taxyear'"));
            assertEquals(1, count(connection,
                    "SELECT COUNT(*) FROM information_schema.columns "
                            + "WHERE lower(table_name)='earning_type' AND lower(column_name)='bir2316itemno'"));
            assertEquals(1, count(connection,
                    "SELECT COUNT(*) FROM information_schema.columns "
                            + "WHERE lower(table_name)='payroll_detail_earning' AND lower(column_name)='bir2316itemno'"));
            assertEquals(1, count(connection,
                    "SELECT COUNT(*) FROM information_schema.columns "
                            + "WHERE lower(table_name)='payroll_adjustment_line' AND lower(column_name)='bir2316itemno'"));
            assertEquals(1, count(connection,
                    "SELECT COUNT(*) FROM information_schema.tables "
                            + "WHERE lower(table_name)='previous_employer_tax_data'"));
            assertEquals(1, count(connection,
                    "SELECT COUNT(*) FROM information_schema.tables "
                            + "WHERE lower(table_name)='annualization_snapshot'"));
            assertEquals(1, count(connection,
                    "SELECT COUNT(*) FROM information_schema.tables "
                            + "WHERE lower(table_name)='annualization_snapshot_item'"));
            assertEquals(1, count(connection,
                    "SELECT COUNT(*) FROM information_schema.columns "
                            + "WHERE lower(table_name)='annualization_snapshot' "
                            + "AND lower(column_name)='employeebirthdate'"));
            assertEquals(1, count(connection,
                    "SELECT COUNT(*) FROM information_schema.columns "
                            + "WHERE lower(table_name)='settings' AND lower(column_name)='rdocode'"));
            assertEquals(1, count(connection,
                    "SELECT COUNT(*) FROM information_schema.columns "
                            + "WHERE lower(table_name)='personaldata' AND lower(column_name)='rdocode'"));
            assertEquals(1, count(connection,
                    "SELECT COUNT(*) FROM information_schema.tables "
                            + "WHERE lower(table_name)='minimum_wage_rate'"));
            assertEquals(1, count(connection,
                    "SELECT COUNT(*) FROM information_schema.tables "
                            + "WHERE lower(table_name)='employee_mwe_status'"));
            assertEquals(1, count(connection,
                    "SELECT COUNT(*) FROM information_schema.columns "
                            + "WHERE lower(table_name)='annualization_snapshot_period' "
                            + "AND lower(column_name)='minimumwageearner'"));
            assertEquals(1, count(connection,
                    "SELECT COUNT(*) FROM information_schema.columns "
                            + "WHERE lower(table_name)='annualization_snapshot_period' "
                            + "AND lower(column_name)='minimumwagedailyrate'"));
            assertEquals(1, count(connection,
                    "SELECT COUNT(*) FROM information_schema.tables "
                            + "WHERE lower(table_name)='annual_pera_tax_credit'"));
            assertEquals(1, count(connection,
                    "SELECT COUNT(*) FROM information_schema.columns "
                            + "WHERE lower(table_name)='annualization_snapshot' "
                            + "AND lower(column_name)='perataxcredit'"));
            assertEquals(1, count(connection,
                    "SELECT COUNT(*) FROM information_schema.columns "
                            + "WHERE lower(table_name)='annualization_snapshot' "
                            + "AND lower(column_name)='substitutedfilingqualified'"));
        }
    }

    private int count(java.sql.Connection connection, String sql) throws Exception {
        try (var statement = connection.createStatement();
             var result = statement.executeQuery(sql)) {
            result.next();
            return result.getInt(1);
        }
    }
}
