package com.primehr.positionprofile.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.primehr.config.PrimeHrProperties;
import com.primehr.positionprofile.application.PositionProfileAdminService;
import com.primehr.security.*;
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
import com.primehr.integration.administrative.EffectiveFeaturePermission;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PositionProfileAdminController.class)
@Import({PrimeHrSecurityConfiguration.class, PrimeHrJwtAuthenticationFilter.class,
        ConfiguredCompetencyReadPermissionResolver.class, PrimeHrExceptionHandler.class,
        PositionProfileAdminControllerTest.PropertyBinding.class})
@TestPropertySource(properties = {
        "primehr.security.jwt-secret=test-only-secret-with-at-least-32-characters",
        "primehr.security.competency-reader-roles=COMPETENCY_READER",
        "primehr.agency.id=TRUSTED-AGENCY",
        "primehr.cors.allowed-origins=http://localhost:3086",
        "primehr.administrative.base-url=http://localhost:18082"
})
class PositionProfileAdminControllerTest {
    private static final String SECRET = "test-only-secret-with-at-least-32-characters";

    @EnableConfigurationProperties(PrimeHrProperties.class)
    static class PropertyBinding {
    }

    @Autowired private MockMvc mockMvc;
    @MockBean private PositionProfileAdminService service;
    @MockBean private AgencyScopeResolver agencyScope;
    @MockBean private PositionProfilePermissionGuard permission;

    @Test
    void unauthenticatedRequestIsRejectedBeforePositionAuthorization() throws Exception {
        mockMvc.perform(get("/api/primehr/v1/admin/position-profiles"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(permission, service);
    }

    @Test
    void dedicatedAddDenialReturnsForbidden() throws Exception {
        String authorization = "Bearer " + token("2");
        doThrow(new AccessDeniedException("denied")).when(permission)
                .require(PrimeHrAction.ADD, authorization);

        mockMvc.perform(post("/api/primehr/v1/admin/position-profiles")
                        .header("Authorization", authorization)
                        .contentType("application/json")
                        .content("""
                                {"targetType":"JOB_POSITION","targetId":14,"name":"Profile"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));
        verifyNoInteractions(service);
    }

    @Test
    void createPassesTrustedAgencyAndBearerTokenToTheService() throws Exception {
        String authorization = "Bearer " + token("2");
        org.mockito.Mockito.when(agencyScope.resolveAgencyId(any())).thenReturn("TRUSTED-AGENCY");

        mockMvc.perform(post("/api/primehr/v1/admin/position-profiles")
                        .header("Authorization", authorization)
                        .header("X-Correlation-Id", "profile-create")
                        .contentType("application/json")
                        .content("""
                                {"targetType":"JOB_POSITION","targetId":14,"name":"Profile"}
                                """))
                .andExpect(status().isCreated());

        verify(permission).require(PrimeHrAction.ADD, authorization);
        verify(service).create(eq("TRUSTED-AGENCY"), any(CreatePositionProfileRequest.class),
                eq(authorization), eq("profile-create"));
    }

    @Test
    void submitRequiresDedicatedSubmitPermissionAndPassesTrustedContext() throws Exception {
        String authorization = "Bearer " + token("2");
        org.mockito.Mockito.when(agencyScope.resolveAgencyId(any())).thenReturn("TRUSTED-AGENCY");

        mockMvc.perform(post("/api/primehr/v1/admin/position-profiles/profile-1/submit")
                        .header("Authorization", authorization)
                        .header("X-Correlation-Id", "submit-correlation")
                        .contentType("application/json")
                        .content("{\"recordVersion\":3}"))
                .andExpect(status().isOk());

        verify(permission).require(PrimeHrAction.SUBMIT, authorization);
        verify(service).submit(eq("TRUSTED-AGENCY"), eq("profile-1"),
                any(SubmitPositionProfileRequest.class), eq(authorization), eq("submit-correlation"));
    }

    @Test
    void approveUsesDedicatedPermissionAndServerVerifiedAdministratorFlag() throws Exception {
        String authorization = "Bearer " + token("1");
        org.mockito.Mockito.when(agencyScope.resolveAgencyId(any())).thenReturn("TRUSTED-AGENCY");
        org.mockito.Mockito.when(permission.require(PrimeHrAction.APPROVE, authorization)).thenReturn(
                new EffectiveFeaturePermission(PositionProfilePermissionGuard.FEATURE_KEY,
                        true, true, true, true, true, false, true, true));

        mockMvc.perform(post("/api/primehr/v1/admin/position-profiles/profile-1/approve")
                        .header("Authorization", authorization)
                        .header("X-Correlation-Id", "approve-correlation")
                        .contentType("application/json")
                        .content("{\"recordVersion\":4,\"reason\":\"Emergency administrator override\"}"))
                .andExpect(status().isOk());

        verify(service).approve(eq("TRUSTED-AGENCY"), eq("profile-1"),
                any(ApprovePositionProfileRequest.class), eq(authorization), eq(true),
                eq("approve-correlation"));
    }

    private static String token(String role) {
        return JWT.create().withSubject("EMP-00001").withClaim("role", role)
                .withExpiresAt(new Date(System.currentTimeMillis() + 60_000))
                .sign(Algorithm.HMAC256(SECRET));
    }
}
