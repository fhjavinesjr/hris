package com.primehr.notification;

import com.primehr.notification.api.NotificationController;
import com.primehr.notification.application.NotificationService;
import com.primehr.security.AgencyScopeResolver;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class NotificationControllerTest {
    @Test
    void feedAlwaysUsesAuthenticatedEmployeeIdentity() {
        NotificationService service = mock(NotificationService.class);
        AgencyScopeResolver agency = mock(AgencyScopeResolver.class);
        var authentication = new TestingAuthenticationToken("EMP-00012", "", "ROLE_USER");
        when(agency.resolveAgencyId(authentication)).thenReturn("A");
        var controller = new NotificationController(service, agency);

        controller.feed(authentication, false, false, 0, 20);

        verify(service).feed("A", "EMP-00012", false, false, 0, 20);
    }

    @Test
    void blankIdentityIsDenied() {
        var controller = new NotificationController(mock(NotificationService.class),
                mock(AgencyScopeResolver.class));
        assertThatThrownBy(() -> controller.feed(null, false, false, 0, 20))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    @Test
    void targetResolutionAlwaysUsesAuthenticatedEmployeeIdentity() {
        NotificationService service = mock(NotificationService.class);
        AgencyScopeResolver agency = mock(AgencyScopeResolver.class);
        var authentication = new TestingAuthenticationToken("EMP-00012", "", "ROLE_USER");
        when(agency.resolveAgencyId(authentication)).thenReturn("A");
        var controller = new NotificationController(service, agency);

        controller.resolveTarget(authentication, "notice-1", "correlation");

        verify(service).resolveTarget("A", "EMP-00012", "notice-1", "correlation");
    }
}
