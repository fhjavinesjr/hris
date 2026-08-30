package com.primehr.rsp.applicant;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.primehr.rsp.applicant.api.ApplicantDtos;
import com.primehr.rsp.applicant.application.ApplicantFoundationService;
import com.primehr.rsp.applicant.domain.PrivacyNotice;
import com.primehr.rsp.applicant.infrastructure.PrivacyNoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApplicantHttpSecurityIsolationTest {
    private static final String EMPLOYEE_SECRET = "test-only-secret-with-at-least-32-characters";

    @Autowired private MockMvc mockMvc;
    @Autowired private ApplicantFoundationService service;
    @Autowired private PrivacyNoticeRepository notices;

    private String applicantToken;

    @BeforeEach
    void registerApplicant() {
        PrivacyNotice notice = notices.save(new PrivacyNotice("TEST-AGENCY", "Privacy Notice",
                "Recruitment privacy terms", "Agency retention policy", 1,
                LocalDate.now().minusDays(1), null, PrivacyNotice.Status.ACTIVE));
        applicantToken = service.register(new ApplicantDtos.Register("isolation@example.com",
                "correct-password-123", "Token", "Isolation", notice.getId(), true),
                "127.0.0.1", "test").token();
    }

    @Test
    void applicantTokenCanReachOnlyApplicantSelfService() throws Exception {
        mockMvc.perform(get("/api/primehr/applicant/v1/me")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("isolation@example.com"));

        mockMvc.perform(get("/api/primehr/v1/rsp/recruitment-plans")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/primehr/applicant/v1/me/applications")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/primehr/v1/rsp/applications")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/primehr/applicant/v1/session")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void employeeTokenCannotReachApplicantSelfService() throws Exception {
        String employeeToken = JWT.create().withSubject("001").withClaim("role", "1")
                .sign(Algorithm.HMAC256(EMPLOYEE_SECRET));
        mockMvc.perform(get("/api/primehr/applicant/v1/me")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/primehr/applicant/v1/me/applications")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }
}
