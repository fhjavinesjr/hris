package com.administrative.controllers;

import com.administrative.dtos.EffectiveFeaturePermissionResponse;
import com.administrative.dtos.PositionTargetType;
import com.administrative.services.EffectiveAuthorizationService;
import com.administrative.services.PositionTargetService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PositionTargetIntegrationControllerTest {
    private final PositionTargetService targets = mock(PositionTargetService.class);
    private final EffectiveAuthorizationService authorization = mock(EffectiveAuthorizationService.class);
    private final PositionTargetIntegrationController controller =
            new PositionTargetIntegrationController(targets, authorization);

    @Test
    void authorizedReadUsesTheDedicatedFeaturePermission() {
        var authentication = authentication();
        when(authorization.resolve("EMP-00001", "ROLE_2", "primehr.position-profile"))
                .thenReturn(new EffectiveFeaturePermissionResponse("primehr.position-profile",
                        false, true, false, false, false, false, false, false));

        controller.get(authentication, PositionTargetType.JOB_POSITION, 14L);

        verify(targets).get(PositionTargetType.JOB_POSITION, 14L);
    }

    @Test
    void deniedPermissionCannotReadPositionMasters() {
        var authentication = authentication();
        when(authorization.resolve("EMP-00001", "ROLE_2", "primehr.position-profile"))
                .thenReturn(EffectiveFeaturePermissionResponse.denied("primehr.position-profile"));

        assertThatThrownBy(() -> controller.get(authentication, PositionTargetType.JOB_POSITION, 14L))
                .isInstanceOf(AccessDeniedException.class);
    }

    private static UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken("EMP-00001", "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_2")));
    }
}
