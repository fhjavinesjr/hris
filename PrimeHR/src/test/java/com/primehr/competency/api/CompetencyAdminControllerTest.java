package com.primehr.competency.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.primehr.competency.application.CompetencyAdminService;
import com.primehr.config.PrimeHrProperties;
import com.primehr.security.*;
import com.primehr.shared.api.PageResponse;
import com.primehr.shared.exception.PrimeHrExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompetencyAdminController.class)
@Import({PrimeHrSecurityConfiguration.class, PrimeHrJwtAuthenticationFilter.class,
        ConfiguredCompetencyReadPermissionResolver.class, PrimeHrExceptionHandler.class,
        CompetencyAdminControllerTest.PropertyBinding.class})
@TestPropertySource(properties = {
        "primehr.security.jwt-secret=test-only-secret-with-at-least-32-characters",
        "primehr.security.competency-reader-roles=COMPETENCY_READER",
        "primehr.agency.id=TRUSTED-AGENCY",
        "primehr.cors.allowed-origins=http://localhost:3086",
        "primehr.administrative.base-url=http://localhost:18082"
})
class CompetencyAdminControllerTest {
    private static final String SECRET = "test-only-secret-with-at-least-32-characters";

    @EnableConfigurationProperties(PrimeHrProperties.class)
    static class PropertyBinding {}

    @Autowired private MockMvc mockMvc;
    @MockBean private CompetencyAdminService service;
    @MockBean private AgencyScopeResolver agencyScope;
    @MockBean private PrimeHrPermissionGuard permission;

    @Test
    void rejectsUnauthenticatedRequestsBeforeTheAuthorizationDependency() throws Exception {
        mockMvc.perform(get("/api/primehr/v1/admin/competencies"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(permission, service);
    }

    @Test
    void exactActionDenialReturnsForbidden() throws Exception {
        String authorization = "Bearer " + token("2");
        doThrow(new AccessDeniedException("denied")).when(permission)
                .require(PrimeHrAction.ADD, authorization);

        mockMvc.perform(post("/api/primehr/v1/admin/competency-categories")
                        .header("Authorization", authorization)
                        .contentType("application/json")
                        .content("""
                                {"code":"CORE","name":"Core","displayOrder":1}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));
        verifyNoInteractions(service);
    }

    @Test
    void authenticatedReadUsesOnlyServerResolvedAgencyAndApprovedFilters() throws Exception {
        String authorization = "Bearer " + token("2");
        when(agencyScope.resolveAgencyId(any())).thenReturn("TRUSTED-AGENCY");
        when(service.listCompetencies(eq("TRUSTED-AGENCY"), eq(null), eq("category-1"),
                eq("lead"), eq(null), eq(0), eq(20)))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, true, true));

        mockMvc.perform(get("/api/primehr/v1/admin/competencies")
                        .header("Authorization", authorization)
                        .param("agencyId", "ATTACKER-AGENCY")
                        .param("categoryId", "category-1")
                        .param("search", "lead"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(permission).require(PrimeHrAction.ACCESS, authorization);
        verify(service).listCompetencies("TRUSTED-AGENCY", null, "category-1", "lead", null, 0, 20);
    }

    @Test
    void hardDeleteAndPatchAreNeverExposed() throws Exception {
        String authorization = "Bearer " + token("1");
        mockMvc.perform(delete("/api/primehr/v1/admin/competencies/id")
                        .header("Authorization", authorization)).andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/primehr/v1/admin/competencies/id")
                        .header("Authorization", authorization)).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    private static String token(String role) {
        return JWT.create().withSubject("001").withClaim("role", role)
                .withExpiresAt(new Date(System.currentTimeMillis() + 60_000))
                .sign(Algorithm.HMAC256(SECRET));
    }
}
