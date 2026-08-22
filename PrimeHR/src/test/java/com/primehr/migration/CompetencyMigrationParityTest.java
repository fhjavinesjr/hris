package com.primehr.migration;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class CompetencyMigrationParityTest {

    private static final String POSTGRES = "db/migration/postgresql/V1__competency_foundation.sql";
    private static final String SQL_SERVER = "db/migration/sqlserver/V1__competency_foundation.sql";
    private static final String POSTGRES_V2 = "db/migration/postgresql/V2__competency_draft_administration.sql";
    private static final String SQL_SERVER_V2 = "db/migration/sqlserver/V2__competency_draft_administration.sql";
    private static final String POSTGRES_V3 = "db/migration/postgresql/V3__competency_controlled_publishing.sql";
    private static final String SQL_SERVER_V3 = "db/migration/sqlserver/V3__competency_controlled_publishing.sql";
    private static final String POSTGRES_V4 = "db/migration/postgresql/V4__position_competency_profiles.sql";
    private static final String SQL_SERVER_V4 = "db/migration/sqlserver/V4__position_competency_profiles.sql";
    private static final String POSTGRES_V5 = "db/migration/postgresql/V5__position_profile_approval_lifecycle.sql";
    private static final String SQL_SERVER_V5 = "db/migration/sqlserver/V5__position_profile_approval_lifecycle.sql";
    private static final Set<String> TABLES = Set.of(
            "prime_competency_category", "prime_proficiency_scale", "prime_proficiency_level",
            "prime_competency", "prime_behavioral_indicator");

    @Test
    void providerMigrationsContainTheSameTablesAndPortableLogicalConstraints() throws IOException {
        String postgres = read(POSTGRES);
        String sqlServer = read(SQL_SERVER);

        assertThat(tableNames(postgres)).isEqualTo(TABLES);
        assertThat(tableNames(sqlServer)).isEqualTo(TABLES);
        assertThat(postgres).contains("\"${primehrSchema}\".");
        assertThat(sqlServer).contains("[${primehrSchema}].");
        assertThat(sqlServer).contains("COLLATE Latin1_General_100_BIN2 = UPPER(code) COLLATE Latin1_General_100_BIN2");
        for (String constraint : Set.of("uk_prime_category_agency_code", "uk_prime_scale_agency_code",
                "uk_prime_level_scale_code", "uk_prime_level_scale_order",
                "uk_prime_competency_agency_code", "uk_prime_indicator_order")) {
            assertThat(postgres).contains(constraint);
            assertThat(sqlServer).contains(constraint);
        }
    }

    @Test
    void migrationsContainNoProductionSeedOrCrossDomainReferences() throws IOException {
        for (String migration : Set.of(read(POSTGRES), read(SQL_SERVER))) {
            assertThat(migration.toLowerCase())
                    .doesNotContain("insert into")
                    .doesNotContain("employeeappointment")
                    .doesNotContain("system_config")
                    .doesNotContain("payroll_detail")
                    .doesNotContain("dtrdaily");
        }
    }

    @Test
    void phase1BDraftMigrationsHaveEquivalentLifecycleAuditAndLineageObjects() throws IOException {
        String postgres = read(POSTGRES_V2);
        String sqlServer = read(SQL_SERVER_V2);
        for (String required : Set.of("definition_version", "supersedes_id", "prime_audit_event",
                "uk_prime_category_agency_code_version", "uk_prime_scale_agency_code_version",
                "uk_prime_competency_agency_code_version", "ck_prime_category_status",
                "ck_prime_scale_status", "ck_prime_competency_status", "ix_prime_audit_aggregate")) {
            assertThat(postgres).contains(required);
            assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table");
        assertThat(sqlServer.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table");
    }

    @Test
    void phase1CPublishingMigrationsHaveEquivalentMetadataAndIndexes() throws IOException {
        String postgres = read(POSTGRES_V3);
        String sqlServer = read(SQL_SERVER_V3);
        for (String required : Set.of("published_at", "published_by",
                "ix_prime_category_publication_chain", "ix_prime_scale_publication_chain",
                "ix_prime_competency_publication_chain")) {
            assertThat(postgres).contains(required);
            assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table");
        assertThat(sqlServer.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table");
    }

    @Test
    void phase2PositionProfileMigrationsHaveEquivalentTablesConstraintsAndIndexes() throws IOException {
        String postgres = read(POSTGRES_V4);
        String sqlServer = read(SQL_SERVER_V4);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder(
                "prime_position_profile", "prime_position_profile_requirement");
        assertThat(tableNames(sqlServer)).containsExactlyInAnyOrder(
                "prime_position_profile", "prime_position_profile_requirement");
        for (String required : Set.of("uk_prime_profile_target_version",
                "uk_prime_profile_requirement_competency", "fk_prime_profile_requirement_competency",
                "fk_prime_profile_requirement_level", "ix_prime_profile_filter",
                "ix_prime_profile_target_chain", "ix_prime_profile_requirement_order")) {
            assertThat(postgres).contains(required);
            assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table");
        assertThat(sqlServer.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table");
    }

    @Test
    void phase2ApprovalMigrationsHaveEquivalentMetadataConstraintAndResolutionIndex() throws IOException {
        String postgres = read(POSTGRES_V5);
        String sqlServer = read(SQL_SERVER_V5);
        for (String required : Set.of("submitted_by", "submitted_at", "approved_by", "approved_at",
                "ck_prime_profile_lifecycle_metadata", "ix_prime_profile_effective_resolution")) {
            assertThat(postgres).contains(required);
            assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table");
        assertThat(sqlServer.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table");
    }

    private static Set<String> tableNames(String sql) {
        Matcher matcher = Pattern.compile(
                "(?i)CREATE\\s+TABLE\\s+(?:(?:\"\\$\\{primehrSchema}\"|\\[\\$\\{primehrSchema}])\\.)?([a-z0-9_]+)")
                .matcher(sql);
        java.util.HashSet<String> names = new java.util.HashSet<>();
        while (matcher.find()) {
            names.add(matcher.group(1).toLowerCase());
        }
        return names;
    }

    private static String read(String path) throws IOException {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    }
}
