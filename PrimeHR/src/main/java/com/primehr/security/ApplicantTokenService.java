package com.primehr.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.primehr.config.PrimeHrProperties;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service @ConditionalOnProperty(name="primehr.applicant.enabled",havingValue="true")
public class ApplicantTokenService {
    public static final String ISSUER = "isoft-primehr-applicant";
    public static final String AUDIENCE = "primehr-applicant";
    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long tokenMinutes;

    public ApplicantTokenService(PrimeHrProperties properties) {
        String secret = properties.applicant().jwtSecret();
        if (secret.length() < 32) throw new IllegalStateException("PRIMEHR_APPLICANT_JWT_SECRET must contain at least 32 characters");
        algorithm = Algorithm.HMAC256(secret);
        verifier = JWT.require(algorithm).withIssuer(ISSUER).withAudience(AUDIENCE).build();
        tokenMinutes = properties.applicant().tokenMinutes();
    }

    public String issue(String applicantId) {
        Instant now = Instant.now();
        return JWT.create().withIssuer(ISSUER).withAudience(AUDIENCE).withSubject(applicantId)
                .withClaim("subjectType", "APPLICANT").withIssuedAt(now)
                .withExpiresAt(now.plus(tokenMinutes, ChronoUnit.MINUTES)).sign(algorithm);
    }

    public DecodedJWT verify(String token) {
        DecodedJWT jwt = verifier.verify(token);
        if (!"APPLICANT".equals(jwt.getClaim("subjectType").asString())) throw new IllegalArgumentException("Invalid applicant token");
        return jwt;
    }
}
