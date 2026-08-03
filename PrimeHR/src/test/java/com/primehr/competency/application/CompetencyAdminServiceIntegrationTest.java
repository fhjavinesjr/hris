package com.primehr.competency.application;

import com.primehr.competency.api.*;
import com.primehr.competency.domain.*;
import com.primehr.competency.infrastructure.*;
import com.primehr.shared.audit.PrimeHrAuditEventRepository;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import com.primehr.shared.exception.OptimisticConflictException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class CompetencyAdminServiceIntegrationTest {
    private static final String AGENCY = "TEST-AGENCY";

    @Autowired private CompetencyAdminService service;
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

    private static void authenticate(String employeeNo) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(employeeNo, null, List.of()));
    }

    private static String code(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
