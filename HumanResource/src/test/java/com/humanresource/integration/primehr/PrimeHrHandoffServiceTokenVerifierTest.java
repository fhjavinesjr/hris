package com.humanresource.integration.primehr;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrimeHrHandoffServiceTokenVerifierTest {
    private static final String SECRET = "phase-5e-test-secret-at-least-32-chars";
    private final PrimeHrHandoffServiceTokenVerifier verifier =
            new PrimeHrHandoffServiceTokenVerifier(SECRET, "primehr", "humanresource");

    @Test
    void acceptsTheRequiredAudienceScopeAndAgency() {
        String token = token(List.of("appointment-handoff.write"), "agency-1");

        var principal = verifier.verify("Bearer " + token);

        assertEquals("primehr-service", principal.subject());
        assertEquals("agency-1", principal.agencyId());
    }

    @Test
    void rejectsATokenWithoutTheDedicatedScope() {
        String token = token(List.of("unrelated.read"), "agency-1");

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> verifier.verify("Bearer " + token));
        assertEquals(401, error.getStatusCode().value());
    }

    private String token(List<String> scopes, String agencyId) {
        Instant now = Instant.now();
        return JWT.create().withIssuer("primehr").withAudience("humanresource").withSubject("primehr-service")
                .withClaim("scope", scopes).withClaim("agencyId", agencyId).withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(60))).sign(Algorithm.HMAC256(SECRET));
    }
}
