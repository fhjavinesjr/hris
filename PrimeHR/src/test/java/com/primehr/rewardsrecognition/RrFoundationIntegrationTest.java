package com.primehr.rewardsrecognition;

import com.primehr.rewardsrecognition.api.RrFoundationDtos.*;
import com.primehr.rewardsrecognition.application.RrFoundationService;
import com.primehr.rewardsrecognition.domain.RrAwardCategoryVersion;
import com.primehr.rewardsrecognition.domain.RrCommitteeCharterVersion;
import com.primehr.rewardsrecognition.domain.RrCommitteeMember;
import com.primehr.shared.audit.PrimeHrAuditEventRepository;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import com.primehr.shared.exception.OptimisticConflictException;
import com.primehr.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RrFoundationIntegrationTest {
    @Autowired RrFoundationService service;
    @Autowired PrimeHrAuditEventRepository audit;

    @Test
    void praisePolicyIsVersionedEffectiveAuditedAndAgencyScoped() {
        String agency = "RR-POLICY";
        PolicyResponse draft = service.createPolicy(agency, policy(null), "create");
        PolicyResponse published = service.publishPolicy(agency, draft.id(),
                new PublishTransition(draft.recordVersion(), LocalDate.of(2026, 1, 1), null, "approved"), "publish");

        assertThat(service.effectivePolicy(agency, LocalDate.of(2026, 6, 1)).id()).isEqualTo(published.id());
        assertThatThrownBy(() -> service.updatePolicy(agency, published.id(), policy(published.recordVersion()), null))
                .isInstanceOf(IllegalLifecycleTransitionException.class);

        PolicyResponse revision = service.revisePolicy(agency, published.id(),
                new DefinitionTransition(published.recordVersion(), "annual policy review"), null);
        PolicyResponse successor = service.publishPolicy(agency, revision.id(),
                new PublishTransition(revision.recordVersion(), LocalDate.of(2027, 1, 1), null, "approved"), null);
        assertThat(successor.definitionVersion()).isEqualTo(2);
        assertThat(service.policy(agency, published.id()).effectiveTo()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThatThrownBy(() -> service.policy("OTHER", published.id())).isInstanceOf(ResourceNotFoundException.class);
        assertThat(audit.count()).isGreaterThanOrEqualTo(4);
    }

    @Test
    void categoryCriteriaMustTotalOneHundredAndFreezeOnPublication() {
        String agency = "RR-CATEGORY";
        CategoryResponse category = service.createCategory(agency, category(null), null);
        CategoryResponse emptyCategory = category;
        assertThatThrownBy(() -> service.replaceCriteria(agency, emptyCategory.id(),
                new CriteriaInput(emptyCategory.recordVersion(), List.of(criterion("ONLY", "60", 1))), null))
                .hasMessageContaining("100");

        category = service.replaceCriteria(agency, category.id(), new CriteriaInput(category.recordVersion(), List.of(
                criterion("IMPACT", "60", 1), criterion("INNOVATION", "40", 2))), null);
        category = service.publishCategory(agency, category.id(),
                new PublishTransition(category.recordVersion(), LocalDate.of(2026, 1, 1), null, "approved"), null);
        assertThat(category.criteria()).hasSize(2);
        CategoryResponse frozen = category;
        assertThatThrownBy(() -> service.replaceCriteria(agency, frozen.id(),
                new CriteriaInput(frozen.recordVersion(), List.of(criterion("NEW", "100", 1))), null))
                .isInstanceOf(IllegalLifecycleTransitionException.class);
    }

    @Test
    void committeeRosterEnforcesChairSecretariatVotingAndOptimisticLock() {
        String agency = "RR-COMMITTEE";
        CommitteeCharterResponse charter = service.createCharter(agency, charter(null), null);
        CommitteeCharterResponse emptyCharter = charter;
        assertThatThrownBy(() -> service.replaceRoster(agency, emptyCharter.id(),
                new CommitteeRosterInput(emptyCharter.recordVersion(), List.of(member("001", RrCommitteeMember.Role.MEMBER, true))), null))
                .hasMessageContaining("chair");

        charter = service.replaceRoster(agency, charter.id(), new CommitteeRosterInput(charter.recordVersion(), List.of(
                member("001", RrCommitteeMember.Role.CHAIR, true),
                member("002", RrCommitteeMember.Role.MEMBER, true),
                member("003", RrCommitteeMember.Role.SECRETARIAT, false))), null);
        CommitteeCharterResponse updated = service.updateCharter(agency, charter.id(),
                new CommitteeCharterInput("PRAISE-COMMITTEE", "PRAISE Committee 2026", null,
                        "Agency PRAISE Policy", RrCommitteeCharterVersion.QuorumType.COUNT,
                        new BigDecimal("2"), true, true, true, charter.recordVersion()), null);
        CommitteeCharterResponse stale = charter;
        assertThatThrownBy(() -> service.updateCharter(agency, stale.id(), charter(stale.recordVersion()), null))
                .isInstanceOf(OptimisticConflictException.class);
        updated = service.publishCharter(agency, updated.id(),
                new PublishTransition(updated.recordVersion(), LocalDate.of(2026, 1, 1), null, "approved"), null);
        assertThat(updated.members()).hasSize(3);
    }

    private static PolicyInput policy(Long version) {
        return new PolicyInput("PRAISE", "Agency PRAISE Policy", null, "CSC MC No. 1, s. 2001",
                "Agency PRAISE Resolution", true, true, true, true, true, true, true, true, version);
    }

    private static CategoryInput category(Long version) {
        return new CategoryInput("MERIT", "Award of Merit", null,
                RrAwardCategoryVersion.NomineeType.INDIVIDUAL,
                RrAwardCategoryVersion.IncentiveType.BOTH,
                "Review the published eligibility rules", "Documented contribution and validation",
                new BigDecimal("80.00"), version);
    }

    private static CriterionInput criterion(String code, String weight, int order) {
        return new CriterionInput(code, code, null, new BigDecimal(weight), new BigDecimal("100"),
                "Attach validated evidence", order);
    }

    private static CommitteeCharterInput charter(Long version) {
        return new CommitteeCharterInput("PRAISE-COMMITTEE", "PRAISE Committee", null,
                "Agency PRAISE Policy", RrCommitteeCharterVersion.QuorumType.COUNT,
                new BigDecimal("2"), true, true, true, version);
    }

    private static CommitteeMemberInput member(String employeeNo, RrCommitteeMember.Role role, boolean voting) {
        return new CommitteeMemberInput(employeeNo, "Employee " + employeeNo, "Position " + employeeNo,
                role, voting, LocalDate.of(2026, 1, 1), null, "HRM-v1");
    }
}
