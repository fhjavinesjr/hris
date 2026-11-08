package com.primehr.notification;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.primehr.config.PrimeHrProperties;
import com.primehr.notification.api.NotificationController;
import com.primehr.notification.application.NotificationService;
import com.primehr.security.ConfiguredCompetencyReadPermissionResolver;
import com.primehr.security.ConfiguredSingleAgencyScopeResolver;
import com.primehr.security.PrimeHrJwtAuthenticationFilter;
import com.primehr.security.PrimeHrSecurityConfiguration;
import com.primehr.shared.api.PageResponse;
import com.primehr.shared.exception.PrimeHrExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import({PrimeHrSecurityConfiguration.class, PrimeHrJwtAuthenticationFilter.class,
        ConfiguredCompetencyReadPermissionResolver.class, ConfiguredSingleAgencyScopeResolver.class,
        PrimeHrExceptionHandler.class, NotificationSecurityTest.PropertyBinding.class})
@TestPropertySource(properties = {
        "primehr.security.jwt-secret=test-only-secret-with-at-least-32-characters",
        "primehr.security.competency-reader-roles=COMPETENCY_READER",
        "primehr.agency.id=TRUSTED-AGENCY",
        "primehr.cors.allowed-origins=http://localhost:3086"
})
class NotificationSecurityTest {
    private static final String SECRET = "test-only-secret-with-at-least-32-characters";

    @EnableConfigurationProperties(PrimeHrProperties.class)
    static class PropertyBinding { }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService service;

    @Test
    void rejectsUnauthenticatedFeed() throws Exception {
        mockMvc.perform(get("/api/primehr/v1/notifications/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void authenticatedEmployeeWithoutCompetencyPermissionReadsOnlyOwnFeed() throws Exception {
        when(service.feed("TRUSTED-AGENCY", "EMP-00012", false, false, 0, 20))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, true, true));

        mockMvc.perform(get("/api/primehr/v1/notifications/me")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(service).feed("TRUSTED-AGENCY", "EMP-00012", false, false, 0, 20);
    }

    private static String token() {
        return JWT.create()
                .withSubject("EMP-00012")
                .withClaim("role", "NO_COMPETENCY_ACCESS")
                .withExpiresAt(new Date(System.currentTimeMillis() + 60_000))
                .sign(Algorithm.HMAC256(SECRET));
    }
}
