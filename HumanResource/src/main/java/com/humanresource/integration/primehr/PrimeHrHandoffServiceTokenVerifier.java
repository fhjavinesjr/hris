package com.humanresource.integration.primehr;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
class PrimeHrHandoffServiceTokenVerifier {
    private static final String REQUIRED_SCOPE = "appointment-handoff.write";
    private final JWTVerifier verifier;

    PrimeHrHandoffServiceTokenVerifier(
            @Value("${primehr.handoff.jwt-secret:change-me-before-enabling-primehr-intake}") String secret,
            @Value("${primehr.handoff.issuer:primehr}") String issuer,
            @Value("${primehr.handoff.audience:humanresource}") String audience) {
        verifier = JWT.require(Algorithm.HMAC256(secret)).withIssuer(issuer).withAudience(audience).build();
    }

    ServicePrincipal verify(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw unauthorized("A PrimeHR service bearer token is required");
        }
        try {
            DecodedJWT jwt = verifier.verify(authorization.substring(7));
            List<String> scopes = jwt.getClaim("scope").asList(String.class);
            String agencyId = jwt.getClaim("agencyId").asString();
            if (scopes == null || !scopes.contains(REQUIRED_SCOPE) || agencyId == null || agencyId.isBlank()) {
                throw unauthorized("The PrimeHR service token lacks the required scope or agency claim");
            }
            return new ServicePrincipal(jwt.getSubject(), agencyId);
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw unauthorized("The PrimeHR service bearer token is invalid");
        }
    }

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }

    record ServicePrincipal(String subject, String agencyId) {
    }
}
