package com.primehr.rsp.applicant;

import com.auth0.jwt.JWT;import com.auth0.jwt.algorithms.Algorithm;import com.primehr.security.ApplicantTokenService;import org.junit.jupiter.api.Test;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.*;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest @ActiveProfiles("test") class ApplicantTokenIsolationTest {
 @Autowired ApplicantTokenService tokens;
 @Test void applicantTokenHasDedicatedIssuerAudienceAndSubjectType(){String token=tokens.issue("applicant-id");var jwt=tokens.verify(token);assertThat(jwt.getIssuer()).isEqualTo(ApplicantTokenService.ISSUER);assertThat(jwt.getAudience()).containsExactly(ApplicantTokenService.AUDIENCE);assertThat(jwt.getSubject()).isEqualTo("applicant-id");assertThat(jwt.getClaim("subjectType").asString()).isEqualTo("APPLICANT");}
 @Test void employeeSignedTokenAndWrongAudienceAreRejected(){String employee=JWT.create().withSubject("001").withClaim("role","1").sign(Algorithm.HMAC256("test-only-secret-with-at-least-32-characters"));assertThatThrownBy(()->tokens.verify(employee)).isInstanceOf(RuntimeException.class);}
}
