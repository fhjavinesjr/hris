package com.primehr.migration;

import com.primehr.competency.api.AdminCategoryResponse;
import com.primehr.competency.api.AdminCompetencyResponse;
import com.primehr.competency.api.AdminLevelResponse;
import com.primehr.competency.api.AdminScaleResponse;
import com.primehr.competency.api.DraftCategoryRequest;
import com.primehr.competency.api.DraftCompetencyRequest;
import com.primehr.competency.api.DraftIndicatorRequest;
import com.primehr.competency.api.DraftLevelRequest;
import com.primehr.competency.api.DraftScaleRequest;
import com.primehr.competency.api.PublishDefinitionRequest;
import com.primehr.competency.application.CompetencyAdminService;
import com.primehr.competency.domain.BehavioralIndicator;
import com.primehr.competency.domain.Competency;
import com.primehr.competency.domain.CompetencyCategory;
import com.primehr.competency.domain.ProficiencyLevel;
import com.primehr.competency.domain.ProficiencyScale;
import com.primehr.competency.infrastructure.BehavioralIndicatorRepository;
import com.primehr.competency.infrastructure.CompetencyCategoryRepository;
import com.primehr.competency.infrastructure.CompetencyRepository;
import com.primehr.competency.infrastructure.CompetencySpecifications;
import com.primehr.competency.infrastructure.ProficiencyScaleRepository;
import com.primehr.positionprofile.domain.PositionProfile;
import com.primehr.positionprofile.domain.PositionTargetSnapshot;
import com.primehr.positionprofile.domain.PositionTargetType;
import com.primehr.positionprofile.infrastructure.PositionProfileRepository;
import com.primehr.positionprofile.infrastructure.PositionProfileSpecifications;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

abstract class AbstractPrimeHrProviderIntegration {

    private static final Set<String> EXPECTED_TABLES = Set.of(
            "prime_competency_category", "prime_proficiency_scale", "prime_proficiency_level",
            "prime_competency", "prime_behavioral_indicator", "prime_audit_event",
            "prime_position_profile", "prime_position_profile_requirement",
            "prime_assessment_cycle", "prime_assessment_tool", "prime_assessment_tool_method",
            "prime_assessment_case", "prime_assessor_assignment", "prime_assessment_rating",
            "prime_assessment_evidence", "prime_assessment_validation",
            "prime_assessment_validated_rating", "prime_person_competency_profile",
            "prime_person_competency_result", "prime_gap_priority_scheme", "prime_gap_priority_level",
            "prime_gap_priority_rule", "prime_competency_gap_analysis", "prime_competency_gap_item",
            "prime_ld_referral", "prime_ld_referral_item",
            "rsp_recruitment_plan", "rsp_vacancy_request", "rsp_vacancy_publication",
            "rsp_vacancy_publication_channel", "rsp_vacancy_publication_requirement",
            "rsp_applicant_account", "rsp_privacy_notice", "rsp_applicant_consent",
            "rsp_applicant_profile", "rsp_applicant_profile_entry", "rsp_applicant_document",
            "rsp_position_application", "rsp_application_document_snapshot", "rsp_applicant_communication",
            "rsp_screening_policy", "rsp_screening_policy_criterion", "rsp_screening_reason_code",
            "rsp_publication_screening_policy", "rsp_screening_case", "rsp_screening_assignment",
            "rsp_screening_finding", "rsp_screening_evidence_link", "rsp_screening_decision",
            "rsp_evaluation_policy", "rsp_evaluation_policy_stage", "rsp_evaluation_policy_criterion",
            "rsp_publication_evaluation_policy", "prime_committee", "prime_committee_member",
            "rsp_evaluation_proceeding", "rsp_evaluation_candidate", "rsp_evaluation_session",
            "rsp_evaluation_session_candidate", "rsp_evaluation_assignment", "rsp_conflict_declaration",
            "rsp_stage_result", "rsp_panel_rating", "rsp_panel_rating_item", "rsp_reference_check",
            "rsp_evaluation_evidence", "rsp_hrmpsb_meeting", "rsp_hrmpsb_attendance",
            "rsp_hrmpsb_resolution", "rsp_comparative_evaluation", "rsp_comparative_evaluation_item",
            "rsp_selection_case", "rsp_selection_candidate_decision", "rsp_selection_notice", "rsp_offer_response",
            "rsp_appointment_handoff", "rsp_appointment_handoff_attempt",
            "spms_policy", "spms_policy_version", "spms_cycle", "spms_cycle_milestone",
            "spms_pmt", "spms_pmt_member", "spms_rating_scale", "spms_rating_scale_version", "spms_rating_band",
            "spms_success_indicator", "spms_success_indicator_version", "spms_indicator_dimension", "spms_indicator_dimension_level",
            "spms_template", "spms_template_version", "spms_template_section", "spms_template_item",
            "spms_objective", "spms_objective_version", "spms_plan_assignment", "spms_plan_assignment_objective",
            "spms_commitment", "spms_commitment_version", "spms_commitment_section", "spms_commitment_item", "spms_commitment_cascade",
            "spms_commitment_route", "spms_commitment_route_step", "spms_commitment_action",
            "flyway_schema_history");
    private static final Set<String> EXPECTED_INDEXES = Set.of(
            "ix_prime_category_agency_active", "ix_prime_scale_agency_active",
            "ix_prime_level_agency_scale", "ix_prime_competency_filter", "ix_prime_indicator_lookup");
    private static final Set<String> PHASE_1B_INDEXES = Set.of(
            "ix_prime_audit_aggregate", "ix_prime_audit_actor_time");
    private static final Set<String> PHASE_1C_INDEXES = Set.of(
            "ix_prime_category_publication_chain", "ix_prime_scale_publication_chain",
            "ix_prime_competency_publication_chain");
    private static final Set<String> PHASE_2_INDEXES = Set.of(
            "ix_prime_profile_filter", "ix_prime_profile_target_chain",
            "ix_prime_profile_requirement_order", "ix_prime_profile_effective_resolution");
    private static final Set<String> PHASE_3_1_INDEXES = Set.of(
            "ix_prime_assessment_cycle_filter", "ix_prime_assessment_tool_cycle",
            "ix_prime_assessment_case_subject", "ix_prime_assessment_case_tool",
            "ix_prime_assessor_employee");
    private static final Set<String> PHASE_3_2_INDEXES = Set.of(
            "ix_prime_assessment_assignment_inbox", "ix_prime_assessment_rating_assignment",
            "ix_prime_assessment_evidence_rating");
    private static final Set<String> PHASE_3_3_INDEXES = Set.of(
            "ix_prime_validation_status", "ix_prime_person_profile_latest",
            "ix_prime_person_result_profile");
    private static final Set<String> PHASE_4_1_INDEXES = Set.of(
            "ix_prime_gap_scheme_effective", "ix_prime_gap_level_scheme", "ix_prime_gap_rule_scheme",
            "ix_prime_gap_analysis_employee", "ix_prime_gap_analysis_profiles", "ix_prime_gap_item_filter");
    private static final Set<String> PHASE_4_2_INDEXES = Set.of(
            "ix_prime_ld_referral_employee", "ix_prime_ld_referral_analysis", "ix_prime_ld_referral_item_gap");
    private static final Set<String> PHASE_5A_1_INDEXES = Set.of(
            "ix_rsp_plan_period", "ix_rsp_vacancy_plantilla", "ix_rsp_vacancy_plan");
    private static final Set<String> PHASE_5A_2_INDEXES = Set.of(
            "ix_rsp_publication_status", "ix_rsp_publication_plantilla",
            "ix_rsp_publication_channel", "ix_rsp_publication_requirement");
    private static final Set<String> PHASE_5B_1_INDEXES = Set.of(
            "ix_rsp_privacy_effective", "ix_rsp_consent_applicant",
            "ix_rsp_profile_entry", "ix_rsp_document_owner");
    private static final Set<String> PHASE_5B_2_INDEXES = Set.of(
            "uk_rsp_application_acknowledgment", "ix_rsp_application_owner", "ix_rsp_application_queue",
            "ix_rsp_application_vacancy", "ix_rsp_appdoc_application",
            "ix_rsp_communication_application", "ix_rsp_communication_applicant");
    private static final Set<String> PHASE_5C_1_INDEXES = Set.of(
            "ix_rsp_screening_policy_lookup", "ix_rsp_screening_criterion_policy",
            "ix_rsp_screening_reason_policy", "ix_rsp_publication_screening_lookup");
    private static final Set<String> PHASE_5C_2_INDEXES = Set.of(
            "uk_rsp_screening_case_current", "ix_rsp_screening_case_queue",
            "ix_rsp_screening_case_publication", "ix_rsp_screening_assignment_queue",
            "ix_rsp_screening_finding_case", "ix_rsp_screening_evidence_case",
            "ix_rsp_screening_decision_outcome");
    private static final Set<String> PHASE_5D_1_INDEXES = Set.of(
            "ix_rsp_evaluation_policy_lookup", "ix_rsp_evaluation_stage_policy",
            "ix_rsp_evaluation_criterion_stage", "ix_rsp_publication_evaluation_lookup",
            "ix_prime_committee_lookup", "ix_prime_committee_member_employee",
            "ix_rsp_evaluation_proceeding_queue", "ix_rsp_evaluation_proceeding_committee",
            "ix_rsp_evaluation_candidate_application", "ix_rsp_evaluation_candidate_proceeding");
    private static final Set<String> PHASE_5D_2_INDEXES = Set.of(
            "ix_rsp_eval_session_queue", "ix_rsp_eval_sc_candidate",
            "ix_rsp_eval_assignment_employee", "ix_rsp_conflict_actor", "ix_rsp_stage_result_queue",
            "ix_rsp_panel_rating_queue", "ix_rsp_evidence_owner");
    private static final Set<String> PHASE_5E_1_INDEXES = Set.of(
            "uk_rsp_selection_current", "ix_rsp_selection_source", "uk_rsp_selection_one_selected",
            "ix_rsp_selection_candidate_applicant", "uk_rsp_selection_notice_current", "uk_rsp_offer_idempotency");
    private static final Set<String> PHASE_6A_INDEXES = Set.of(
            "ix_spms_policy_effective", "ix_spms_cycle_period", "ix_spms_milestone_cycle",
            "ix_spms_pmt_effective", "ix_spms_pmt_member_effective");
    private static final Set<String> PHASE_6B_1_INDEXES = Set.of(
            "ix_spms_rating_scale_effective", "ix_spms_rating_band_version");
    private static final Set<String> PHASE_6B_2_INDEXES = Set.of(
            "ix_spms_success_indicator_status", "ix_spms_indicator_dimension", "ix_spms_indicator_level");
    private static final Set<String> PHASE_6B_3_INDEXES = Set.of("ix_spms_template_status","ix_spms_template_section","ix_spms_template_item");
    private static final Set<String> PHASE_6C_1_INDEXES = Set.of("ix_spms_objective_status","ix_spms_objective_parent","ix_spms_assignment_cycle","ix_spms_assignment_owner","ix_spms_assignment_objective");
    private static final Set<String> PHASE_6C_2_INDEXES = Set.of("ix_spms_commitment_assignment","ix_spms_commitment_status","ix_spms_commitment_owner","ix_spms_commitment_section","ix_spms_commitment_item","ix_spms_cascade_upstream","ix_spms_cascade_downstream");
    private static final Set<String> PHASE_6C_3_INDEXES = Set.of("ix_spms_commitment_route_current","ix_spms_commitment_route_actor","ix_spms_commitment_action_history");

    @Autowired private Flyway flyway;
    @Autowired private DataSource dataSource;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private CompetencyCategoryRepository categoryRepository;
    @Autowired private ProficiencyScaleRepository scaleRepository;
    @Autowired private CompetencyRepository competencyRepository;
    @Autowired private BehavioralIndicatorRepository indicatorRepository;
    @Autowired private CompetencyAdminService adminService;
    @Autowired private PositionProfileRepository positionProfileRepository;
    @Value("${spring.flyway.default-schema}") private String databaseSchema;

    @Test
    void flywayV1ThroughV30CreatesTablesForeignKeysAndIndexesBeforeHibernateValidation() throws Exception {
        assertThat(flyway.info().current()).isNotNull();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("30");

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            assertThat(readNames(metadata.getTables(connection.getCatalog(), databaseSchema, "%", new String[]{"TABLE"}),
                    "TABLE_NAME")).containsAll(EXPECTED_TABLES);
            assertThat(indexNames(metadata, connection)).containsAll(EXPECTED_INDEXES)
                    .containsAll(PHASE_1B_INDEXES).containsAll(PHASE_1C_INDEXES).containsAll(PHASE_2_INDEXES)
                    .containsAll(PHASE_3_1_INDEXES).containsAll(PHASE_3_2_INDEXES);
            assertThat(indexNames(metadata, connection)).containsAll(PHASE_3_3_INDEXES)
                    .containsAll(PHASE_4_1_INDEXES).containsAll(PHASE_4_2_INDEXES)
                    .containsAll(PHASE_5A_1_INDEXES).containsAll(PHASE_5A_2_INDEXES);
            assertThat(indexNames(metadata, connection)).containsAll(PHASE_5B_1_INDEXES);
            assertThat(indexNames(metadata, connection)).containsAll(PHASE_5B_2_INDEXES);
            assertThat(indexNames(metadata, connection)).containsAll(PHASE_5C_1_INDEXES);
            assertThat(indexNames(metadata, connection)).containsAll(PHASE_5C_2_INDEXES);
            assertThat(indexNames(metadata, connection)).containsAll(PHASE_5D_1_INDEXES);
            assertThat(indexNames(metadata, connection)).containsAll(PHASE_5D_2_INDEXES);
            assertThat(indexNames(metadata, connection)).containsAll(PHASE_5E_1_INDEXES);
            assertThat(indexNames(metadata, connection)).containsAll(PHASE_6A_INDEXES);
            assertThat(indexNames(metadata, connection)).containsAll(PHASE_6B_1_INDEXES);
            assertThat(indexNames(metadata, connection)).containsAll(PHASE_6B_2_INDEXES);
            assertThat(indexNames(metadata, connection)).containsAll(PHASE_6B_3_INDEXES);
            assertThat(indexNames(metadata, connection)).containsAll(PHASE_6C_1_INDEXES);
            assertThat(indexNames(metadata, connection)).containsAll(PHASE_6C_2_INDEXES);
            assertThat(indexNames(metadata, connection)).containsAll(PHASE_6C_3_INDEXES);

            assertThat(importedKeyCount(metadata, connection, "prime_proficiency_level")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "prime_competency")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "prime_behavioral_indicator")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "prime_position_profile_requirement"))
                    .isGreaterThanOrEqualTo(3);
            assertThat(importedKeyCount(metadata, connection, "spms_policy_version")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "spms_cycle")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "spms_cycle_milestone")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "spms_pmt_member")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "spms_rating_scale_version")).isGreaterThanOrEqualTo(3);
            assertThat(importedKeyCount(metadata, connection, "spms_rating_band")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "spms_success_indicator_version")).isGreaterThanOrEqualTo(4);
            assertThat(importedKeyCount(metadata, connection, "spms_indicator_dimension")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "spms_indicator_dimension_level")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "spms_template_version")).isGreaterThanOrEqualTo(4);
            assertThat(importedKeyCount(metadata, connection, "spms_template_section")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "spms_template_item")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "spms_objective_version")).isGreaterThanOrEqualTo(5);
            assertThat(importedKeyCount(metadata, connection, "spms_plan_assignment")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "spms_plan_assignment_objective")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "prime_assessment_tool")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "prime_assessment_case")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "prime_assessor_assignment")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "prime_assessment_rating")).isGreaterThanOrEqualTo(3);
            assertThat(importedKeyCount(metadata, connection, "prime_assessment_evidence")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "prime_assessment_validation")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "prime_assessment_validated_rating")).isGreaterThanOrEqualTo(3);
            assertThat(importedKeyCount(metadata, connection, "prime_person_competency_profile")).isGreaterThanOrEqualTo(3);
            assertThat(importedKeyCount(metadata, connection, "prime_person_competency_result")).isGreaterThanOrEqualTo(4);
            assertThat(importedKeyCount(metadata, connection, "prime_gap_priority_level")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "prime_gap_priority_rule")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "prime_competency_gap_analysis")).isGreaterThanOrEqualTo(3);
            assertThat(importedKeyCount(metadata, connection, "prime_competency_gap_item")).isGreaterThanOrEqualTo(8);
            assertThat(importedKeyCount(metadata, connection, "prime_ld_referral")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "prime_ld_referral_item")).isGreaterThanOrEqualTo(3);
            assertThat(importedKeyCount(metadata, connection, "rsp_vacancy_request")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "rsp_vacancy_publication")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "rsp_vacancy_publication_channel")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "rsp_vacancy_publication_requirement")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "rsp_applicant_consent")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "rsp_applicant_profile")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "rsp_applicant_profile_entry")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "rsp_applicant_document")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "rsp_position_application")).isGreaterThanOrEqualTo(3);
            assertThat(importedKeyCount(metadata, connection, "rsp_application_document_snapshot")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "rsp_applicant_communication")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "rsp_screening_policy")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "rsp_screening_policy_criterion")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "rsp_screening_reason_code")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "rsp_publication_screening_policy")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "rsp_screening_case")).isGreaterThanOrEqualTo(4);
            assertThat(importedKeyCount(metadata, connection, "rsp_screening_assignment")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "rsp_screening_finding")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "rsp_screening_evidence_link")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "rsp_screening_decision")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "rsp_evaluation_policy_stage")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "rsp_evaluation_policy_criterion")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "rsp_publication_evaluation_policy")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "prime_committee_member")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "rsp_evaluation_proceeding")).isGreaterThanOrEqualTo(3);
            assertThat(importedKeyCount(metadata, connection, "rsp_evaluation_candidate")).isGreaterThanOrEqualTo(3);
        }
    }

    @Test
    @Transactional
    void positionProfileDraftPersistsAgainstTheRealProviderWithoutCrossDomainTables() {
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        PositionProfile profile = positionProfileRepository.saveAndFlush(PositionProfile.draft(
                "PROVIDER-AGENCY", new PositionTargetSnapshot(PositionTargetType.JOB_POSITION, 1400L,
                        1400L, "Provider Position " + suffix, 15L, 1L, null, null,
                        "provider-fingerprint-" + suffix, Instant.now()),
                "Provider Profile " + suffix, null, LocalDate.of(2028, 1, 1), null));

        assertThat(positionProfileRepository.findByIdAndAgencyId(profile.getId(), "PROVIDER-AGENCY"))
                .get().satisfies(saved -> {
                    assertThat(saved.getTargetKey()).isEqualTo("JOB_POSITION:1400");
                    assertThat(saved.getDefinitionVersion()).isEqualTo(1);
                    assertThat(saved.isDraft()).isTrue();
                });
    }

    @Test
    @Transactional
    void positionProfileApprovalMetadataAndEffectiveSpecificationWorkAgainstTheRealProvider() {
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        PositionTargetSnapshot target = new PositionTargetSnapshot(PositionTargetType.JOB_POSITION, 2400L,
                2400L, "Approved Provider Position " + suffix, 16L, 2L, null, null,
                "provider-approved-fingerprint-" + suffix, Instant.now());
        PositionProfile profile = positionProfileRepository.saveAndFlush(PositionProfile.draft(
                "PROVIDER-AGENCY", target, "Approved Provider Profile " + suffix, null,
                LocalDate.of(2028, 2, 1), null));
        profile.submit("provider-submitter", Instant.now(), target);
        profile = positionProfileRepository.saveAndFlush(profile);
        profile.approve("provider-approver", Instant.now(), target);
        profile = positionProfileRepository.saveAndFlush(profile);

        assertThat(profile.getSubmittedBy()).isEqualTo("provider-submitter");
        assertThat(profile.getSubmittedAt()).isNotNull();
        assertThat(profile.getApprovedBy()).isEqualTo("provider-approver");
        assertThat(profile.getApprovedAt()).isNotNull();
        assertThat(positionProfileRepository.findAll(PositionProfileSpecifications.effective(
                                "PROVIDER-AGENCY", PositionTargetType.JOB_POSITION,
                                2400L, null, LocalDate.of(2028, 2, 1))))
                .extracting(PositionProfile::getId).containsExactly(profile.getId());
    }

    @Test
    @Transactional
    void repositoryReadsRespectAgencyEffectivityAndIndicatorOrder() {
        Fixture active = fixture("AGENCY-A", "CORE-A", "COMM", "Communication", true,
                LocalDate.of(2026, 1, 1), null);
        fixture("AGENCY-A", "CORE-OLD", "OLD", "Expired", true,
                LocalDate.of(2020, 1, 1), LocalDate.of(2025, 12, 31));
        fixture("AGENCY-B", "CORE-B", "COMM", "Other Agency Communication", true, null, null);

        var page = competencyRepository.findAll(CompetencySpecifications.competencyFilter(
                        "AGENCY-A", null, true, null, LocalDate.of(2026, 8, 3)),
                PageRequest.of(0, 20, Sort.by("displayOrder")));
        assertThat(page.getContent()).extracting(Competency::getCode).containsExactly("COMM");

        ProficiencyLevel first = active.scale().getLevels().get(0);
        ProficiencyLevel second = active.scale().getLevels().get(1);
        indicatorRepository.save(new BehavioralIndicator("AGENCY-A", active.competency(), second,
                "Second level", null, true, 1, null, null));
        indicatorRepository.save(new BehavioralIndicator("AGENCY-A", active.competency(), first,
                "First level second", null, true, 2, null, null));
        indicatorRepository.save(new BehavioralIndicator("AGENCY-A", active.competency(), first,
                "First level first", null, true, 1, null, null));
        indicatorRepository.flush();

        List<BehavioralIndicator> indicators = indicatorRepository
                .findByCompetencyIdAndAgencyIdOrderByProficiencyLevelLevelOrderAscDisplayOrderAsc(
                        active.competency().getId(), "AGENCY-A");
        assertThat(indicators).extracting(BehavioralIndicator::getBehaviorDescription)
                .containsExactly("First level first", "First level second", "Second level");
    }

    @Test
    @Transactional
    void uniqueConstraintRejectsSameCompetencyCodeWithinAgencyButAllowsAnotherAgency() {
        fixture("AGENCY-A", "CORE-A", "COMM", "Communication", true, null, null);
        fixture("AGENCY-B", "CORE-B", "COMM", "Other Agency", true, null, null);

        assertThatThrownBy(() -> fixture("AGENCY-A", "CORE-C", "COMM", "Duplicate", true, null, null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void controlledPublicationPersistsCompleteAggregateAndServerActorAudit() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("provider-publisher", null, List.of()));
        try {
            String code = "PUB" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            AdminCategoryResponse draft = adminService.createCategory("PROVIDER-AGENCY",
                    new DraftCategoryRequest(code, "Provider publication", null, 1,
                            LocalDate.of(2028, 1, 1), null, null), "provider-create");
            AdminCategoryResponse published = adminService.publishCategory("PROVIDER-AGENCY", draft.id(),
                    new PublishDefinitionRequest(draft.recordVersion(), "Provider publication gate"),
                    "provider-publish");

            AdminScaleResponse scale = adminService.createScale("PROVIDER-AGENCY",
                    new DraftScaleRequest("S" + code, "Provider scale", null, 1,
                            LocalDate.of(2028, 1, 1), null, null), "provider-scale-create");
            AdminLevelResponse level = adminService.createLevel("PROVIDER-AGENCY", scale.id(),
                    new DraftLevelRequest("L1", "Level 1", 1, null,
                            LocalDate.of(2028, 1, 1), null, null), "provider-level-create");
            scale = adminService.listScales("PROVIDER-AGENCY", com.primehr.competency.domain.DefinitionStatus.DRAFT,
                    scale.code(), null, 0, 20).content().get(0);
            scale = adminService.publishScale("PROVIDER-AGENCY", scale.id(),
                    new PublishDefinitionRequest(scale.recordVersion(), "Provider scale publication gate"),
                    "provider-scale-publish");

            AdminCompetencyResponse competency = adminService.createCompetency("PROVIDER-AGENCY",
                    new DraftCompetencyRequest("K" + code, "Provider competency", "Provider definition",
                            published.id(), scale.id(), 1, LocalDate.of(2028, 1, 1), null, null),
                    "provider-competency-create");
            adminService.createIndicator("PROVIDER-AGENCY", competency.id(),
                    new DraftIndicatorRequest(level.id(), "Provider behavior", null, 1,
                            LocalDate.of(2028, 1, 1), null, null), "provider-indicator-create");
            competency = adminService.listCompetencies("PROVIDER-AGENCY",
                    com.primehr.competency.domain.DefinitionStatus.DRAFT, null, competency.code(),
                    null, 0, 20).content().get(0);
            competency = adminService.publishCompetency("PROVIDER-AGENCY", competency.id(),
                    new PublishDefinitionRequest(competency.recordVersion(),
                            "Provider competency publication gate"), "provider-competency-publish");

            assertThat(published.status()).isEqualTo("ACTIVE");
            assertThat(scale.status()).isEqualTo("ACTIVE");
            assertThat(competency.status()).isEqualTo("ACTIVE");
            assertThat(published.publishedAt()).isNotNull();
            assertThat(published.publishedBy()).isEqualTo("provider-publisher");
            assertThat(categoryRepository.findByIdAndAgencyId(published.id(), "PROVIDER-AGENCY").orElseThrow())
                    .satisfies(stored -> {
                        assertThat(stored.getPublishedAt()).isNotNull();
                        assertThat(stored.getPublishedBy()).isEqualTo("provider-publisher");
                    });
            assertThat(adminService.listAuditEvents("PROVIDER-AGENCY", "COMPETENCY_CATEGORY",
                            published.id(), 0, 20).content())
                    .filteredOn(event -> event.action().equals("PUBLISH_DRAFT"))
                    .singleElement()
                    .satisfies(event -> {
                        assertThat(event.actor()).isEqualTo("provider-publisher");
                        assertThat(event.reason()).isEqualTo("Provider publication gate");
                    });
            assertThat(adminService.listAuditEvents("PROVIDER-AGENCY", "COMPETENCY",
                            competency.id(), 0, 20).content())
                    .filteredOn(event -> event.action().equals("PUBLISH_DRAFT"))
                    .singleElement()
                    .satisfies(event -> {
                        assertThat(event.actor()).isEqualTo("provider-publisher");
                        assertThat(event.reason()).isEqualTo("Provider competency publication gate");
                    });
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @ParameterizedTest
    @EnumSource(InvalidCategoryCase.class)
    @Transactional
    void databaseCheckConstraintsRejectInvalidCategoryRows(InvalidCategoryCase invalidCase) {
        LocalDate effectiveFrom = invalidCase == InvalidCategoryCase.REVERSED_EFFECTIVITY
                ? LocalDate.of(2026, 2, 1) : LocalDate.of(2026, 1, 1);
        LocalDate effectiveTo = invalidCase == InvalidCategoryCase.REVERSED_EFFECTIVITY
                ? LocalDate.of(2026, 1, 1) : null;
        String code = invalidCase == InvalidCategoryCase.LOWERCASE_CODE ? "lower" : "VALID";
        int displayOrder = invalidCase == InvalidCategoryCase.NEGATIVE_DISPLAY_ORDER ? -1 : 0;
        Timestamp now = Timestamp.from(Instant.now());

        assertThat(databaseSchema).matches("[A-Za-z0-9_]+");
        String sql = """
                        INSERT INTO %s.prime_competency_category
                        (id, agency_id, code, name, active, display_order, effective_from, effective_to,
                         record_version, created_by, created_at, updated_by, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """.formatted(databaseSchema);
        assertThatThrownBy(() -> jdbcTemplate.update(sql,
                UUID.randomUUID().toString(), "AGENCY-A", code, "Invalid", true, displayOrder,
                Date.valueOf(effectiveFrom), effectiveTo == null ? null : Date.valueOf(effectiveTo),
                0L, "integration-test", now, "integration-test", now))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Fixture fixture(String agency, String categoryCode, String competencyCode, String name,
                            boolean active, LocalDate from, LocalDate to) {
        CompetencyCategory category = categoryRepository.saveAndFlush(new CompetencyCategory(
                agency, categoryCode, categoryCode, null, true, 1, null, null));
        ProficiencyScale scale = new ProficiencyScale(agency, "S-" + categoryCode, "Scale", null,
                true, 1, null, null);
        scale.addLevel(new ProficiencyLevel(agency, "L1", "Level 1", 1, null, true, null, null));
        scale.addLevel(new ProficiencyLevel(agency, "L2", "Level 2", 2, null, true, null, null));
        scale = scaleRepository.saveAndFlush(scale);
        Competency competency = competencyRepository.saveAndFlush(new Competency(agency, competencyCode, name,
                "Definition for " + name, "ACTIVE", category, scale, active, 1, from, to));
        return new Fixture(scale, competency);
    }

    private static Set<String> readNames(ResultSet resultSet, String column) throws Exception {
        Set<String> names = new HashSet<>();
        try (resultSet) {
            while (resultSet.next()) {
                String name = resultSet.getString(column);
                if (name != null) {
                    names.add(name.toLowerCase(Locale.ROOT));
                }
            }
        }
        return names;
    }

    private int importedKeyCount(DatabaseMetaData metadata, Connection connection, String table)
            throws Exception {
        for (String candidate : List.of(table, table.toUpperCase(Locale.ROOT), table.toLowerCase(Locale.ROOT))) {
            try (ResultSet resultSet = metadata.getImportedKeys(connection.getCatalog(), databaseSchema, candidate)) {
                int count = 0;
                while (resultSet.next()) {
                    count++;
                }
                if (count > 0) {
                    return count;
                }
            }
        }
        return 0;
    }

    private Set<String> indexNames(DatabaseMetaData metadata, Connection connection) throws Exception {
        Set<String> names = new HashSet<>();
        for (String table : EXPECTED_TABLES) {
            if ("flyway_schema_history".equals(table)) {
                continue;
            }
            for (String candidate : List.of(table, table.toUpperCase(Locale.ROOT), table.toLowerCase(Locale.ROOT))) {
                names.addAll(readNames(metadata.getIndexInfo(connection.getCatalog(), databaseSchema, candidate,
                        false, false), "INDEX_NAME"));
            }
        }
        return names;
    }

    enum InvalidCategoryCase {
        LOWERCASE_CODE,
        NEGATIVE_DISPLAY_ORDER,
        REVERSED_EFFECTIVITY
    }

    private record Fixture(ProficiencyScale scale, Competency competency) {
    }
}
