package com.primehr.rewardsrecognition;

import com.primehr.integration.administrative.AdministrativePerformancePlanningClient;
import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.integration.humanresource.HumanResourceAssessmentSubject;
import com.primehr.integration.humanresource.HumanResourceAssessmentSubjectClient;
import com.primehr.integration.humanresource.HumanResourceEmployeeDirectoryClient;
import com.primehr.integration.humanresource.HumanResourceEmployeeDirectoryEntry;
import com.primehr.rewardsrecognition.api.RrFoundationDtos.*;
import com.primehr.rewardsrecognition.api.RrNominationDtos.*;
import com.primehr.rewardsrecognition.application.RrFoundationService;
import com.primehr.rewardsrecognition.application.RrNominationService;
import com.primehr.rewardsrecognition.domain.RrAwardCategoryVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RrNominationServiceIntegrationTest {
    @Autowired RrFoundationService foundation;
    @Autowired RrNominationService nominations;
    @MockBean HumanResourceEmployeeDirectoryClient directory;
    @MockBean HumanResourceAssessmentSubjectClient subjects;
    @MockBean AdministrativePerformancePlanningClient organization;

    @AfterEach void clearSecurity() { SecurityContextHolder.clearContext(); }

    @Test
    void completeSubjectIsPersistedBeforeDatabaseCheckAndNomineeCannotEditNominatorsDraft() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("EMP-1", null, List.of()));
        String agency = "RR-NOMINATION";
        PolicyResponse policy = foundation.createPolicy(agency, new PolicyInput("PRAISE", "PRAISE", null,
                "CSC MC No. 1, s. 2001", "Resolution", true, true, true, true,
                true, true, true, true, null), null);
        policy = foundation.publishPolicy(agency, policy.id(),
                new PublishTransition(policy.recordVersion(), LocalDate.of(2026, 1, 1), null, "approved"), null);
        CategoryResponse category = foundation.createCategory(agency, new CategoryInput("MERIT", "Merit", null,
                RrAwardCategoryVersion.NomineeType.INDIVIDUAL, RrAwardCategoryVersion.IncentiveType.BOTH,
                "Published eligibility", "Documented contribution", new BigDecimal("80"), null), null);
        category = foundation.replaceCriteria(agency, category.id(), new CriteriaInput(category.recordVersion(),
                List.of(new CriterionInput("IMPACT", "Impact", null, new BigDecimal("100"),
                        new BigDecimal("100"), "Evidence", 1))), null);
        category = foundation.publishCategory(agency, category.id(),
                new PublishTransition(category.recordVersion(), LocalDate.of(2026, 1, 1), null, "approved"), null);

        CycleResponse cycle = nominations.createCycle(agency, new CycleInput("CY-2026", "2026 PRAISE", null,
                policy.id(), LocalDate.now(), LocalDate.now().plusDays(2), LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(4), LocalDate.now().plusDays(5), null, null), null);
        cycle = nominations.transitionCycle(agency, cycle.id(),
                new Transition(cycle.recordVersion(), "Call approved"), "open", null);

        when(directory.require("EMP-1", "Bearer token"))
                .thenReturn(new HumanResourceEmployeeDirectoryEntry(1L, "EMP-1", "Nora", "Nominator", null, "Employee"));
        when(subjects.get(1L, "Bearer token")).thenReturn(subject(1L, "EMP-1", "Nora Nominator"));
        when(subjects.get(2L, "Bearer token")).thenReturn(subject(2L, "EMP-2", "Nina Nominee"));
        when(organization.membership(1L, "Bearer token")).thenReturn(membership(1L));
        when(organization.membership(2L, "Bearer token")).thenReturn(membership(2L));

        NominationInput input = new NominationInput(cycle.id(), category.id(), 2L, null, List.of(), null,
                null, "Documented contribution", null, null, List.of(), null);
        NominationResponse created = nominations.createNomination(agency, input, "Bearer token", "EMP-1",
                PermissionDataScope.AGENCY_WIDE, null);

        assertThat(created.status()).isEqualTo("DRAFT");
        assertThat(created.individualEmployeeNo()).isEqualTo("EMP-2");
        NominationInput staleEdit = new NominationInput(cycle.id(), category.id(), 2L, null, List.of(), null,
                null, "Changed by nominee", null, null, List.of(), created.recordVersion());
        assertThatThrownBy(() -> nominations.updateNomination(agency, created.id(), staleEdit, "Bearer token",
                "EMP-2", PermissionDataScope.OWN_RECORDS, null)).isInstanceOf(AccessDeniedException.class);
    }

    private static HumanResourceAssessmentSubject subject(Long id, String no, String name) {
        return new HumanResourceAssessmentSubject(id, no, name, true, id + 100, LocalDateTime.of(2025, 1, 1, 8, 0),
                1L, 1L, "employee-" + id, LocalDateTime.of(2026, 1, 1, 8, 0), Instant.now());
    }

    private static AdministrativePerformancePlanningClient.PersonnelMembership membership(Long id) {
        return new AdministrativePerformancePlanningClient.PersonnelMembership(id, 10L, "BU-10", "Office",
                1L, "Area", false, false, "MAIN", "organization-" + id, Instant.now());
    }
}
