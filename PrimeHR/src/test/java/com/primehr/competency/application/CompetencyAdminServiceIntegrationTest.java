package com.primehr.competency.application;

import com.primehr.competency.api.*;
import com.primehr.competency.domain.*;
import com.primehr.competency.infrastructure.*;
import com.primehr.shared.audit.PrimeHrAuditEventRepository;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import com.primehr.shared.exception.OptimisticConflictException;
import com.primehr.shared.exception.PublicationConflictException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class CompetencyAdminServiceIntegrationTest {
    private static final String AGENCY = "TEST-AGENCY";

    @Autowired private CompetencyAdminService service;
    @Autowired private CompetencyQueryService queries;
    @Autowired private CompetencyCategoryRepository categories;
    @Autowired private ProficiencyScaleRepository scales;
    @Autowired private CompetencyRepository competencies;
    @Autowired private BehavioralIndicatorRepository indicators;
    @Autowired private PrimeHrAuditEventRepository audits;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void draftCommandsUseOptimisticVersionAndWriteExactlyOneActorAuditEach() {
        authenticate("001");
        long before = audits.count();
        String code = code("CAT");

        AdminCategoryResponse created = service.createCategory(AGENCY,
                new DraftCategoryRequest(code, "Draft", null, 1, null, null, null), "corr-1");
        AdminCategoryResponse updated = service.updateCategory(AGENCY, created.id(),
                new DraftCategoryRequest(code, "Updated", null, 2, null, null, created.recordVersion()), "corr-2");

        assertThat(updated.status()).isEqualTo("DRAFT");
        assertThat(updated.definitionVersion()).isEqualTo(1);
        assertThat(audits.count()).isEqualTo(before + 2);
        var history = service.listAuditEvents(AGENCY, "COMPETENCY_CATEGORY", created.id(), 0, 20).content();
        assertThat(history).hasSize(2).allSatisfy(event -> assertThat(event.actor()).isEqualTo("001"));

        long beforeConflict = audits.count();
        assertThatThrownBy(() -> service.updateCategory(AGENCY, created.id(),
                new DraftCategoryRequest(code, "Stale", null, 1, null, null, created.recordVersion()), null))
                .isInstanceOf(OptimisticConflictException.class);
        assertThat(audits.count()).isEqualTo(beforeConflict);
    }

    @Test
    void activeAggregatesAreImmutableAndSuccessorsCloneOwnedChildren() {
        authenticate("admin");
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        CompetencyCategory category = categories.saveAndFlush(new CompetencyCategory(
                AGENCY, "C" + suffix, "Category", null, true, 1, null, null));
        ProficiencyScale scale = new ProficiencyScale(AGENCY, "S" + suffix, "Scale", null,
                true, 1, null, null);
        scale.addLevel(new ProficiencyLevel(AGENCY, "L1", "Level 1", 1, null, true, null, null));
        scale = scales.saveAndFlush(scale);
        Competency competency = competencies.saveAndFlush(new Competency(AGENCY, "K" + suffix,
                "Competency", "Definition", "ACTIVE", category, scale, true, 1, null, null));
        indicators.saveAndFlush(new BehavioralIndicator(AGENCY, competency, scale.getLevels().get(0),
                "Observe behavior", null, true, 1, null, null));

        ProficiencyScale finalScale = scale;
        assertThatThrownBy(() -> service.updateScale(AGENCY, finalScale.getId(),
                new DraftScaleRequest(finalScale.getCode(), "Changed", null, 1, null, null,
                        finalScale.getVersion()), null))
                .isInstanceOf(IllegalLifecycleTransitionException.class);

        AdminScaleResponse scaleDraft = service.versionScale(AGENCY, scale.getId(),
                new DraftTransitionRequest(scale.getVersion(), "Annual revision"), "corr-scale");
        AdminCompetencyResponse competencyDraft = service.versionCompetency(AGENCY, competency.getId(),
                new DraftTransitionRequest(competency.getVersion(), "Annual revision"), "corr-competency");

        assertThat(scaleDraft.status()).isEqualTo("DRAFT");
        assertThat(scaleDraft.definitionVersion()).isEqualTo(2);
        assertThat(scaleDraft.supersedesId()).isEqualTo(scale.getId());
        assertThat(scaleDraft.levels()).hasSize(1);
        assertThat(scaleDraft.levels().get(0).id()).isNotEqualTo(scale.getLevels().get(0).getId());
        assertThat(competencyDraft.status()).isEqualTo("DRAFT");
        assertThat(competencyDraft.definitionVersion()).isEqualTo(2);
        assertThat(competencyDraft.supersedesId()).isEqualTo(competency.getId());
        assertThat(competencyDraft.indicators()).hasSize(1);
    }

    @Test
    void archiveRequiresDraftAndPreservesTheRecord() {
        authenticate("002");
        AdminCategoryResponse created = service.createCategory(AGENCY,
                new DraftCategoryRequest(code("ARC"), "Archive me", null, 1, null, null, null), null);
        AdminCategoryResponse archived = service.archiveCategory(AGENCY, created.id(),
                new DraftTransitionRequest(created.recordVersion(), "Duplicate draft"), null);

        assertThat(archived.status()).isEqualTo("ARCHIVED");
        assertThat(categories.findByIdAndAgencyId(created.id(), AGENCY)).isPresent();
        assertThatThrownBy(() -> service.archiveCategory(AGENCY, created.id(),
                new DraftTransitionRequest(archived.recordVersion(), "Again"), null))
                .isInstanceOf(IllegalLifecycleTransitionException.class);
    }

    @Test
    void completeAggregatesPublishWithAuthenticatedMetadataAndExactAudit() {
        authenticate("publisher-001");
        LocalDate start = LocalDate.of(2027, 1, 1);
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        AdminCategoryResponse category = service.createCategory(AGENCY,
                new DraftCategoryRequest("PC" + suffix, "Published Category", null, 1,
                        start, null, null), null);
        category = service.publishCategory(AGENCY, category.id(),
                new PublishDefinitionRequest(category.recordVersion(), "Initial category publication"), "pub-cat");

        AdminScaleResponse scale = service.createScale(AGENCY,
                new DraftScaleRequest("PS" + suffix, "Published Scale", null, 1,
                        start, null, null), null);
        AdminLevelResponse levelOne = service.createLevel(AGENCY, scale.id(),
                new DraftLevelRequest("L1", "Level 1", 1, null, start, null, null), null);
        AdminLevelResponse levelTwo = service.createLevel(AGENCY, scale.id(),
                new DraftLevelRequest("L2", "Level 2", 2, null, start, null, null), null);
        scale = service.listScales(AGENCY, DefinitionStatus.DRAFT, scale.code(), null, 0, 20).content().get(0);
        scale = service.publishScale(AGENCY, scale.id(),
                new PublishDefinitionRequest(scale.recordVersion(), "Initial scale publication"), "pub-scale");

        AdminCompetencyResponse competency = service.createCompetency(AGENCY,
                new DraftCompetencyRequest("PK" + suffix, "Published Competency", "Definition",
                        category.id(), scale.id(), 1, start, null, null), null);
        service.createIndicator(AGENCY, competency.id(), new DraftIndicatorRequest(levelOne.id(),
                "Level one behavior", null, 1, start, null, null), null);
        service.createIndicator(AGENCY, competency.id(), new DraftIndicatorRequest(levelTwo.id(),
                "Level two behavior", null, 1, start, null, null), null);
        competency = service.listCompetencies(AGENCY, DefinitionStatus.DRAFT, null,
                competency.code(), null, 0, 20).content().get(0);
        competency = service.publishCompetency(AGENCY, competency.id(),
                new PublishDefinitionRequest(competency.recordVersion(), "Initial competency publication"),
                "pub-competency");

        assertThat(category.status()).isEqualTo("ACTIVE");
        assertThat(scale.status()).isEqualTo("ACTIVE");
        assertThat(competency.status()).isEqualTo("ACTIVE");
        assertThat(category.publishedBy()).isEqualTo("publisher-001");
        assertThat(scale.publishedAt()).isNotNull();
        assertThat(competency.publishedAt()).isNotNull();
        assertThat(service.listAuditEvents(AGENCY, "COMPETENCY", competency.id(), 0, 20).content())
                .filteredOn(event -> event.action().equals("PUBLISH_DRAFT"))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.action()).isEqualTo("PUBLISH_DRAFT");
                    assertThat(event.reason()).isEqualTo("Initial competency publication");
                    assertThat(event.actor()).isEqualTo("publisher-001");
                });
    }

    @Test
    void successorPublicationClosesOnlyOverlappingPredecessorAndPreservesAsOfHistory() {
        authenticate("publisher-002");
        String code = code("VER");
        AdminCategoryResponse first = service.createCategory(AGENCY,
                new DraftCategoryRequest(code, "Version One", null, 1,
                        LocalDate.of(2026, 1, 1), null, null), null);
        first = service.publishCategory(AGENCY, first.id(),
                new PublishDefinitionRequest(first.recordVersion(), "Publish version one"), null);
        AdminCategoryResponse successor = service.versionCategory(AGENCY, first.id(),
                new DraftTransitionRequest(first.recordVersion(), "Prepare version two"), null);
        successor = service.updateCategory(AGENCY, successor.id(),
                new DraftCategoryRequest(code, "Version Two", null, 1,
                        LocalDate.of(2027, 1, 1), null, successor.recordVersion()), null);
        successor = service.publishCategory(AGENCY, successor.id(),
                new PublishDefinitionRequest(successor.recordVersion(), "Publish version two"), "pub-v2");

        CompetencyCategory predecessor = categories.findByIdAndAgencyId(first.id(), AGENCY).orElseThrow();
        assertThat(predecessor.getStatus()).isEqualTo(DefinitionStatus.ACTIVE);
        assertThat(predecessor.getEffectiveTo()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(successor.status()).isEqualTo("ACTIVE");
        assertThat(queries.listCategories(AGENCY, true, LocalDate.of(2026, 6, 1)))
                .extracting(CompetencyCategoryResponse::name).contains("Version One").doesNotContain("Version Two");
        assertThat(queries.listCategories(AGENCY, true, LocalDate.of(2027, 6, 1)))
                .extracting(CompetencyCategoryResponse::name).contains("Version Two").doesNotContain("Version One");
        assertThat(service.listAuditEvents(AGENCY, "COMPETENCY_CATEGORY", first.id(), 0, 20).content())
                .filteredOn(event -> event.action().equals("CLOSE_PUBLISHED_EFFECTIVITY"))
                .hasSize(1);
        assertThat(service.listAuditEvents(AGENCY, "COMPETENCY_CATEGORY", successor.id(), 0, 20).content())
                .filteredOn(event -> event.action().equals("PUBLISH_DRAFT"))
                .hasSize(1);
    }

    @Test
    void incompleteAndStalePublicationFailWithoutSuccessAudit() {
        authenticate("publisher-003");
        AdminScaleResponse scale = service.createScale(AGENCY,
                new DraftScaleRequest(code("EMPTY"), "Incomplete", null, 1,
                        LocalDate.of(2027, 1, 1), null, null), null);
        long before = audits.count();

        assertThatThrownBy(() -> service.publishScale(AGENCY, scale.id(),
                new PublishDefinitionRequest(scale.recordVersion(), "Should fail"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one enabled level");
        assertThat(audits.count()).isEqualTo(before);

        assertThatThrownBy(() -> service.publishScale(AGENCY, scale.id(),
                new PublishDefinitionRequest(scale.recordVersion() + 1, "Stale"), null))
                .isInstanceOf(PublicationConflictException.class);
        assertThat(audits.count()).isEqualTo(before);
    }

    @Test
    void competencyPublicationRejectsMissingIndicatorsAndUnpublishedExactDependenciesWithoutAudit() {
        authenticate("publisher-validation");
        LocalDate start = LocalDate.of(2028, 6, 1);
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        AdminCategoryResponse category = service.createCategory(AGENCY,
                new DraftCategoryRequest("VC" + suffix, "Validation Category", null, 1,
                        start, null, null), null);
        category = service.publishCategory(AGENCY, category.id(),
                new PublishDefinitionRequest(category.recordVersion(), "Publish dependency category"), null);
        AdminScaleResponse publishedScale = service.createScale(AGENCY,
                new DraftScaleRequest("VS" + suffix, "Validation Scale", null, 1,
                        start, null, null), null);
        service.createLevel(AGENCY, publishedScale.id(),
                new DraftLevelRequest("L1", "Level 1", 1, null, start, null, null), null);
        publishedScale = service.listScales(AGENCY, DefinitionStatus.DRAFT,
                publishedScale.code(), null, 0, 20).content().get(0);
        publishedScale = service.publishScale(AGENCY, publishedScale.id(),
                new PublishDefinitionRequest(publishedScale.recordVersion(), "Publish dependency scale"), null);

        AdminCompetencyResponse missingIndicator = service.createCompetency(AGENCY,
                new DraftCompetencyRequest("VK" + suffix, "Missing indicator", "Definition",
                        category.id(), publishedScale.id(), 1, start, null, null), null);
        long beforeMissingIndicator = audits.count();
        assertThatThrownBy(() -> service.publishCompetency(AGENCY, missingIndicator.id(),
                new PublishDefinitionRequest(missingIndicator.recordVersion(), "Must fail"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("indicator");
        assertThat(audits.count()).isEqualTo(beforeMissingIndicator);

        AdminScaleResponse unpublishedScale = service.createScale(AGENCY,
                new DraftScaleRequest("VD" + suffix, "Unpublished exact scale", null, 1,
                        start, null, null), null);
        AdminLevelResponse unpublishedLevel = service.createLevel(AGENCY, unpublishedScale.id(),
                new DraftLevelRequest("L1", "Level 1", 1, null, start, null, null), null);
        AdminCompetencyResponse wrongVersionDependency = service.createCompetency(AGENCY,
                new DraftCompetencyRequest("VW" + suffix, "Wrong scale version", "Definition",
                        category.id(), unpublishedScale.id(), 1, start, null, null), null);
        service.createIndicator(AGENCY, wrongVersionDependency.id(),
                new DraftIndicatorRequest(unpublishedLevel.id(), "Behavior", null, 1,
                        start, null, null), null);
        wrongVersionDependency = service.listCompetencies(AGENCY, DefinitionStatus.DRAFT, null,
                wrongVersionDependency.code(), null, 0, 20).content().get(0);
        long beforeDependencyFailure = audits.count();
        AdminCompetencyResponse finalWrongVersionDependency = wrongVersionDependency;
        assertThatThrownBy(() -> service.publishCompetency(AGENCY, finalWrongVersionDependency.id(),
                new PublishDefinitionRequest(finalWrongVersionDependency.recordVersion(), "Must fail"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("scale must be published");
        assertThat(audits.count()).isEqualTo(beforeDependencyFailure);
    }

    @Test
    void overlappingLegacyChainRollsBackPredecessorClosureAndSuccessAudits() {
        authenticate("publisher-overlap");
        String code = code("OVR");
        CompetencyCategory first = categories.saveAndFlush(new CompetencyCategory(
                AGENCY, code, "Legacy version one", null, true, 1,
                LocalDate.of(2025, 1, 1), null));
        CompetencyCategory overlappingSecond = first.successorDraft();
        overlappingSecond.updateDraft(code, "Legacy version two", null, 1,
                LocalDate.of(2026, 1, 1), null);
        overlappingSecond.publish("legacy-import", java.time.Instant.now());
        overlappingSecond = categories.saveAndFlush(overlappingSecond);

        AdminCategoryResponse draft = service.versionCategory(AGENCY, overlappingSecond.getId(),
                new DraftTransitionRequest(overlappingSecond.getVersion(), "Prepare version three"), null);
        draft = service.updateCategory(AGENCY, draft.id(),
                new DraftCategoryRequest(code, "Version three", null, 1,
                        LocalDate.of(2027, 1, 1), null, draft.recordVersion()), null);
        long before = audits.count();
        AdminCategoryResponse finalDraft = draft;

        assertThatThrownBy(() -> service.publishCategory(AGENCY, finalDraft.id(),
                new PublishDefinitionRequest(finalDraft.recordVersion(), "Reject overlap"), null))
                .isInstanceOf(PublicationConflictException.class)
                .hasMessageContaining("cannot overlap");
        assertThat(audits.count()).isEqualTo(before);
        assertThat(categories.findByIdAndAgencyId(overlappingSecond.getId(), AGENCY).orElseThrow().getEffectiveTo())
                .isNull();

        long beforeIllegalTransition = audits.count();
        CompetencyCategory finalOverlappingSecond = overlappingSecond;
        assertThatThrownBy(() -> service.publishCategory(AGENCY, finalOverlappingSecond.getId(),
                new PublishDefinitionRequest(finalOverlappingSecond.getVersion(), "Cannot republish"), null))
                .isInstanceOf(IllegalLifecycleTransitionException.class);
        assertThat(audits.count()).isEqualTo(beforeIllegalTransition);
    }

    @Test
    void concurrentPublicationProducesOnePublishedVersionAndOneConflict() throws Exception {
        authenticate("publisher-concurrent-setup");
        AdminCategoryResponse draft = service.createCategory(AGENCY,
                new DraftCategoryRequest(code("RACE"), "Concurrent publication", null, 1,
                        LocalDate.of(2028, 1, 1), null, null), null);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Object> publication = () -> {
            authenticate("publisher-concurrent");
            ready.countDown();
            try {
                if (!start.await(5, TimeUnit.SECONDS)) {
                    return new IllegalStateException("Concurrent publication start timed out");
                }
                return service.publishCategory(AGENCY, draft.id(),
                        new PublishDefinitionRequest(draft.recordVersion(), "Concurrent publication"), null);
            } catch (RuntimeException exception) {
                return exception;
            } finally {
                SecurityContextHolder.clearContext();
            }
        };

        Future<Object> first = executor.submit(publication);
        Future<Object> second = executor.submit(publication);
        try {
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            Object firstResult = result(first);
            Object secondResult = result(second);

            assertThat(List.of(firstResult, secondResult))
                    .filteredOn(AdminCategoryResponse.class::isInstance)
                    .singleElement()
                    .satisfies(result -> assertThat(((AdminCategoryResponse) result).status()).isEqualTo("ACTIVE"));
            assertThat(List.of(firstResult, secondResult))
                    .filteredOn(RuntimeException.class::isInstance)
                    .singleElement();
            assertThat(categories.findByIdAndAgencyId(draft.id(), AGENCY).orElseThrow().getStatus())
                    .isEqualTo(DefinitionStatus.ACTIVE);
            assertThat(service.listAuditEvents(AGENCY, "COMPETENCY_CATEGORY", draft.id(), 0, 20).content())
                    .filteredOn(event -> event.action().equals("PUBLISH_DRAFT"))
                    .hasSize(1);
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    private static Object result(Future<Object> future) throws Exception {
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (ExecutionException exception) {
            return exception.getCause();
        }
    }

    private static void authenticate(String employeeNo) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(employeeNo, null, List.of()));
    }

    private static String code(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
