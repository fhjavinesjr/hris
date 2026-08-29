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
    void flywayV1ThroughV10CreateTablesForeignKeysAndIndexesBeforeHibernateValidation() throws Exception {
        assertThat(flyway.info().current()).isNotNull();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("10");

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            assertThat(readNames(metadata.getTables(connection.getCatalog(), databaseSchema, "%", new String[]{"TABLE"}),
                    "TABLE_NAME")).containsAll(EXPECTED_TABLES);
            assertThat(indexNames(metadata, connection)).containsAll(EXPECTED_INDEXES)
                    .containsAll(PHASE_1B_INDEXES).containsAll(PHASE_1C_INDEXES).containsAll(PHASE_2_INDEXES)
                    .containsAll(PHASE_3_1_INDEXES).containsAll(PHASE_3_2_INDEXES);
            assertThat(indexNames(metadata, connection)).containsAll(PHASE_3_3_INDEXES)
                    .containsAll(PHASE_4_1_INDEXES).containsAll(PHASE_4_2_INDEXES);

            assertThat(importedKeyCount(metadata, connection, "prime_proficiency_level")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "prime_competency")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "prime_behavioral_indicator")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "prime_position_profile_requirement"))
                    .isGreaterThanOrEqualTo(3);
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
