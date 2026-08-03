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
                        "\"canEdit\":true,\"canDelete\":false}}");
        when(repository.findByPermissionNameIgnoreCase("HR Editor")).thenReturn(Optional.of(ruleset));

        var result = service.resolve("001", "ROLE_HR Editor", "primehr.competency");

        assertThat(result.administrator()).isFalse();
        assertThat(result.canAccess()).isTrue();
        assertThat(result.canAdd()).isFalse();
        assertThat(result.canEdit()).isTrue();
        assertThat(result.canDelete()).isFalse();
    }

    @Test
    void establishedAdministratorsRemainUnrestricted() {
        assertThat(service.resolve("admin", "99", "primehr.competency").administrator()).isTrue();
        assertThat(service.resolve("001", "ROLE_1", "primehr.competency").administrator()).isTrue();
    }

    @Test
    void malformedOrMissingRulesFailClosed() {
        PermissionRuleset malformed = new PermissionRuleset("Broken", false, "not-json");
        when(repository.findByPermissionNameIgnoreCase("Broken")).thenReturn(Optional.of(malformed));

        assertThat(service.resolve("001", "Broken", "primehr.competency").canAccess()).isFalse();
        assertThat(service.resolve("001", "Missing", "primehr.competency").canAccess()).isFalse();
    }
}
