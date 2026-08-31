package com.administrative.controllers;

import com.administrative.dtos.*;
import com.administrative.services.EffectiveAuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EffectiveAuthorizationControllerTest {
    private final EffectiveAuthorizationService service = mock(EffectiveAuthorizationService.class);
    private final EffectiveAuthorizationController controller = new EffectiveAuthorizationController(service);

    @Test void privilegedAssignmentCallerCanResolveAuthoritativeTargetPermission() {
        var caller = permission(false, true, true, false, false, PermissionDataScope.AGENCY_WIDE);
        var target = permission(false, true, false, true, false, PermissionDataScope.AGENCY_WIDE);
        when(service.resolve("ASSIGNER", "ROLE_ASSIGNER", "primehr.rsp-application-screening")).thenReturn(caller);
        when(service.resolve("SCR-1", "SCREENER", "primehr.rsp-application-screening")).thenReturn(target);
        assertThat(controller.effectiveEmployee(auth(), "SCR-1", "SCREENER", "primehr.rsp-application-screening")).isSameAs(target);
    }

    @Test void callerWithoutAgencyWideAddPermissionCannotInspectAnotherEmployee() {
        when(service.resolve("ASSIGNER", "ROLE_ASSIGNER", "primehr.rsp-application-screening"))
                .thenReturn(permission(false, true, false, true, false, PermissionDataScope.ASSIGNED_RECORDS));
        assertThatThrownBy(() -> controller.effectiveEmployee(auth(), "SCR-1", "SCREENER", "primehr.rsp-application-screening"))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("403 FORBIDDEN");
        verify(service, never()).resolve("SCR-1", "SCREENER", "primehr.rsp-application-screening");
    }

    @Test void assignmentLookupCannotBeUsedToInspectUnrelatedFeaturePermissions() {
        assertThatThrownBy(() -> controller.effectiveEmployee(auth(), "SCR-1", "SCREENER",
                "primehr.payroll"))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("400 BAD_REQUEST");
        verifyNoInteractions(service);
    }

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken("ASSIGNER", "", List.of(new SimpleGrantedAuthority("ROLE_ASSIGNER")));
    }
    private EffectiveFeaturePermissionResponse permission(boolean admin, boolean access, boolean add, boolean submit, boolean approve, PermissionDataScope scope) {
        return new EffectiveFeaturePermissionResponse("primehr.rsp-application-screening", admin, access, add, false, false, false, submit, approve, false, false, false, scope);
    }
}
