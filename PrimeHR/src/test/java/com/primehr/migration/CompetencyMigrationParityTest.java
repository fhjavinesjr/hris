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
    private static final String POSTGRES_V11 = "db/migration/postgresql/V11__rsp_recruitment_planning_foundation.sql";
    private static final String SQL_SERVER_V11 = "db/migration/sqlserver/V11__rsp_recruitment_planning_foundation.sql";
    private static final String POSTGRES_V12 = "db/migration/postgresql/V12__rsp_authority_and_publication.sql";
    private static final String SQL_SERVER_V12 = "db/migration/sqlserver/V12__rsp_authority_and_publication.sql";
    private static final String POSTGRES_V13 = "db/migration/postgresql/V13__rsp_applicant_foundation.sql";
    private static final String SQL_SERVER_V13 = "db/migration/sqlserver/V13__rsp_applicant_foundation.sql";
    private static final String POSTGRES_V14 = "db/migration/postgresql/V14__rsp_application_intake.sql";
    private static final String SQL_SERVER_V14 = "db/migration/sqlserver/V14__rsp_application_intake.sql";
    private static final String POSTGRES_V15 = "db/migration/postgresql/V15__rsp_screening_policy_foundation.sql";
    private static final String SQL_SERVER_V15 = "db/migration/sqlserver/V15__rsp_screening_policy_foundation.sql";
    private static final String POSTGRES_V16 = "db/migration/postgresql/V16__rsp_application_screening.sql";
    private static final String SQL_SERVER_V16 = "db/migration/sqlserver/V16__rsp_application_screening.sql";
    private static final String POSTGRES_V17 = "db/migration/postgresql/V17__rsp_application_screening_status_integrity.sql";
    private static final String SQL_SERVER_V17 = "db/migration/sqlserver/V17__rsp_application_screening_status_integrity.sql";
    private static final String POSTGRES_V18 = "db/migration/postgresql/V18__rsp_evaluation_governance_foundation.sql";
    private static final String SQL_SERVER_V18 = "db/migration/sqlserver/V18__rsp_evaluation_governance_foundation.sql";
    private static final String POSTGRES_V19 = "db/migration/postgresql/V19__rsp_evaluation_execution_deliberation.sql";
    private static final String SQL_SERVER_V19 = "db/migration/sqlserver/V19__rsp_evaluation_execution_deliberation.sql";
    private static final String POSTGRES_V20 = "db/migration/postgresql/V20__rsp_current_proceeding_null_portability.sql";
    private static final String SQL_SERVER_V20 = "db/migration/sqlserver/V20__rsp_current_proceeding_null_portability.sql";
    private static final String POSTGRES_V21 = "db/migration/postgresql/V21__rsp_selection_decision_offer.sql";
    private static final String SQL_SERVER_V21 = "db/migration/sqlserver/V21__rsp_selection_decision_offer.sql";
    private static final String POSTGRES_V21_1 = "db/migration/postgresql/V21_1__rsp_selection_instant_portability.sql";
    private static final String SQL_SERVER_V21_1 = "db/migration/sqlserver/V21_1__rsp_selection_instant_portability.sql";
    private static final String POSTGRES_V22 = "db/migration/postgresql/V22__rsp_appointment_handoff.sql";
    private static final String SQL_SERVER_V22 = "db/migration/sqlserver/V22__rsp_appointment_handoff.sql";
    private static final String POSTGRES_V23 = "db/migration/postgresql/V23__spms_policy_cycle_calendar.sql";
    private static final String SQL_SERVER_V23 = "db/migration/sqlserver/V23__spms_policy_cycle_calendar.sql";
    private static final String POSTGRES_V24 = "db/migration/postgresql/V24__spms_pmt_governance.sql";
    private static final String SQL_SERVER_V24 = "db/migration/sqlserver/V24__spms_pmt_governance.sql";
    private static final String POSTGRES_V25 = "db/migration/postgresql/V25__spms_rating_scale.sql";
    private static final String SQL_SERVER_V25 = "db/migration/sqlserver/V25__spms_rating_scale.sql";
    private static final String POSTGRES_V26 = "db/migration/postgresql/V26__spms_success_indicator.sql";
    private static final String SQL_SERVER_V26 = "db/migration/sqlserver/V26__spms_success_indicator.sql";
    private static final String POSTGRES_V27 = "db/migration/postgresql/V27__spms_performance_template.sql";
    private static final String SQL_SERVER_V27 = "db/migration/sqlserver/V27__spms_performance_template.sql";
    private static final String POSTGRES_V28 = "db/migration/postgresql/V28__spms_performance_planning_foundation.sql";
    private static final String SQL_SERVER_V28 = "db/migration/sqlserver/V28__spms_performance_planning_foundation.sql";
    private static final String POSTGRES_V29 = "db/migration/postgresql/V29__spms_commitment_composition_cascading.sql";
    private static final String SQL_SERVER_V29 = "db/migration/sqlserver/V29__spms_commitment_composition_cascading.sql";
    private static final String POSTGRES_V30 = "db/migration/postgresql/V30__spms_commitment_approval_workflow.sql";
    private static final String SQL_SERVER_V30 = "db/migration/sqlserver/V30__spms_commitment_approval_workflow.sql";
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

    @Test
    void phase5aPlanningMigrationsHaveEquivalentTablesConstraintsIndexesAndRemainProviderNeutral() throws IOException {
        String postgres = read(POSTGRES_V11);
        String sqlServer = read(SQL_SERVER_V11);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder(
                "rsp_recruitment_plan", "rsp_vacancy_request");
        assertThat(tableNames(sqlServer)).isEqualTo(tableNames(postgres));
        for (String required : Set.of("uk_rsp_plan_code", "uk_rsp_vacancy_plan_plantilla",
                "fk_rsp_vacancy_plan", "fk_rsp_vacancy_profile", "ck_rsp_plan_dates",
                "ck_rsp_plan_status", "ck_rsp_vacancy_status", "ck_rsp_vacancy_type",
                "ck_rsp_vacancy_anticipated", "ck_rsp_vacancy_occupant", "ck_rsp_vacancy_versions",
                "ix_rsp_plan_period", "ix_rsp_vacancy_plantilla", "ix_rsp_vacancy_plan")) {
            assertThat(postgres).contains(required);
            assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table")
                .doesNotContain("employeeappointment").doesNotContain(" limit ").doesNotContain("::");
        assertThat(sqlServer.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table")
                .doesNotContain("employeeappointment").doesNotContain(" top ").doesNotContain("isnull(");
    }

    @Test
    void phase5aAuthorityAndPublicationMigrationsAreEquivalentAndProviderNeutral() throws IOException {
        String postgres = read(POSTGRES_V12);
        String sqlServer = read(SQL_SERVER_V12);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder(
                "rsp_vacancy_publication", "rsp_vacancy_publication_channel",
                "rsp_vacancy_publication_requirement");
        assertThat(tableNames(sqlServer)).isEqualTo(tableNames(postgres));
        for (String required : Set.of("fk_rsp_publication_vacancy", "uk_rsp_publication_vacancy",
                "uk_rsp_publication_channel", "uk_rsp_publication_requirement",
                "ck_rsp_publication_status", "ck_rsp_publication_visibility",
                "ck_rsp_publication_dates", "ck_rsp_publication_versions",
                "ix_rsp_publication_status", "ix_rsp_publication_plantilla",
                "ix_rsp_publication_channel", "ix_rsp_publication_requirement")) {
            assertThat(postgres).contains(required);
            assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table")
                .doesNotContain("employeeappointment").doesNotContain(" limit ").doesNotContain("::");
        assertThat(sqlServer.toLowerCase()).doesNotContain("delete from").doesNotContain("drop table")
                .doesNotContain("employeeappointment").doesNotContain(" top ").doesNotContain("isnull(");
    }

    @Test
    void phase5bApplicantFoundationMigrationsAreEquivalentAndProviderNeutral() throws IOException {
        String postgres = read(POSTGRES_V13);
        String sqlServer = read(SQL_SERVER_V13);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder("rsp_applicant_account", "rsp_privacy_notice",
                "rsp_applicant_consent", "rsp_applicant_profile", "rsp_applicant_profile_entry",
                "rsp_applicant_document");
        assertThat(tableNames(sqlServer)).isEqualTo(tableNames(postgres));
        for (String required : Set.of("uk_rsp_applicant_email", "uk_rsp_privacy_version",
                "uk_rsp_consent_notice", "uk_rsp_applicant_profile", "uk_rsp_profile_entry_order",
                "uk_rsp_document_object", "fk_rsp_consent_applicant", "fk_rsp_consent_notice",
                "fk_rsp_profile_applicant", "fk_rsp_profile_entry", "fk_rsp_document_applicant",
                "ix_rsp_privacy_effective", "ix_rsp_consent_applicant", "ix_rsp_profile_entry",
                "ix_rsp_document_owner")) {
            assertThat(postgres).contains(required);
            assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("insert into").doesNotContain("delete from")
                .doesNotContain("drop table").doesNotContain(" limit ").doesNotContain("::");
        assertThat(sqlServer.toLowerCase()).doesNotContain("insert into").doesNotContain("delete from")
                .doesNotContain("drop table").doesNotContain(" top ").doesNotContain("isnull(");
    }

    @Test
    void phase5bApplicationIntakeMigrationsAreEquivalentAndForwardOnly() throws IOException {
        String postgres = read(POSTGRES_V14);
        String sqlServer = read(SQL_SERVER_V14);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder("rsp_position_application",
                "rsp_application_document_snapshot", "rsp_applicant_communication");
        assertThat(tableNames(sqlServer)).isEqualTo(tableNames(postgres));
        for (String required : Set.of("uk_rsp_application_version", "uk_rsp_application_acknowledgment",
                "uk_rsp_application_document", "fk_rsp_application_applicant",
                "fk_rsp_application_publication", "fk_rsp_application_notice",
                "fk_rsp_appdoc_application", "fk_rsp_appdoc_document",
                "fk_rsp_communication_application", "fk_rsp_communication_applicant",
                "ck_rsp_application_status", "ck_rsp_application_submission",
                "ck_rsp_application_withdrawal", "ix_rsp_application_owner",
                "ix_rsp_application_queue", "ix_rsp_application_vacancy",
                "ix_rsp_appdoc_application", "ix_rsp_communication_application",
                "ix_rsp_communication_applicant")) {
            assertThat(postgres).contains(required);
            assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("insert into").doesNotContain("delete from")
                .doesNotContain("drop table").doesNotContain(" limit ").doesNotContain("::");
        assertThat(sqlServer.toLowerCase()).doesNotContain("insert into").doesNotContain("delete from")
                .doesNotContain("drop table").doesNotContain(" top ").doesNotContain("isnull(");
    }

    @Test
    void phase5cScreeningPolicyMigrationsAreEquivalentProviderNeutralAndForwardOnly() throws IOException {
        String postgres = read(POSTGRES_V15);
        String sqlServer = read(SQL_SERVER_V15);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder("rsp_screening_policy",
                "rsp_screening_policy_criterion", "rsp_screening_reason_code",
                "rsp_publication_screening_policy");
        assertThat(tableNames(sqlServer)).isEqualTo(tableNames(postgres));
        for (String required : Set.of("uk_rsp_screening_policy_version", "uk_rsp_screening_criterion_code",
                "uk_rsp_screening_criterion_order", "uk_rsp_screening_reason_code",
                "uk_rsp_screening_reason_order", "uk_rsp_publication_screening_policy",
                "fk_rsp_screening_policy_prior", "fk_rsp_screening_criterion_policy",
                "fk_rsp_screening_reason_policy", "fk_rsp_publication_screening_vacancy",
                "fk_rsp_publication_screening_policy", "ck_rsp_screening_policy_status",
                "ck_rsp_screening_policy_publish", "ck_rsp_screening_criterion_category",
                "ck_rsp_screening_criterion_mode", "ix_rsp_screening_policy_lookup",
                "ix_rsp_screening_criterion_policy", "ix_rsp_screening_reason_policy",
                "ix_rsp_publication_screening_lookup")) {
            assertThat(postgres).contains(required);
            assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("insert into").doesNotContain("delete from")
                .doesNotContain("drop table").doesNotContain(" limit ").doesNotContain("::");
        assertThat(sqlServer.toLowerCase()).doesNotContain("insert into").doesNotContain("delete from")
                .doesNotContain("drop table").doesNotContain(" top ").doesNotContain("isnull(");
    }

    @Test
    void phase5cApplicationScreeningMigrationsAreEquivalentAndPreserveExistingApplications() throws IOException {
        String postgres = read(POSTGRES_V16);
        String sqlServer = read(SQL_SERVER_V16);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder("rsp_screening_case",
                "rsp_screening_assignment", "rsp_screening_finding", "rsp_screening_evidence_link",
                "rsp_screening_decision");
        assertThat(tableNames(sqlServer)).isEqualTo(tableNames(postgres));
        for (String required : Set.of("uk_rsp_screening_case_revision", "uk_rsp_screening_case_current",
                "uk_rsp_screening_assignment", "uk_rsp_screening_finding", "uk_rsp_screening_evidence",
                "uk_rsp_screening_decision_case", "fk_rsp_screening_case_application",
                "fk_rsp_screening_case_policy", "fk_rsp_screening_assignment_case",
                "fk_rsp_screening_finding_case", "fk_rsp_screening_evidence_finding",
                "fk_rsp_screening_decision_case", "ck_rsp_screening_case_status",
                "ck_rsp_screening_finding_result", "ix_rsp_screening_case_queue",
                "ix_rsp_screening_assignment_queue", "ix_rsp_screening_finding_case",
                "ix_rsp_screening_evidence_case", "ix_rsp_screening_decision_outcome")) {
            assertThat(postgres).contains(required);
            assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("insert into").doesNotContain("delete from")
                .doesNotContain("drop table").doesNotContain(" limit ").doesNotContain("::");
        assertThat(sqlServer.toLowerCase()).doesNotContain("insert into").doesNotContain("delete from")
                .doesNotContain("drop table").doesNotContain(" top ").doesNotContain("isnull(");
    }

    @Test
    void phase5cStatusIntegrityMigrationSupportsEverySubmittedApplicationOutcome() throws IOException {
        String postgres = read(POSTGRES_V17);
        String sqlServer = read(SQL_SERVER_V17);
        for (String status : Set.of("SUBMITTED", "UNDER_SCREENING", "QUALIFIED", "DISQUALIFIED", "WITHDRAWN")) {
            assertThat(postgres).contains(status);
            assertThat(sqlServer).contains(status);
        }
        assertThat(postgres).contains("ck_rsp_application_submission", "acknowledgment_number", "submitted_at");
        assertThat(sqlServer).contains("ck_rsp_application_submission", "acknowledgment_number", "submitted_at");
        assertThat(postgres.toLowerCase()).doesNotContain("insert into", "delete from", " limit ", "::");
        assertThat(sqlServer.toLowerCase()).doesNotContain("insert into", "delete from", " top ", "isnull(");
    }

    @Test
    void phase5dEvaluationGovernanceMigrationsAreEquivalentProviderNeutralAndForwardOnly() throws IOException {
        String postgres=read(POSTGRES_V18); String sqlServer=read(SQL_SERVER_V18);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder("rsp_evaluation_policy",
                "rsp_evaluation_policy_stage","rsp_evaluation_policy_criterion",
                "rsp_publication_evaluation_policy","prime_committee","prime_committee_member",
                "rsp_evaluation_proceeding","rsp_evaluation_candidate");
        assertThat(tableNames(sqlServer)).isEqualTo(tableNames(postgres));
        for(String required:Set.of("uk_rsp_evaluation_policy_version","uk_rsp_evaluation_stage_code",
                "uk_rsp_evaluation_criterion_code","uk_rsp_publication_evaluation_policy",
                "uk_prime_committee_version","uk_prime_committee_member",
                "uk_rsp_evaluation_current_publication","uk_rsp_evaluation_candidate_application",
                "ck_rsp_evaluation_policy_rounding","ck_rsp_evaluation_stage_weight",
                "ck_prime_committee_publish","ck_rsp_evaluation_proceeding_lifecycle",
                "ix_rsp_evaluation_policy_lookup","ix_prime_committee_member_employee",
                "ix_rsp_evaluation_proceeding_queue","ix_rsp_evaluation_candidate_proceeding")){
            assertThat(postgres).contains(required);assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("insert into").doesNotContain("delete from")
                .doesNotContain("drop table").doesNotContain("employeeappointment")
                .doesNotContain(" limit ").doesNotContain("::");
        assertThat(sqlServer.toLowerCase()).doesNotContain("insert into").doesNotContain("delete from")
                .doesNotContain("drop table").doesNotContain("employeeappointment")
                .doesNotContain(" top ").doesNotContain("isnull(");
    }

    @Test
    void phase5dExecutionMigrationsAreEquivalentProviderNeutralAndForwardOnly() throws IOException {
        String postgres=read(POSTGRES_V19);String sqlServer=read(SQL_SERVER_V19);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder("rsp_evaluation_session",
                "rsp_evaluation_session_candidate","rsp_evaluation_assignment","rsp_conflict_declaration",
                "rsp_stage_result","rsp_panel_rating","rsp_panel_rating_item","rsp_reference_check",
                "rsp_evaluation_evidence","rsp_hrmpsb_meeting","rsp_hrmpsb_attendance",
                "rsp_hrmpsb_resolution","rsp_comparative_evaluation","rsp_comparative_evaluation_item");
        assertThat(tableNames(sqlServer)).isEqualTo(tableNames(postgres));
        for(String required:Set.of("uk_rsp_eval_session_revision","uk_rsp_eval_session_candidate",
                "uk_rsp_eval_assignment_active","uk_rsp_conflict_scope","uk_rsp_stage_result_current",
                "uk_rsp_panel_rating_current","uk_rsp_reference_check","uk_rsp_hrmpsb_attendance",
                "uk_rsp_hrmpsb_resolution_candidate","uk_rsp_comparative_final","ix_rsp_eval_session_queue",
                "ix_rsp_stage_result_queue","ix_rsp_panel_rating_queue","ix_rsp_evidence_owner")){
            assertThat(postgres).contains(required);assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("insert into").doesNotContain("delete from")
                .doesNotContain("drop table").doesNotContain("employeeappointment")
                .doesNotContain(" limit ").doesNotContain("::");
        assertThat(sqlServer.toLowerCase()).doesNotContain("insert into").doesNotContain("delete from")
                .doesNotContain("drop table").doesNotContain("employeeappointment")
                .doesNotContain(" top ").doesNotContain("isnull(");
    }

    @Test
    void phase5dCurrentProceedingUniquenessPreservesProviderNullSemantics() throws IOException {
        String postgres = read(POSTGRES_V20).toLowerCase();
        String sqlServer = read(SQL_SERVER_V20).toLowerCase();

        for (String migration : Set.of(postgres, sqlServer)) {
            assertThat(migration).contains("drop constraint uk_rsp_evaluation_current_publication");
            assertThat(migration).contains("create unique index uk_rsp_evaluation_current_publication");
            assertThat(migration).contains("agency_id, current_publication_key");
            assertThat(migration).doesNotContain("insert into", "delete from", "drop table");
        }
        assertThat(postgres).doesNotContain("where current_publication_key is not null");
        assertThat(sqlServer).contains("where current_publication_key is not null");
    }

    @Test
    void phase5eSelectionMigrationsAreEquivalentPortableAndForwardOnly() throws IOException {
        String postgres=read(POSTGRES_V21);String sqlServer=read(SQL_SERVER_V21);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder("rsp_selection_case",
                "rsp_selection_candidate_decision","rsp_selection_notice","rsp_offer_response");
        assertThat(tableNames(sqlServer)).isEqualTo(tableNames(postgres));
        for(String required:Set.of("uk_rsp_selection_revision","uk_rsp_selection_current",
                "uk_rsp_selection_one_selected","uk_rsp_selection_notice_current","uk_rsp_offer_idempotency",
                "ck_rsp_selection_choice","ck_rsp_selection_variance","ck_rsp_selection_candidate_selected",
                "ck_rsp_offer_response","ix_rsp_selection_source","ix_rsp_selection_candidate_applicant")){
            assertThat(postgres).contains(required);assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("insert into","delete from","drop table",
                "employeeappointment"," limit ","::");
        assertThat(sqlServer.toLowerCase()).doesNotContain("insert into","delete from","drop table",
                "employeeappointment"," top ","isnull(");
    }

    @Test
    void phase5eSelectionInstantCorrectionIsForwardOnlyAndProviderSpecific() throws IOException {
        String postgres=read(POSTGRES_V21_1).toLowerCase();String sqlServer=read(SQL_SERVER_V21_1).toLowerCase();
        assertThat(postgres).contains("timestamp with time zone");
        assertThat(sqlServer).contains("alter table","datetimeoffset","offer_response_deadline",
                "released_at","responded_at","created_at","updated_at");
        assertThat(postgres).doesNotContain("drop table","delete from","insert into");
        assertThat(sqlServer).doesNotContain("drop table","delete from","insert into");
    }

    @Test
    void phase5eHandoffMigrationsAreEquivalentPortableAndForwardOnly() throws IOException {
        String postgres=read(POSTGRES_V22);String sqlServer=read(SQL_SERVER_V22);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder("rsp_appointment_handoff","rsp_appointment_handoff_attempt");
        assertThat(tableNames(sqlServer)).isEqualTo(tableNames(postgres));
        for(String required:Set.of("uk_rsp_handoff_revision","uk_rsp_handoff_current","uk_rsp_handoff_attempt",
                "fk_rsp_handoff_selection","fk_rsp_handoff_application","fk_rsp_handoff_attempt_handoff",
                "ck_rsp_handoff_status","ck_rsp_handoff_receipt","ix_rsp_handoff_status",
                "ix_rsp_handoff_application","ix_rsp_handoff_attempt_time")){
            assertThat(postgres).contains(required);assertThat(sqlServer).contains(required);
        }
        assertThat(postgres.toLowerCase()).doesNotContain("insert into","delete from","drop table","employeeappointment"," limit ","::");
        assertThat(sqlServer.toLowerCase()).doesNotContain("insert into","delete from","drop table","employeeappointment"," top ","isnull(");
    }

    @Test
    void phase6aPolicyCycleMigrationsAreEquivalentPortableAndForwardOnly() throws IOException {
        String postgres=read(POSTGRES_V23);String sqlServer=read(SQL_SERVER_V23);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder("spms_policy","spms_policy_version","spms_cycle","spms_cycle_milestone");
        assertThat(tableNames(sqlServer)).isEqualTo(tableNames(postgres));
        assertThat(postgres).contains("uk_spms_policy_code","uk_spms_policy_version","ck_spms_policy_lifecycle","ck_spms_cycle_dates","ck_spms_milestone_type");
        assertThat(sqlServer).contains("uk_spms_policy_code","uk_spms_policy_version","ck_spms_policy_lifecycle","ck_spms_cycle_dates","ck_spms_milestone_type");
        assertThat(postgres.toLowerCase()).doesNotContain("insert into","delete from","drop table","employeeappointment"," limit ","::");
        assertThat(sqlServer.toLowerCase()).doesNotContain("insert into","delete from","drop table","employeeappointment"," top ","isnull(");
    }

    @Test
    void phase6aPmtMigrationsAreEquivalentPortableAndForwardOnly() throws IOException {
        String postgres=read(POSTGRES_V24);String sqlServer=read(SQL_SERVER_V24);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder("spms_pmt","spms_pmt_member");
        assertThat(tableNames(sqlServer)).isEqualTo(tableNames(postgres));
        assertThat(postgres).contains("uk_spms_pmt_code","uk_spms_pmt_member_period","ck_spms_pmt_member_role","ck_spms_pmt_member_vote");
        assertThat(sqlServer).contains("uk_spms_pmt_code","uk_spms_pmt_member_period","ck_spms_pmt_member_role","ck_spms_pmt_member_vote");
        assertThat(postgres.toLowerCase()).doesNotContain("insert into","delete from","drop table","employeeappointment"," limit ","::");
        assertThat(sqlServer.toLowerCase()).doesNotContain("insert into","delete from","drop table","employeeappointment"," top ","isnull(");
    }

    @Test
    void phase6bRatingScaleMigrationsAreEquivalentPortableAndForwardOnly() throws IOException {
        String postgres=read(POSTGRES_V25);String sqlServer=read(SQL_SERVER_V25);
        assertThat(tableNames(postgres)).containsExactlyInAnyOrder("spms_rating_scale","spms_rating_scale_version","spms_rating_band");
        assertThat(tableNames(sqlServer)).isEqualTo(tableNames(postgres));
        assertThat(postgres).contains("uk_spms_rating_scale_code","uk_spms_rating_scale_version","ck_spms_rating_scale_rounding","ck_spms_rating_scale_measure","ck_spms_rating_band_bounds");
        assertThat(sqlServer).contains("uk_spms_rating_scale_code","uk_spms_rating_scale_version","ck_spms_rating_scale_rounding","ck_spms_rating_scale_measure","ck_spms_rating_band_bounds");
        assertThat(postgres.toLowerCase()).doesNotContain("insert into","delete from","drop table","employeeappointment"," limit ","::");
        assertThat(sqlServer.toLowerCase()).doesNotContain("insert into","delete from","drop table","employeeappointment"," top ","isnull(");
    }
    @Test void phase6bSuccessIndicatorMigrationsAreEquivalentPortableAndForwardOnly()throws IOException{String p=read(POSTGRES_V26),s=read(SQL_SERVER_V26);assertThat(tableNames(p)).containsExactlyInAnyOrder("spms_success_indicator","spms_success_indicator_version","spms_indicator_dimension","spms_indicator_dimension_level");assertThat(tableNames(s)).isEqualTo(tableNames(p));assertThat(p).contains("uk_spms_success_indicator_code","ck_spms_dimension_type","ck_spms_level_operator","description","requires_evidence","effective_from","effective_to","dimension_code","uk_spms_indicator_dimension_code");assertThat(s).contains("uk_spms_success_indicator_code","ck_spms_dimension_type","ck_spms_level_operator","description","requires_evidence","effective_from","effective_to","dimension_code","uk_spms_indicator_dimension_code");assertThat(p.toLowerCase()).doesNotContain("insert into","drop table"," limit ","::");assertThat(s.toLowerCase()).doesNotContain("insert into","drop table"," top ","isnull(");}
    @Test void phase6bTemplateMigrationsAreEquivalentPortableAndForwardOnly()throws IOException{String p=read(POSTGRES_V27),s=read(SQL_SERVER_V27);assertThat(tableNames(p)).containsExactlyInAnyOrder("spms_template","spms_template_version","spms_template_section","spms_template_item");assertThat(tableNames(s)).isEqualTo(tableNames(p));assertThat(p).contains("uk_spms_template_code","ck_spms_template_form","ck_spms_template_aggregation","uk_spms_template_item_indicator","form_label","legal_basis","effective_from","section_code","uk_spms_template_section_code","label_override","required_item","evidence_override");assertThat(s).contains("uk_spms_template_code","ck_spms_template_form","ck_spms_template_aggregation","uk_spms_template_item_indicator","form_label","legal_basis","effective_from","section_code","uk_spms_template_section_code","label_override","required_item","evidence_override");assertThat(p.toLowerCase()).doesNotContain("insert into","drop table"," limit ","::");assertThat(s.toLowerCase()).doesNotContain("insert into","drop table"," top ","isnull(");}
    @Test void phase6cPlanningMigrationsAreEquivalentPortableAndForwardOnly()throws IOException{String p=read(POSTGRES_V28),s=read(SQL_SERVER_V28);assertThat(tableNames(p)).containsExactlyInAnyOrder("spms_objective","spms_objective_version","spms_plan_assignment","spms_plan_assignment_objective");assertThat(tableNames(s)).isEqualTo(tableNames(p));for(String required:Set.of("uk_spms_objective_code","uk_spms_objective_version","ck_spms_objective_level","fk_spms_objective_parent","uk_spms_assignment_subject","ck_spms_assignment_subject","uk_spms_assignment_objective","ix_spms_objective_status","ix_spms_assignment_cycle")){assertThat(p).contains(required);assertThat(s).contains(required);}assertThat(p.toLowerCase()).doesNotContain("insert into","drop table"," limit ","::","employeeappointment","manage_personnel");assertThat(s.toLowerCase()).doesNotContain("insert into","drop table"," top ","isnull(","employeeappointment","manage_personnel");}
    @Test void phase6cCommitmentMigrationsAreEquivalentPortableAndForwardOnly()throws IOException{String p=read(POSTGRES_V29),s=read(SQL_SERVER_V29);assertThat(tableNames(p)).containsExactlyInAnyOrder("spms_commitment","spms_commitment_version","spms_commitment_section","spms_commitment_item","spms_commitment_cascade");assertThat(tableNames(s)).isEqualTo(tableNames(p));for(String required:Set.of("uk_spms_commitment_assignment","uk_spms_commitment_version","fk_spms_commitment_current","ck_spms_commitment_status","uk_spms_commitment_item_order","uk_spms_commitment_cascade","ck_spms_cascade_share","ix_spms_commitment_status","ix_spms_cascade_upstream")){assertThat(p).contains(required);assertThat(s).contains(required);}assertThat(p.toLowerCase()).doesNotContain("insert into","delete from","drop table"," limit ","::","employeeappointment","manage_personnel");assertThat(s.toLowerCase()).doesNotContain("insert into","delete from","drop table"," top ","isnull(","employeeappointment","manage_personnel");}
    @Test void phase6cApprovalMigrationsAreEquivalentPortableAndForwardOnly()throws IOException{String p=read(POSTGRES_V30),s=read(SQL_SERVER_V30);assertThat(tableNames(p)).containsExactlyInAnyOrder("spms_commitment_route","spms_commitment_route_step","spms_commitment_action");assertThat(tableNames(s)).isEqualTo(tableNames(p));for(String required:Set.of("uk_spms_commitment_route_revision","uk_spms_commitment_route_step","uk_spms_commitment_action_request","ck_spms_commitment_route_status","ck_spms_commitment_route_step_action","ck_spms_commitment_action_type","ix_spms_commitment_route_actor")){assertThat(p).contains(required);assertThat(s).contains(required);}assertThat(p.toLowerCase()).doesNotContain("insert into","delete from","drop table"," limit ","::","employeeappointment","manage_personnel");assertThat(s.toLowerCase()).doesNotContain("insert into","delete from","drop table"," top ","isnull(","employeeappointment","manage_personnel");}

    private static Set<String> tableNames(String sql) {
        Matcher matcher = Pattern.compile(
                "(?i)CREATE\\s+TABLE\\s+(?:(?:\"\\$\\{primehrSchema}\"|\\[\\$\\{primehrSchema}]|\\$\\{primehrSchema})\\.)?([a-z0-9_]+)")
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
