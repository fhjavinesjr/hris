package com.administrative.impl;

import com.administrative.entitymodels.PermissionRuleset;
import com.administrative.repositories.PermissionRulesetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.administrative.dtos.PermissionDataScope;

class EffectiveAuthorizationServiceImplTest {

    private final PermissionRulesetRepository repository = mock(PermissionRulesetRepository.class);
    private final EffectiveAuthorizationServiceImpl service =
            new EffectiveAuthorizationServiceImpl(repository, new ObjectMapper());

    @Test
    void resolvesOnlyCanonicalPersistedFeatureFlags() {
        PermissionRuleset ruleset = new PermissionRuleset("HR Editor", false,
                "{\"primehr.competency\":{\"canAccess\":true,\"canAdd\":false," +
                        "\"canEdit\":true,\"canDelete\":false,\"canPublish\":true}}");
        when(repository.findByPermissionNameIgnoreCase("HR Editor")).thenReturn(Optional.of(ruleset));

        var result = service.resolve("001", "ROLE_HR Editor", "primehr.competency");

        assertThat(result.administrator()).isFalse();
        assertThat(result.canAccess()).isTrue();
        assertThat(result.canAdd()).isFalse();
        assertThat(result.canEdit()).isTrue();
        assertThat(result.canDelete()).isFalse();
        assertThat(result.canPublish()).isTrue();
    }

    @Test
    void establishedAdministratorsRemainUnrestricted() {
        PermissionRuleset administratorRuleset = new PermissionRuleset("Agency Administrator", true, "{}");
        when(repository.findByPermissionNameIgnoreCase("Agency Administrator"))
                .thenReturn(Optional.of(administratorRuleset));

        assertThat(service.resolve("admin", "99", "primehr.competency").administrator()).isTrue();
        assertThat(service.resolve("001", "ROLE_1", "primehr.competency").administrator()).isTrue();
        assertThat(service.resolve("001", "Agency Administrator", "primehr.competency").administrator()).isTrue();
        assertThat(service.resolve("001", "Agency Administrator", "primehr.competency").canPublish()).isTrue();
    }

    @Test
    void roleOneUsesMatchingRulesetBeforeLegacyAdministratorFallback() {
        PermissionRuleset userRuleset = new PermissionRuleset("USER", false,
                "{\"primehr.competency\":{\"canAccess\":false}}");
        userRuleset.setPermissionId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(userRuleset));

        var permission = service.resolve("001", "ROLE_1", "primehr.competency");

        assertThat(permission.administrator()).isFalse();
        assertThat(permission.canAccess()).isFalse();
    }

    @Test
    void malformedOrMissingRulesFailClosed() {
        PermissionRuleset malformed = new PermissionRuleset("Broken", false, "not-json");
        when(repository.findByPermissionNameIgnoreCase("Broken")).thenReturn(Optional.of(malformed));

        assertThat(service.resolve("001", "Broken", "primehr.competency").canAccess()).isFalse();
        assertThat(service.resolve("001", "Missing", "primehr.competency").canAccess()).isFalse();
    }

    @Test
    void publishRequiresAccessAndMissingPublishFailsClosed() {
        PermissionRuleset noAccess = new PermissionRuleset("Publisher", false,
                "{\"primehr.competency\":{\"canAccess\":false,\"canPublish\":true}}");
        PermissionRuleset legacy = new PermissionRuleset("Legacy", false,
                "{\"primehr.competency\":{\"canAccess\":true,\"canEdit\":true}}");
        when(repository.findByPermissionNameIgnoreCase("Publisher")).thenReturn(Optional.of(noAccess));
        when(repository.findByPermissionNameIgnoreCase("Legacy")).thenReturn(Optional.of(legacy));

        assertThat(service.resolve("001", "Publisher", "primehr.competency").canPublish()).isFalse();
        assertThat(service.resolve("001", "Legacy", "primehr.competency").canPublish()).isFalse();
        assertThat(service.resolve("admin", "99", "primehr.competency").canPublish()).isTrue();
    }

    @Test
    void resolvesPositionProfileAccessIndependentlyFromCompetencyAccess() {
        PermissionRuleset ruleset = new PermissionRuleset("Profile Reader", false,
                "{\"primehr.competency\":{\"canAccess\":false}," +
                        "\"primehr.position-profile\":{\"canAccess\":true," +
                        "\"canSubmit\":true,\"canApprove\":false}}" );
        when(repository.findByPermissionNameIgnoreCase("Profile Reader")).thenReturn(Optional.of(ruleset));

        assertThat(service.resolve("001", "Profile Reader", "primehr.position-profile").canAccess()).isTrue();
        assertThat(service.resolve("001", "Profile Reader", "primehr.position-profile").canSubmit()).isTrue();
        assertThat(service.resolve("001", "Profile Reader", "primehr.position-profile").canApprove()).isFalse();
        assertThat(service.resolve("001", "Profile Reader", "primehr.competency").canAccess()).isFalse();
    }

    @Test
    void legacyProfileRulesFailClosedForSubmitAndApprove() {
        PermissionRuleset legacy = new PermissionRuleset("Legacy Profile Editor", false,
                "{\"primehr.position-profile\":{\"canAccess\":true,\"canEdit\":true}}" );
        when(repository.findByPermissionNameIgnoreCase("Legacy Profile Editor")).thenReturn(Optional.of(legacy));

        var permission = service.resolve("001", "Legacy Profile Editor", "primehr.position-profile");

        assertThat(permission.canAccess()).isTrue();
        assertThat(permission.canSubmit()).isFalse();
        assertThat(permission.canApprove()).isFalse();
        assertThat(service.resolve("admin", "99", "primehr.position-profile").canSubmit()).isTrue();
        assertThat(service.resolve("admin", "99", "primehr.position-profile").canApprove()).isTrue();
    }

    @Test
    void phaseThreeActionsAndScopeAreIndependentAndRequireAccess() {
        PermissionRuleset ruleset = new PermissionRuleset("Assessor", false,
                "{\"primehr.competency-assessment\":{\"canAccess\":true," +
                        "\"canAssess\":true,\"canSubmit\":true,\"canValidate\":false," +
                        "\"dataScope\":\"ASSIGNED_RECORDS\"}," +
                        "\"primehr.assessment-validation\":{\"canAccess\":false," +
                        "\"canValidate\":true,\"dataScope\":\"AGENCY_WIDE\"}}" );
        when(repository.findByPermissionNameIgnoreCase("Assessor")).thenReturn(Optional.of(ruleset));

        var assessment = service.resolve("001", "Assessor", "primehr.competency-assessment");
        assertThat(assessment.canAssess()).isTrue();
        assertThat(assessment.canSubmit()).isTrue();
        assertThat(assessment.canValidate()).isFalse();
        assertThat(assessment.dataScope()).isEqualTo(PermissionDataScope.ASSIGNED_RECORDS);

        var validation = service.resolve("001", "Assessor", "primehr.assessment-validation");
        assertThat(validation.canValidate()).isFalse();
        assertThat(validation.dataScope()).isEqualTo(PermissionDataScope.NONE);
    }

    @Test
    void missingOrInvalidPhaseThreeScopeFailsClosedAndAdministratorIsAgencyWide() {
        PermissionRuleset legacy = new PermissionRuleset("Legacy Assessment", false,
                "{\"primehr.assessment-administration\":{\"canAccess\":true," +
                        "\"canAdd\":true,\"dataScope\":\"DIRECT_SUBORDINATES\"}}" );
        when(repository.findByPermissionNameIgnoreCase("Legacy Assessment")).thenReturn(Optional.of(legacy));

        var permission = service.resolve("001", "Legacy Assessment", "primehr.assessment-administration");
        assertThat(permission.canAdd()).isTrue();
        assertThat(permission.canAssess()).isFalse();
        assertThat(permission.canValidate()).isFalse();
        assertThat(permission.canFinalize()).isFalse();
        assertThat(permission.dataScope()).isEqualTo(PermissionDataScope.NONE);

        assertThat(service.resolve("admin", "99", "primehr.person-profile").dataScope())
                .isEqualTo(PermissionDataScope.AGENCY_WIDE);
    }

    @Test
    void phaseFourFeaturesAreIndependentAndLegacyRulesFailClosed() {
        PermissionRuleset ruleset = new PermissionRuleset("Gap Analyst", false,
                "{\"primehr.gap-configuration\":{\"canAccess\":true,\"canAdd\":true," +
                        "\"canPublish\":false,\"dataScope\":\"AGENCY_WIDE\"}," +
                        "\"primehr.competency-gap\":{\"canAccess\":true,\"canAdd\":false," +
                        "\"dataScope\":\"OWN_RECORDS\"}}" );
        when(repository.findByPermissionNameIgnoreCase("Gap Analyst")).thenReturn(Optional.of(ruleset));

        var configuration = service.resolve("001", "Gap Analyst", "primehr.gap-configuration");
        assertThat(configuration.canAccess()).isTrue();
        assertThat(configuration.canAdd()).isTrue();
        assertThat(configuration.canPublish()).isFalse();
        assertThat(configuration.dataScope()).isEqualTo(PermissionDataScope.AGENCY_WIDE);

        var gaps = service.resolve("001", "Gap Analyst", "primehr.competency-gap");
        assertThat(gaps.canAccess()).isTrue();
        assertThat(gaps.canAdd()).isFalse();
        assertThat(gaps.dataScope()).isEqualTo(PermissionDataScope.OWN_RECORDS);

        var referral = service.resolve("001", "Gap Analyst", "primehr.ld-referral");
        assertThat(referral.canAccess()).isFalse();
        assertThat(referral.canSubmit()).isFalse();
        assertThat(referral.dataScope()).isEqualTo(PermissionDataScope.NONE);

        assertThat(service.resolve("admin", "99", "primehr.competency-gap").canAdd()).isTrue();
        assertThat(service.resolve("admin", "99", "primehr.ld-referral").canSubmit()).isTrue();
    }

    @Test
    void phaseFiveQualificationAndRspFeaturesAreIndependentAndFailClosed() {
        PermissionRuleset ruleset = new PermissionRuleset("RSP Planner", false,
                "{\"administrative.qualification-standard\":{\"canAccess\":true,\"canPublish\":false," +
                        "\"dataScope\":\"AGENCY_WIDE\"}," +
                        "\"primehr.rsp-recruitment-planning\":{\"canAccess\":true,\"canAdd\":true," +
                        "\"canEdit\":false,\"dataScope\":\"AGENCY_WIDE\"}}" );
        when(repository.findByPermissionNameIgnoreCase("RSP Planner")).thenReturn(Optional.of(ruleset));

        var qualification = service.resolve("001", "RSP Planner", "administrative.qualification-standard");
        assertThat(qualification.canAccess()).isTrue();
        assertThat(qualification.canPublish()).isFalse();

        var planning = service.resolve("001", "RSP Planner", "primehr.rsp-recruitment-planning");
        assertThat(planning.canAccess()).isTrue();
        assertThat(planning.canAdd()).isTrue();
        assertThat(planning.canEdit()).isFalse();
        assertThat(planning.dataScope()).isEqualTo(PermissionDataScope.AGENCY_WIDE);

        var publication = service.resolve("001", "RSP Planner", "primehr.rsp-vacancy-publication");
        assertThat(publication.canAccess()).isFalse();
        assertThat(publication.canPublish()).isFalse();
        assertThat(publication.dataScope()).isEqualTo(PermissionDataScope.NONE);
    }

    @Test
    void applicantIntakeAccessAndMessagePermissionAreIndependentAndAgencyScoped() {
        PermissionRuleset ruleset = new PermissionRuleset("Applicant Intake", false,
                "{\"primehr.rsp-applicant-intake\":{\"canAccess\":true,\"canAdd\":false," +
                        "\"canEdit\":true,\"dataScope\":\"AGENCY_WIDE\"}}" );
        when(repository.findByPermissionNameIgnoreCase("Applicant Intake")).thenReturn(Optional.of(ruleset));

        var permission = service.resolve("001", "Applicant Intake", "primehr.rsp-applicant-intake");

        assertThat(permission.canAccess()).isTrue();
        assertThat(permission.canAdd()).isFalse();
        assertThat(permission.canEdit()).isTrue();
        assertThat(permission.dataScope()).isEqualTo(PermissionDataScope.AGENCY_WIDE);
        assertThatThrownBy(() -> service.resolve("001", "Applicant Intake", "primehr.rsp-screening"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void screeningPolicyActionsAreIndependentAgencyWideAndFailClosed() {
        PermissionRuleset ruleset = new PermissionRuleset("Screening Policy", false,
                "{\"primehr.rsp-screening-policy\":{\"canAccess\":true,\"canAdd\":true," +
                        "\"canEdit\":false,\"canPublish\":true,\"dataScope\":\"AGENCY_WIDE\"}}" );
        when(repository.findByPermissionNameIgnoreCase("Screening Policy")).thenReturn(Optional.of(ruleset));

        var permission = service.resolve("001", "Screening Policy", "primehr.rsp-screening-policy");
        assertThat(permission.canAccess()).isTrue();
        assertThat(permission.canAdd()).isTrue();
        assertThat(permission.canEdit()).isFalse();
        assertThat(permission.canPublish()).isTrue();
        assertThat(permission.dataScope()).isEqualTo(PermissionDataScope.AGENCY_WIDE);
        assertThat(service.resolve("001", "Screening Policy", "primehr.rsp-application-screening").canAccess())
                .isFalse();
    }

    @Test
    void applicationScreeningActionsAreIndependentAgencyWideAndFailClosed() {
        PermissionRuleset ruleset = new PermissionRuleset("Application Screening", false,
                "{\"primehr.rsp-application-screening\":{\"canAccess\":true,\"canAdd\":true," +
                        "\"canEdit\":true,\"canSubmit\":true,\"canApprove\":false," +
                        "\"dataScope\":\"AGENCY_WIDE\"}}" );
        when(repository.findByPermissionNameIgnoreCase("Application Screening")).thenReturn(Optional.of(ruleset));

        var permission = service.resolve("001", "Application Screening", "primehr.rsp-application-screening");
        assertThat(permission.canAccess()).isTrue();
        assertThat(permission.canAdd()).isTrue();
        assertThat(permission.canEdit()).isTrue();
        assertThat(permission.canSubmit()).isTrue();
        assertThat(permission.canApprove()).isFalse();
        assertThat(permission.canPublish()).isFalse();
        assertThat(permission.dataScope()).isEqualTo(PermissionDataScope.AGENCY_WIDE);
        assertThat(service.resolve("001", "Application Screening", "primehr.rsp-screening-policy").canAccess())
                .isFalse();
    }
}
