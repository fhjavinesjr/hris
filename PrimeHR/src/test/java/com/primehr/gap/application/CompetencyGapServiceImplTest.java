package com.primehr.gap.application;

import com.primehr.assessment.domain.PersonCompetencyProfile;
import com.primehr.assessment.domain.PersonCompetencyResult;
import com.primehr.assessment.infrastructure.PersonCompetencyProfileRepository;
import com.primehr.assessment.infrastructure.PersonCompetencyResultRepository;
import com.primehr.competency.domain.Competency;
import com.primehr.competency.domain.ProficiencyLevel;
import com.primehr.competency.domain.ProficiencyScale;
import com.primehr.gap.api.CompetencyGapDtos.GenerateRequest;
import com.primehr.gap.domain.*;
import com.primehr.gap.infrastructure.*;
import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.integration.humanresource.HumanResourceAssessmentSubject;
import com.primehr.integration.humanresource.HumanResourceAssessmentSubjectClient;
import com.primehr.positionprofile.domain.*;
import com.primehr.positionprofile.infrastructure.*;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.OptimisticConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CompetencyGapServiceImplTest {
    private static final String AGENCY = "TEST-AGENCY";
    private final HumanResourceAssessmentSubjectClient subjects = mock(HumanResourceAssessmentSubjectClient.class);
    private final PositionProfileRepository positionProfiles = mock(PositionProfileRepository.class);
    private final PositionProfileRequirementRepository requirements = mock(PositionProfileRequirementRepository.class);
    private final PersonCompetencyProfileRepository personProfiles = mock(PersonCompetencyProfileRepository.class);
    private final PersonCompetencyResultRepository personResults = mock(PersonCompetencyResultRepository.class);
    private final GapPrioritySchemeRepository schemes = mock(GapPrioritySchemeRepository.class);
    private final GapPriorityRuleRepository rules = mock(GapPriorityRuleRepository.class);
    private final CompetencyGapAnalysisRepository analyses = mock(CompetencyGapAnalysisRepository.class);
    private final CompetencyGapItemRepository items = mock(CompetencyGapItemRepository.class);
    private final PrimeHrAuditService audit = mock(PrimeHrAuditService.class);
    private CompetencyGapService service;

    @BeforeEach
    void setUp() {
        service = new CompetencyGapServiceImpl(subjects, positionProfiles, requirements, personProfiles,
                personResults, schemes, rules, analyses, items, audit);
    }

    @Test
    void dynamicFiveLevelScaleProducesBelowMeetsExceedsAndExplicitNotAssessedResults() {
        HumanResourceAssessmentSubject subject = subject("fingerprint-current");
        when(subjects.get(7L, "Bearer token")).thenReturn(subject);
        PositionProfile profile = positionProfile("position-profile", PositionTargetType.PLANTILLA);
        when(positionProfiles.findAll(org.mockito.ArgumentMatchers.<Specification<PositionProfile>>any(),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(profile)));
        PersonCompetencyProfile personProfile = personProfile("person-profile");
        when(personProfiles.findEffective(eq(AGENCY), eq("007"), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(List.of(personProfile));
        GapPriorityScheme scheme = priorityScheme("scheme");
        when(schemes.findEffective(eq(AGENCY), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(List.of(scheme));
        when(analyses.findByAgencyIdAndRequestKey(AGENCY, "request-1")).thenReturn(Optional.empty());
        when(analyses.findByAgencyIdAndSubjectEmployeeIdAndAnalysisDateAndPositionProfileIdAndPersonProfileIdAndPrioritySchemeId(
                eq(AGENCY), eq(7L), any(LocalDate.class), eq("position-profile"), eq("person-profile"), eq("scheme")))
                .thenReturn(Optional.empty());
        when(analyses.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        GapPriorityLevel priorityLevel = mock(GapPriorityLevel.class);
        when(priorityLevel.getCode()).thenReturn("ACTION");
        when(priorityLevel.getLabel()).thenReturn("Action required");
        when(priorityLevel.getPriorityRank()).thenReturn(1);
        GapPriorityRule fallback = mock(GapPriorityRule.class);
        when(fallback.matches(any(), any(), any(), any())).thenReturn(true);
        when(fallback.getPriorityLevel()).thenReturn(priorityLevel);
        when(fallback.getExplanation()).thenReturn("Configured agency fallback");
        when(rules.findBySchemeIdAndAgencyIdAndActiveTrueOrderByDisplayOrderAsc("scheme", AGENCY))
                .thenReturn(List.of(fallback));

        ProficiencyScale scale = scale("scale-v1", 1);
        RequirementFixture below = requirement("C-BELOW", "Below", scale, 5, 2, 0);
        RequirementFixture meets = requirement("C-MEETS", "Meets", scale, 3, 3, 1);
        RequirementFixture exceeds = requirement("C-EXCEEDS", "Exceeds", scale, 2, 4, 2);
        RequirementFixture missing = requirement("C-MISSING", "Missing", scale, 1, null, 3);
        RequirementFixture mismatch = requirement("C-VERSION", "Versioned", scale, 4, null, 4);
        when(requirements.findByProfileIdAndAgencyIdAndActiveTrueOrderByDisplayOrderAscIdAsc(
                "position-profile", AGENCY)).thenReturn(List.of(below.requirement(), meets.requirement(),
                exceeds.requirement(), missing.requirement(), mismatch.requirement()));
        Competency olderSameCode = competency("older-version", "C-VERSION", "Old version", scale);
        PersonCompetencyResult olderResult = result(olderSameCode, level("old-level", "L2", 2, scale));
        when(personResults.findByPersonProfileIdOrderByCompetencyCode("person-profile")).thenReturn(List.of(
                below.result(), meets.result(), exceeds.result(), olderResult));

        List<CompetencyGapItem> stored = new ArrayList<>();
        when(items.saveAll(any())).thenAnswer(invocation -> {
            stored.addAll(invocation.getArgument(0));
            return stored;
        });
        when(items.findByAnalysisIdOrderByDisplayOrderAscCompetencyCodeAsc(any())).thenAnswer(invocation -> stored);

        var response = service.generate(AGENCY, new GenerateRequest(7L, "fingerprint-current", "request-1"),
                "Bearer token", "generator", "corr-gap");

        assertThat(response.items()).extracting(item -> item.classification()).containsExactly(
                GapClassification.BELOW, GapClassification.MEETS, GapClassification.EXCEEDS,
                GapClassification.NOT_ASSESSED, GapClassification.NOT_ASSESSED);
        assertThat(response.items()).extracting(item -> item.gap()).containsExactly(3, 0, -2, null, null);
        assertThat(response.items().get(3).notAssessedReason()).isEqualTo(NotAssessedReason.NO_RESULT);
        assertThat(response.items().get(4).notAssessedReason()).isEqualTo(NotAssessedReason.VERSION_NOT_COMPARABLE);
        assertThat(response.items().subList(0, 1)).allSatisfy(item -> assertThat(item.priorityCode()).isEqualTo("ACTION"));
        assertThat(response.items().subList(1, 3)).allSatisfy(item -> assertThat(item.priorityCode()).isNull());
        verify(audit).record(eq(AGENCY), eq("GENERATE_GAP_ANALYSIS"), eq("COMPETENCY_GAP_ANALYSIS"),
                nullable(String.class), eq(1), eq(0L), isNull(), any(), isNull(), eq("corr-gap"));
    }

    @Test
    void staleHrmFingerprintFailsBeforeAnyAnalysisIsPersisted() {
        when(analyses.findByAgencyIdAndRequestKey(AGENCY, "stale-request")).thenReturn(Optional.empty());
        when(subjects.get(7L, "Bearer token")).thenReturn(subject("new-fingerprint"));
        assertThatThrownBy(() -> service.generate(AGENCY,
                new GenerateRequest(7L, "old-fingerprint", "stale-request"),
                "Bearer token", "generator", null))
                .isInstanceOf(OptimisticConflictException.class).hasMessageContaining("changed");
        verifyNoInteractions(positionProfiles, personProfiles, schemes, requirements, items);
        verify(analyses, never()).save(any());
    }

    @Test
    void missingPersonProfileFailsWithoutPartialAnalysisRows() {
        when(analyses.findByAgencyIdAndRequestKey(AGENCY, "missing-profile")).thenReturn(Optional.empty());
        when(subjects.get(7L, "Bearer token")).thenReturn(subject("fingerprint-current"));
        PositionProfile profile = positionProfile("position-profile", PositionTargetType.PLANTILLA);
        when(positionProfiles.findAll(org.mockito.ArgumentMatchers.<Specification<PositionProfile>>any(),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(profile)));
        when(personProfiles.findEffective(eq(AGENCY), eq("007"), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.generate(AGENCY,
                new GenerateRequest(7L, "fingerprint-current", "missing-profile"),
                "Bearer token", "generator", null))
                .hasMessageContaining("No valid person competency profile");
        verify(analyses, never()).saveAndFlush(any());
        verifyNoInteractions(items);
    }

    @Test
    void ownRecordScopeCannotReadAnotherEmployee() {
        assertThatThrownBy(() -> service.list(AGENCY, "008", null, null, 0, 20,
                "007", PermissionDataScope.OWN_RECORDS)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(analyses);
    }

    private static HumanResourceAssessmentSubject subject(String fingerprint) {
        return new HumanResourceAssessmentSubject(7L, "007", "Employee Seven", true, 70L,
                LocalDateTime.now().minusYears(1), 14L, 99L, fingerprint,
                LocalDateTime.now(), Instant.now());
    }

    private static PositionProfile positionProfile(String id, PositionTargetType type) {
        PositionProfile profile = mock(PositionProfile.class);
        when(profile.getId()).thenReturn(id);
        when(profile.getAgencyId()).thenReturn(AGENCY);
        when(profile.getTargetType()).thenReturn(type);
        when(profile.getSourceJobPositionName()).thenReturn("Position");
        when(profile.getSourcePlantillaName()).thenReturn("Plantilla");
        when(profile.getDefinitionVersion()).thenReturn(2);
        when(profile.getContentRevision()).thenReturn(4L);
        return profile;
    }

    private static PersonCompetencyProfile personProfile(String id) {
        PersonCompetencyProfile profile = mock(PersonCompetencyProfile.class);
        when(profile.getId()).thenReturn(id);
        when(profile.getAgencyId()).thenReturn(AGENCY);
        when(profile.getProfileVersion()).thenReturn(3);
        when(profile.getValidFrom()).thenReturn(LocalDate.now().minusDays(30));
        return profile;
    }

    private static GapPriorityScheme priorityScheme(String id) {
        GapPriorityScheme scheme = mock(GapPriorityScheme.class);
        when(scheme.getId()).thenReturn(id);
        when(scheme.getAgencyId()).thenReturn(AGENCY);
        when(scheme.getCode()).thenReturn("AGENCY-POLICY");
        when(scheme.getDefinitionVersion()).thenReturn(1);
        return scheme;
    }

    private static RequirementFixture requirement(String code, String name, ProficiencyScale scale,
                                                   int requiredOrder, Integer attainedOrder, int displayOrder) {
        Competency competency = competency("id-" + code, code, name, scale);
        ProficiencyLevel required = level("required-" + code, "L" + requiredOrder, requiredOrder, scale);
        PositionProfileRequirement requirement = mock(PositionProfileRequirement.class);
        when(requirement.getId()).thenReturn("requirement-" + code);
        when(requirement.getCompetency()).thenReturn(competency);
        when(requirement.getRequiredProficiencyLevel()).thenReturn(required);
        when(requirement.getClassification()).thenReturn(RequirementClassification.MANDATORY);
        when(requirement.getCriticalityCode()).thenReturn("CORE");
        when(requirement.getDisplayOrder()).thenReturn(displayOrder);
        PersonCompetencyResult result = attainedOrder == null ? null
                : result(competency, level("attained-" + code, "L" + attainedOrder, attainedOrder, scale));
        return new RequirementFixture(requirement, result);
    }

    private static Competency competency(String id, String code, String name, ProficiencyScale scale) {
        Competency competency = mock(Competency.class);
        when(competency.getId()).thenReturn(id);
        when(competency.getCode()).thenReturn(code);
        when(competency.getName()).thenReturn(name);
        when(competency.getDefinitionVersion()).thenReturn(1);
        when(competency.getProficiencyScale()).thenReturn(scale);
        return competency;
    }

    private static ProficiencyScale scale(String id, int definitionVersion) {
        ProficiencyScale scale = mock(ProficiencyScale.class);
        when(scale.getId()).thenReturn(id);
        when(scale.getDefinitionVersion()).thenReturn(definitionVersion);
        return scale;
    }

    private static ProficiencyLevel level(String id, String code, int order, ProficiencyScale scale) {
        ProficiencyLevel level = mock(ProficiencyLevel.class);
        when(level.getId()).thenReturn(id);
        when(level.getCode()).thenReturn(code);
        when(level.getLabel()).thenReturn("Level " + order);
        when(level.getLevelOrder()).thenReturn(order);
        when(level.getScale()).thenReturn(scale);
        return level;
    }

    private static PersonCompetencyResult result(Competency competency, ProficiencyLevel attained) {
        PersonCompetencyResult result = mock(PersonCompetencyResult.class);
        when(result.getCompetency()).thenReturn(competency);
        when(result.getAttainedLevel()).thenReturn(attained);
        return result;
    }

    private record RequirementFixture(PositionProfileRequirement requirement, PersonCompetencyResult result) { }
}
