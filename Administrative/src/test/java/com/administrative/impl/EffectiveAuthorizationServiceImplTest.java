package com.administrative.impl;

import com.administrative.entitymodels.PermissionRuleset;
import com.administrative.repositories.PermissionRulesetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
}
