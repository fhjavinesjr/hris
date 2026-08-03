package com.primehr.competency.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.primehr.competency.application.CompetencyQueryService;
import com.primehr.config.PrimeHrProperties;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompetencyQueryController.class)
@Import({PrimeHrSecurityConfiguration.class, PrimeHrJwtAuthenticationFilter.class,
        ConfiguredCompetencyReadPermissionResolver.class, ConfiguredSingleAgencyScopeResolver.class,
        PrimeHrExceptionHandler.class, CompetencyQueryControllerTest.PropertyBinding.class})
@TestPropertySource(properties = {
        "primehr.security.jwt-secret=test-only-secret-with-at-least-32-characters",
        "primehr.security.competency-reader-roles=COMPETENCY_READER",
        "primehr.agency.id=TRUSTED-AGENCY",
        "primehr.cors.allowed-origins=http://localhost:3086"
})
class CompetencyQueryControllerTest {

    private static final String SECRET = "test-only-secret-with-at-least-32-characters";

    @EnableConfigurationProperties(PrimeHrProperties.class)
    static class PropertyBinding {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompetencyQueryService service;

    @Test
    void rejectsUnauthenticatedRead() throws Exception {
        mockMvc.perform(get("/api/primehr/v1/competencies"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void rejectsAuthenticatedUserWithoutCompetencyPermission() throws Exception {
        mockMvc.perform(get("/api/primehr/v1/competencies")
                        .header("Authorization", "Bearer " + token("NO_ACCESS")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void authorizedReaderUsesTrustedAgencyAndCannotOverrideIt() throws Exception {
        when(service.listCompetencies(eq("TRUSTED-AGENCY"), eq(null), eq(true), eq("commun"), any(), eq(0), eq(20)))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, true, true));

        mockMvc.perform(get("/api/primehr/v1/competencies")
                        .header("Authorization", "Bearer " + token("COMPETENCY_READER"))
                        .param("agencyId", "ATTACKER-CONTROLLED")
                        .param("active", "true")
                        .param("search", "commun"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.content").isArray());

        verify(service).listCompetencies(eq("TRUSTED-AGENCY"), eq(null), eq(true), eq("commun"), any(), eq(0), eq(20));
    }

    @Test
    void establishedAdministratorRoleRemainsAuthorized() throws Exception {
        when(service.listCategories(eq("TRUSTED-AGENCY"), eq(null), eq(null))).thenReturn(List.of());

        mockMvc.perform(get("/api/primehr/v1/competency-categories")
                        .header("Authorization", "Bearer " + token("1")))
                .andExpect(status().isOk());

        verify(service).listCategories("TRUSTED-AGENCY", null, null);
    }

    @Test
    void deniesEveryWriteMethodForAuthorizedReader() throws Exception {
        String authorization = "Bearer " + token("COMPETENCY_READER");

        mockMvc.perform(post("/api/primehr/v1/competencies")
                        .header("Authorization", authorization))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/primehr/v1/competencies/id")
                        .header("Authorization", authorization))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/primehr/v1/competencies/id")
                        .header("Authorization", authorization))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/primehr/v1/competencies/id")
                        .header("Authorization", authorization))
                .andExpect(status().isForbidden());
    }

    private static String token(String role) {
        return JWT.create()
                .withSubject("001")
                .withClaim("role", role)
                .withExpiresAt(new Date(System.currentTimeMillis() + 60_000))
                .sign(Algorithm.HMAC256(SECRET));
    }
}
