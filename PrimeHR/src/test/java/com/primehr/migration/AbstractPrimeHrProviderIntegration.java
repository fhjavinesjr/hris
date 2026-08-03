package com.primehr.migration;

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
            "prime_competency", "prime_behavioral_indicator", "prime_audit_event", "flyway_schema_history");
    private static final Set<String> EXPECTED_INDEXES = Set.of(
            "ix_prime_category_agency_active", "ix_prime_scale_agency_active",
            "ix_prime_level_agency_scale", "ix_prime_competency_filter", "ix_prime_indicator_lookup");
    private static final Set<String> PHASE_1B_INDEXES = Set.of(
            "ix_prime_audit_aggregate", "ix_prime_audit_actor_time");

    @Autowired private Flyway flyway;
    @Autowired private DataSource dataSource;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private CompetencyCategoryRepository categoryRepository;
    @Autowired private ProficiencyScaleRepository scaleRepository;
    @Autowired private CompetencyRepository competencyRepository;
    @Autowired private BehavioralIndicatorRepository indicatorRepository;
    @Value("${spring.flyway.default-schema}") private String databaseSchema;

    @Test
    void flywayV1AndV2CreateTablesForeignKeysAndIndexesBeforeHibernateValidation() throws Exception {
        assertThat(flyway.info().current()).isNotNull();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("2");

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            assertThat(readNames(metadata.getTables(connection.getCatalog(), databaseSchema, "%", new String[]{"TABLE"}),
                    "TABLE_NAME")).containsAll(EXPECTED_TABLES);
            assertThat(indexNames(metadata, connection)).containsAll(EXPECTED_INDEXES)
                    .containsAll(PHASE_1B_INDEXES);

            assertThat(importedKeyCount(metadata, connection, "prime_proficiency_level")).isGreaterThanOrEqualTo(1);
            assertThat(importedKeyCount(metadata, connection, "prime_competency")).isGreaterThanOrEqualTo(2);
            assertThat(importedKeyCount(metadata, connection, "prime_behavioral_indicator")).isGreaterThanOrEqualTo(2);
        }
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
