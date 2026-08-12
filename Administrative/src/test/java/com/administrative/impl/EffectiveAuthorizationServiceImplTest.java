package com.administrative.impl;

import com.administrative.entitymodels.PermissionRuleset;
import com.administrative.repositories.PermissionRulesetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
}
