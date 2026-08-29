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
    private static final String POSTGRES_V6 = "db/migration/postgresql/V6__assessment_draft_foundation.sql";
    private static final String SQL_SERVER_V6 = "db/migration/sqlserver/V6__assessment_draft_foundation.sql";
    private static final String POSTGRES_V7 = "db/migration/postgresql/V7__assessment_execution.sql";
    private static final String SQL_SERVER_V7 = "db/migration/sqlserver/V7__assessment_execution.sql";
    private static final String POSTGRES_V8 = "db/migration/postgresql/V8__assessment_validation_person_profiles.sql";
    private static final String SQL_SERVER_V8 = "db/migration/sqlserver/V8__assessment_validation_person_profiles.sql";
    private static final String POSTGRES_V9 = "db/migration/postgresql/V9__competency_gap_analysis.sql";
    private static final String SQL_SERVER_V9 = "db/migration/sqlserver/V9__competency_gap_analysis.sql";
    private static final String POSTGRES_V10 = "db/migration/postgresql/V10__manual_ld_referrals.sql";
    private static final String SQL_SERVER_V10 = "db/migration/sqlserver/V10__manual_ld_referrals.sql";
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

    @Test
    void phase3DraftMigrationsHaveEquivalentTablesConstraintsAndIndexes() throws IOException {
        String postgres = read(POSTGRES_V6);
        String sqlServer = read(SQL_SERVER_V6);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder(
                "prime_assessment_cycle", "prime_assessment_tool", "prime_assessment_tool_method",
                "prime_assessment_case", "prime_assessor_assignment");
        assertThat(tableNames(sqlServer)).isEqualTo(tableNames(postgres));
        for (String required : Set.of("uk_prime_assessment_cycle_code", "uk_prime_assessment_tool_name",
                "uk_prime_assessment_tool_method", "uk_prime_assessment_case_subject",
                "uk_prime_assessor_assignment", "ix_prime_assessment_cycle_filter",
                "ix_prime_assessment_tool_cycle", "ix_prime_assessment_case_subject",
                "ix_prime_assessment_case_tool", "ix_prime_assessor_employee")) {
            assertThat(postgres).contains(required);
            assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table")
                .doesNotContain("employeeappointment");
        assertThat(sqlServer.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table")
                .doesNotContain("employeeappointment");
    }

    @Test
    void phase3ExecutionMigrationsHaveEquivalentTablesMetadataAndIndexes() throws IOException {
        String postgres = read(POSTGRES_V7);
        String sqlServer = read(SQL_SERVER_V7);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder(
                "prime_assessment_rating", "prime_assessment_evidence");
        assertThat(tableNames(sqlServer)).isEqualTo(tableNames(postgres));
        for (String required : Set.of("opened_by", "opened_at", "closed_by", "closed_at",
                "published_by", "published_at", "for_validation_at", "submitted_by", "submitted_at",
                "uk_prime_assessment_rating", "ix_prime_assessment_assignment_inbox",
                "ix_prime_assessment_rating_assignment", "ix_prime_assessment_evidence_rating")) {
            assertThat(postgres).contains(required);
            assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table");
        assertThat(sqlServer.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table");
    }

    @Test
    void phase3ValidationMigrationsHaveEquivalentImmutableResultTablesConstraintsAndIndexes() throws IOException {
        String postgres = read(POSTGRES_V8);
        String sqlServer = read(SQL_SERVER_V8);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder(
                "prime_assessment_validation", "prime_assessment_validated_rating",
                "prime_person_competency_profile", "prime_person_competency_result");
        assertThat(tableNames(sqlServer)).isEqualTo(tableNames(postgres));
        for (String required : Set.of("uk_prime_assessment_validation_case", "uk_prime_validated_rating",
                "uk_prime_person_profile_case", "uk_prime_person_profile_validation",
                "uk_prime_person_profile_version", "uk_prime_person_result",
                "uk_prime_person_result_validated", "ck_prime_validation_override_reason",
                "ck_prime_person_profile_status", "ck_prime_person_profile_dates",
                "ix_prime_validation_status", "ix_prime_person_profile_latest",
                "ix_prime_person_result_profile")) {
            assertThat(postgres).contains(required);
            assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table")
                .doesNotContain("employeeappointment");
        assertThat(sqlServer.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table")
                .doesNotContain("employeeappointment");
    }

    @Test
    void phase4GapMigrationsHaveEquivalentTablesConstraintsAndIndexes() throws IOException {
        String postgres = read(POSTGRES_V9);
        String sqlServer = read(SQL_SERVER_V9);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder(
                "prime_gap_priority_scheme", "prime_gap_priority_level", "prime_gap_priority_rule",
                "prime_competency_gap_analysis", "prime_competency_gap_item");
        assertThat(tableNames(sqlServer)).isEqualTo(tableNames(postgres));
        for (String required : Set.of("uk_prime_gap_scheme_version", "uk_prime_gap_level_code",
                "uk_prime_gap_level_rank", "uk_prime_gap_rule_order", "uk_prime_gap_analysis_request",
                "uk_prime_gap_analysis_source", "uk_prime_gap_item_competency",
                "ck_prime_gap_rule_not_assessed", "ck_prime_gap_item_values",
                "ck_prime_gap_item_formula", "ck_prime_gap_item_not_assessed_reason",
                "ix_prime_gap_scheme_effective", "ix_prime_gap_analysis_employee",
                "ix_prime_gap_analysis_profiles", "ix_prime_gap_item_filter")) {
            assertThat(postgres).contains(required);
            assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table")
                .doesNotContain("employeeappointment").doesNotContain(" limit ").doesNotContain("::");
        assertThat(sqlServer.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table")
                .doesNotContain("employeeappointment").doesNotContain(" top ").doesNotContain("isnull(");
    }

    @Test
    void phase4ReferralMigrationsHaveEquivalentTablesConstraintsIndexesAndNoDownstreamSideEffects() throws IOException {
        String postgres=read(POSTGRES_V10); String sqlServer=read(SQL_SERVER_V10);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder("prime_ld_referral","prime_ld_referral_item");
        assertThat(tableNames(sqlServer)).isEqualTo(tableNames(postgres));
        for(String required:Set.of("fk_prime_ld_referral_analysis","fk_prime_ld_referral_item_referral",
                "fk_prime_ld_referral_item_analysis","fk_prime_ld_referral_item_gap",
                "uk_prime_ld_referral_item","ck_prime_ld_referral_status","ck_prime_ld_referral_submission",
                "ck_prime_ld_referral_item_class","ck_prime_ld_referral_item_values",
                "ix_prime_ld_referral_employee","ix_prime_ld_referral_analysis","ix_prime_ld_referral_item_gap")) {
            assertThat(postgres).contains(required); assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("insert into").doesNotContain("delete from")
                .doesNotContain("drop table").doesNotContain("employeeappointment").doesNotContain("training")
                .doesNotContain("enrollment").doesNotContain("payroll").doesNotContain(" limit ").doesNotContain("::");
        assertThat(sqlServer.toLowerCase()).doesNotContain("insert into").doesNotContain("delete from")
                .doesNotContain("drop table").doesNotContain("employeeappointment").doesNotContain("training")
                .doesNotContain("enrollment").doesNotContain("payroll").doesNotContain(" top ").doesNotContain("isnull(");
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
