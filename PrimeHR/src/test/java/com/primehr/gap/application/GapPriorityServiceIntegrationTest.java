package com.primehr.gap.application;

import com.primehr.gap.api.GapPriorityDtos.*;
import com.primehr.gap.domain.GapClassification;
import com.primehr.gap.domain.GapPrioritySchemeStatus;
import com.primehr.shared.audit.PrimeHrAuditEventRepository;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class GapPriorityServiceIntegrationTest {
    private String agency;

    @Autowired GapPriorityService service;
    @Autowired PrimeHrAuditEventRepository audits;

    @BeforeEach void isolateAgency() {
        agency = "TEST-GAP-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @AfterEach void clearSecurity() { SecurityContextHolder.clearContext(); }

    @Test
    void publicationRequiresFallbackCoverageAndPublishedDefinitionsAreImmutable() {
        authenticate("gap-admin");
        String code = "GAP-" + UUID.randomUUID().toString().substring(0, 8);
        SchemeResponse scheme = service.create(agency, new CreateSchemeRequest(code, "Agency priorities",
                null, 0, LocalDate.now().minusDays(1), null), "gap-create");
        scheme = service.addLevel(agency, scheme.id(), new CreateLevelRequest(
                "URGENT", "Urgent", null, 1, 0), "level-create");
        String levelId = scheme.levels().get(0).id();
        scheme = service.addRule(agency, scheme.id(), new CreateRuleRequest(GapClassification.BELOW,
                null, null, null, null, levelId, "Fallback below", 0), "below-rule");

        SchemeResponse incomplete = scheme;
        assertThatThrownBy(() -> service.publish(agency, incomplete.id(),
                new PublishRequest(incomplete.recordVersion(), "Publish incomplete"), "publish-incomplete"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NOT_ASSESSED");

        scheme = service.addRule(agency, scheme.id(), new CreateRuleRequest(GapClassification.NOT_ASSESSED,
                null, null, null, null, levelId, "Fallback not assessed", 1), "missing-rule");
        long auditBefore = audits.count();
        SchemeResponse active = service.publish(agency, scheme.id(),
                new PublishRequest(scheme.recordVersion(), "Approved agency policy"), "publish-gap");

        assertThat(active.status()).isEqualTo(GapPrioritySchemeStatus.ACTIVE);
        assertThat(active.rules()).hasSize(2);
        assertThat(audits.count()).isEqualTo(auditBefore + 1);
        assertThatThrownBy(() -> service.addLevel(agency, active.id(),
                new CreateLevelRequest("LOW", "Low", null, 2, 1), "late-change"))
                .isInstanceOf(IllegalLifecycleTransitionException.class);
    }

    @Test
    void successorClonesPolicyAndClosesPredecessorEffectivity() {
        authenticate("gap-publisher");
        SchemeResponse first = completeDraft("CHAIN-" + UUID.randomUUID().toString().substring(0, 8),
                LocalDate.now().minusDays(20));
        first = service.publish(agency, first.id(),
                new PublishRequest(first.recordVersion(), "Initial policy"), "publish-first");
        SchemeResponse successor = service.createSuccessor(agency, first.id(),
                new TransitionRequest(first.recordVersion(), "Annual revision"), "successor");
        LocalDate successorFrom = LocalDate.now().plusDays(10);
        successor = service.update(agency, successor.id(), new UpdateSchemeRequest(successor.code(),
                successor.name(), successor.description(), successor.displayOrder(), successorFrom, null,
                successor.recordVersion()), "successor-date");
        SchemeResponse active = service.publish(agency, successor.id(),
                new PublishRequest(successor.recordVersion(), "Approved successor"), "publish-successor");

        SchemeResponse closed = service.get(agency, first.id());
        assertThat(active.definitionVersion()).isEqualTo(2);
        assertThat(active.levels()).hasSameSizeAs(first.levels());
        assertThat(active.rules()).hasSameSizeAs(first.rules());
        assertThat(closed.effectiveTo()).isEqualTo(successorFrom.minusDays(1));
    }

    @Test
    void notAssessedRuleCannotUseNumericRangeAndDuplicateLevelRankIsRejected() {
        authenticate("gap-admin");
        SchemeResponse scheme = service.create(agency, new CreateSchemeRequest(
                "VALID-" + UUID.randomUUID().toString().substring(0, 8), "Validation", null,
                0, LocalDate.now(), null), "create");
        scheme = service.addLevel(agency, scheme.id(), new CreateLevelRequest(
                "P1", "Priority 1", null, 1, 0), "level");
        SchemeResponse withLevel = scheme;
        String levelId = scheme.levels().get(0).id();
        assertThatThrownBy(() -> service.addLevel(agency, withLevel.id(), new CreateLevelRequest(
                "P2", "Priority 2", null, 1, 1), "duplicate-rank"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("rank");
        assertThatThrownBy(() -> service.addRule(agency, withLevel.id(), new CreateRuleRequest(
                GapClassification.NOT_ASSESSED, 1, 2, null, null, levelId, null, 0), "bad-rule"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("numeric gap");
    }

    private SchemeResponse completeDraft(String code, LocalDate effectiveFrom) {
        SchemeResponse scheme = service.create(agency, new CreateSchemeRequest(code, code,
                null, 0, effectiveFrom, null), "create-complete");
        scheme = service.addLevel(agency, scheme.id(), new CreateLevelRequest(
                "ACTION", "Action", null, 1, 0), "level-complete");
        String levelId = scheme.levels().get(0).id();
        scheme = service.addRule(agency, scheme.id(), new CreateRuleRequest(GapClassification.BELOW,
                null, null, null, null, levelId, "Below fallback", 0), "below-complete");
        return service.addRule(agency, scheme.id(), new CreateRuleRequest(GapClassification.NOT_ASSESSED,
                null, null, null, null, levelId, "Missing fallback", 1), "missing-complete");
    }

    private static void authenticate(String employeeNo) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(employeeNo, null, List.of()));
    }
}
